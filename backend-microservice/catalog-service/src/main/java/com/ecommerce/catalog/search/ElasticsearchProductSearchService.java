package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ecommerce.catalog.model.request.ElasticsearchProductSearchRequest;
import com.ecommerce.catalog.model.response.ProductAutocompleteResponse;
import com.ecommerce.catalog.model.response.StorefrontProductSearchItem;
import com.ecommerce.catalog.model.response.StorefrontProductSearchItem.CategorySummary;
import com.ecommerce.catalog.model.response.StorefrontSearchResponse;
import com.ecommerce.catalog.model.response.StorefrontSearchResponse.FacetBucket;
import com.ecommerce.catalog.model.response.StorefrontSearchResponse.RangeFacetBucket;
import com.ecommerce.catalog.model.response.StorefrontSearchResponse.SearchFacets;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchProductSearchService {
    private final ElasticsearchProductSearchQueryBuilder queryBuilder;
    private final ElasticsearchProductSearchClient searchClient;
    private final SearchCursorCodec cursorCodec;
    private final MeterRegistry meterRegistry;
    private final String pitKeepAlive;
    private final long pitCursorMaxAgeMillis;

    public ElasticsearchProductSearchService(
            ElasticsearchProductSearchQueryBuilder queryBuilder,
            ElasticsearchProductSearchClient searchClient,
            SearchCursorCodec cursorCodec,
            MeterRegistry meterRegistry,
            @Value("${catalog.search.elasticsearch.pit.keep-alive:2m}") String pitKeepAlive,
            @Value("${catalog.search.elasticsearch.pit.cursor-max-age-millis:120000}") long pitCursorMaxAgeMillis) {
        this.queryBuilder = queryBuilder;
        this.searchClient = searchClient;
        this.cursorCodec = cursorCodec;
        this.meterRegistry = meterRegistry;
        this.pitKeepAlive = pitKeepAlive;
        this.pitCursorMaxAgeMillis = pitCursorMaxAgeMillis;
    }

    @SuppressWarnings("rawtypes")
    public StorefrontSearchResponse search(ElasticsearchProductSearchRequest request) {
        queryBuilder.validateRequest(request);
        Timer.Sample sample = Timer.start(meterRegistry);
        String activePitId = null;
        boolean openedPointInTime = false;
        try {
            SearchCursorCodec.DecodedCursor cursor = decodeCursor(request);
            activePitId = cursor == null
                    ? searchClient.openPointInTime(queryBuilder.indexAlias(), pitKeepAlive)
                    : cursor.pitId();
            openedPointInTime = cursor == null;
            if (activePitId == null || activePitId.isBlank()) {
                throw new IOException("Elasticsearch returned an empty PIT id");
            }
            SearchRequest searchRequest = queryBuilder.build(request, activePitId,
                    cursor == null ? null : cursor.values(), pitKeepAlive);
            SearchResponse<Map> response = searchClient.search(searchRequest);
            List<Hit<Map>> hits = response.hits().hits();
            List<StorefrontProductSearchItem> items = hits.stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> toStorefrontItem(hit.source(), hit.highlight()))
                    .toList();
            long total = cursor == null
                    ? (response.hits().total() == null ? items.size() : response.hits().total().value())
                    : cursor.totalHits();
            long totalPages = total == 0 ? 0 : (total + request.getSize() - 1) / request.getSize();
            if (total == 0) {
                meterRegistry.counter("catalog.search.zero_results", "has_keyword",
                        request.getQ() == null || request.getQ().isBlank() ? "false" : "true").increment();
            }
            boolean hasNextPage = !hits.isEmpty() && request.getPage() + 1L < totalPages;
            String responsePitId = response.pitId() == null || response.pitId().isBlank()
                    ? activePitId : response.pitId();
            activePitId = responsePitId;
            String effectiveSort = queryBuilder.effectiveSort(request);
            String nextCursor = hasNextPage
                    ? cursorCodec.encode(responsePitId, effectiveSort, cursorCodec.fingerprint(request, effectiveSort),
                    request.getPage() + 1, request.getSize(), total,
                    hits.get(hits.size() - 1).sort(), System.currentTimeMillis())
                    : null;
            if (!hasNextPage) closePointInTimeQuietly(responsePitId);
            return new StorefrontSearchResponse(items, request.getPage(), request.getSize(), total, totalPages,
                    nextCursor, cursor == null ? facets(response.aggregations()) : SearchFacets.empty());
        } catch (ElasticsearchException exception) {
            if (openedPointInTime) closePointInTimeQuietly(activePitId);
            if (request.getCursor() != null && !request.getCursor().isBlank() && exception.status() == 404) {
                throw new IllegalArgumentException("SEARCH_CURSOR_EXPIRED", exception);
            }
            meterRegistry.counter("catalog.search.errors", "operation", "search").increment();
            throw new ElasticsearchSearchUnavailableException("PRODUCT_SEARCH_TEMPORARILY_UNAVAILABLE", exception);
        } catch (IOException exception) {
            if (openedPointInTime) closePointInTimeQuietly(activePitId);
            meterRegistry.counter("catalog.search.errors", "operation", "search").increment();
            throw new ElasticsearchSearchUnavailableException("PRODUCT_SEARCH_TEMPORARILY_UNAVAILABLE", exception);
        } catch (RuntimeException exception) {
            if (openedPointInTime) closePointInTimeQuietly(activePitId);
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("catalog.search.elasticsearch.latency", "operation", "search"));
        }
    }

    public boolean closePointInTime(String cursorValue) {
        SearchCursorCodec.DecodedCursor cursor = cursorCodec.decode(cursorValue);
        try {
            return searchClient.closePointInTime(cursor.pitId());
        } catch (IOException | ElasticsearchException exception) {
            throw new ElasticsearchSearchUnavailableException("PRODUCT_SEARCH_PIT_CLOSE_FAILED", exception);
        }
    }

    private SearchCursorCodec.DecodedCursor decodeCursor(ElasticsearchProductSearchRequest request) {
        String value = request.getCursor();
        if (value == null || value.isBlank()) return null;
        String effectiveSort = queryBuilder.effectiveSort(request);
        SearchCursorCodec.DecodedCursor cursor = cursorCodec.decode(value, effectiveSort,
                cursorCodec.fingerprint(request, effectiveSort), request.getPage(), request.getSize());
        long age = System.currentTimeMillis() - cursor.issuedAtEpochMillis();
        if (age < 0L || age > pitCursorMaxAgeMillis) {
            closePointInTimeQuietly(cursor.pitId());
            throw new IllegalArgumentException("SEARCH_CURSOR_EXPIRED");
        }
        return cursor;
    }

    private void closePointInTimeQuietly(String pitId) {
        if (pitId == null || pitId.isBlank()) return;
        try {
            searchClient.closePointInTime(pitId);
        } catch (IOException | ElasticsearchException ignored) {
            meterRegistry.counter("catalog.search.errors", "operation", "pit_close").increment();
        }
    }

    @SuppressWarnings("rawtypes")
    public ProductAutocompleteResponse autocomplete(String keyword, int size) {
        SearchRequest request = queryBuilder.buildAutocomplete(keyword, size);
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            SearchResponse<Map> response = searchClient.search(request);
            List<ProductAutocompleteResponse.Suggestion> suggestions = response.hits().hits().stream()
                    .map(Hit::source).filter(source -> source != null)
                    .map(source -> new ProductAutocompleteResponse.Suggestion(
                            string(source.get("id")), string(source.get("name")), string(source.get("categoryName"))))
                    .toList();
            return new ProductAutocompleteResponse(suggestions);
        } catch (IOException | ElasticsearchException exception) {
            meterRegistry.counter("catalog.search.errors", "operation", "autocomplete").increment();
            throw new ElasticsearchSearchUnavailableException("PRODUCT_AUTOCOMPLETE_TEMPORARILY_UNAVAILABLE", exception);
        } finally {
            sample.stop(meterRegistry.timer("catalog.search.elasticsearch.latency", "operation", "autocomplete"));
        }
    }

    private SearchFacets facets(Map<String, Aggregate> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) return SearchFacets.empty();
        return new SearchFacets(
                rootTerms(aggregations.get("categories")),
                rootTerms(aggregations.get("shops")),
                priceRanges(aggregations.get("price_ranges")),
                selectionTerms(aggregations.get("colors")),
                selectionTerms(aggregations.get("sizes")));
    }

    private List<FacetBucket> rootTerms(Aggregate root) {
        if (root == null || !root.isFilter()) return List.of();
        Aggregate values = root.filter().aggregations().get("values");
        if (values == null || !values.isSterms() || !values.sterms().buckets().isArray()) return List.of();
        return values.sterms().buckets().array().stream()
                .map(bucket -> new FacetBucket(bucket.key().stringValue(), bucket.docCount())).toList();
    }

    private List<FacetBucket> selectionTerms(Aggregate root) {
        Aggregate values = descendant(root, "variants", "active", "selections", "axis", "values");
        if (values == null || !values.isSterms() || !values.sterms().buckets().isArray()) return List.of();
        return values.sterms().buckets().array().stream()
                .map(bucket -> new FacetBucket(bucket.key().stringValue(), productCount(bucket))).toList();
    }

    private List<RangeFacetBucket> priceRanges(Aggregate root) {
        Aggregate values = descendant(root, "variants", "active", "values");
        if (values == null || !values.isRange() || !values.range().buckets().isArray()) return List.of();
        return values.range().buckets().array().stream()
                .map(bucket -> new RangeFacetBucket(bucket.key(), bucket.from(), bucket.to(), productCount(bucket)))
                .toList();
    }

    private static Aggregate descendant(Aggregate root, String... path) {
        Aggregate current = root;
        for (String name : path) {
            Map<String, Aggregate> children = children(current);
            current = children.get(name);
            if (current == null) return null;
        }
        return current;
    }

    private static Map<String, Aggregate> children(Aggregate aggregate) {
        if (aggregate == null) return Map.of();
        if (aggregate.isFilter()) return aggregate.filter().aggregations();
        if (aggregate.isNested()) return aggregate.nested().aggregations();
        return Map.of();
    }

    private static long productCount(StringTermsBucket bucket) {
        Aggregate products = bucket.aggregations().get("products");
        return products != null && products.isReverseNested() ? products.reverseNested().docCount() : bucket.docCount();
    }

    private static long productCount(RangeBucket bucket) {
        Aggregate products = bucket.aggregations().get("products");
        return products != null && products.isReverseNested() ? products.reverseNested().docCount() : bucket.docCount();
    }

    private StorefrontProductSearchItem toStorefrontItem(Map<?, ?> source, Map<String, List<String>> highlights) {
        List<Map<?, ?>> activeVariants = maps(source.get("variants")).stream()
                .filter(variant -> "ACTIVE".equals(string(variant.get("status"))))
                .toList();
        List<BigDecimal> prices = activeVariants.stream()
                .map(variant -> decimal(variant.get("salePrice")))
                .filter(value -> value != null)
                .toList();
        BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(null);
        long totalQuantity = activeVariants.stream().mapToLong(variant -> longValue(variant.get("quantity"))).sum();
        CategorySummary category = new CategorySummary(
                string(source.get("categoryId")), string(source.get("categoryName")), string(source.get("categorySlug")));
        return new StorefrontProductSearchItem(
                string(source.get("id")), string(source.get("sellerId")), string(source.get("name")),
                string(source.get("status")), category, minPrice, maxPrice, totalQuantity, activeVariants.size(),
                string(source.get("imageUrl")), decimal(source.get("ratingAverage")),
                longValue(source.get("ratingCount")), safeHighlights(highlights));
    }

    private static Map<String, List<String>> safeHighlights(Map<String, List<String>> highlights) {
        if (highlights == null || highlights.isEmpty()) return Map.of();
        return new LinkedHashMap<>(highlights);
    }

    private static List<Map<?, ?>> maps(Object value) {
        if (!(value instanceof List<?> values)) return List.of();
        List<Map<?, ?>> result = new ArrayList<>();
        for (Object item : values) if (item instanceof Map<?, ?> map) result.add(map);
        return result;
    }

    private static String string(Object value) { return value == null ? null : String.valueOf(value); }

    private static BigDecimal decimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return new BigDecimal(number.toString());
        try { return new BigDecimal(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static long longValue(Object value) {
        if (value instanceof Number number) return number.longValue();
        try { return value == null ? 0L : Long.parseLong(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return 0L; }
    }
}
