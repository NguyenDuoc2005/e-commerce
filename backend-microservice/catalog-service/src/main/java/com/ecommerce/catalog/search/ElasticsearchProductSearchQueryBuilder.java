package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.ecommerce.catalog.model.request.ElasticsearchProductSearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Component
public class ElasticsearchProductSearchQueryBuilder {
    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_AUTOCOMPLETE_SIZE = 20;
    static final long MAX_RESULT_WINDOW = 10_000L;
    static final int MAX_KEYWORD_LENGTH = 200;
    static final int MAX_FILTER_LENGTH = 128;
    private static final Set<String> SUPPORTED_SORTS = Set.of(
            "relevance", "newest", "price_asc", "price_desc", "rating_desc");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String indexAlias;

    public ElasticsearchProductSearchQueryBuilder(
            @Value("${catalog.search.elasticsearch.index:products}") String indexAlias) {
        this.indexAlias = indexAlias;
    }

    public SearchRequest build(ElasticsearchProductSearchRequest request) {
        return build(request, null, null, null);
    }

    SearchRequest build(ElasticsearchProductSearchRequest request, String pitId,
                        ArrayNode searchAfter, String pitKeepAlive) {
        validate(request);
        String keyword = trimToNull(request.getQ());
        String effectiveSort = effectiveSort(request.getSort(), keyword);
        ObjectNode body = MAPPER.createObjectNode();
        body.set("query", query(request, ExcludedFacet.NONE));
        body.put("size", request.getSize());
        body.put("track_total_hits", searchAfter == null);
        ObjectNode source = body.putObject("_source");
        source.putArray("includes")
                .add("id").add("sellerId").add("name").add("status")
                .add("categoryId").add("categoryName").add("categorySlug")
                .add("imageUrl").add("ratingAverage").add("ratingCount")
                .add("variants.salePrice").add("variants.quantity").add("variants.status");

        if (searchAfter == null) body.put("from", Math.multiplyExact(request.getPage(), request.getSize()));
        else body.set("search_after", searchAfter);
        if (pitId != null) {
            ObjectNode pit = body.putObject("pit").put("id", pitId);
            if (pitKeepAlive != null) pit.put("keep_alive", pitKeepAlive);
        }
        body.set("sort", sorts(effectiveSort));

        if (keyword != null) body.set("highlight", highlight());
        if (searchAfter == null) body.set("aggs", aggregations(request));
        return fromJson(body, pitId == null);
    }

    public SearchRequest buildAutocomplete(String keyword, int size) {
        String normalized = trimToNull(keyword);
        if (normalized == null || normalized.length() < 2) {
            throw new IllegalArgumentException("AUTOCOMPLETE_QUERY_MUST_HAVE_AT_LEAST_2_CHARACTERS");
        }
        if (size < 1 || size > MAX_AUTOCOMPLETE_SIZE) {
            throw new IllegalArgumentException("AUTOCOMPLETE_SIZE_MUST_BE_BETWEEN_1_AND_20");
        }
        ObjectNode body = MAPPER.createObjectNode();
        body.put("size", size);
        body.putObject("_source").putArray("includes").add("id").add("name").add("categoryName");
        ObjectNode bool = body.putObject("query").putObject("bool");
        bool.putArray("filter").add(term("status", "ACTIVE"));
        ObjectNode multiMatch = bool.putArray("must").addObject().putObject("multi_match");
        multiMatch.put("query", normalized);
        multiMatch.put("type", "bool_prefix");
        multiMatch.putArray("fields")
                .add("name.autocomplete")
                .add("name.autocomplete._2gram")
                .add("name.autocomplete._3gram")
                .add("categoryName.autocomplete");
        ArrayNode sort = body.putArray("sort");
        sort.addObject().putObject("_score").put("order", "desc");
        sort.addObject().put("id", "asc");
        return fromJson(body, true);
    }

    String effectiveSort(ElasticsearchProductSearchRequest request) {
        return effectiveSort(request.getSort(), trimToNull(request.getQ()));
    }

    void validateRequest(ElasticsearchProductSearchRequest request) { validate(request); }

    String indexAlias() { return indexAlias; }

    private SearchRequest fromJson(ObjectNode body, boolean includeIndex) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        if (includeIndex) builder.index(indexAlias);
        return builder.withJson(new ByteArrayInputStream(
                body.toString().getBytes(StandardCharsets.UTF_8))).build();
    }

    private static ObjectNode query(ElasticsearchProductSearchRequest request, ExcludedFacet excluded) {
        ObjectNode bool = MAPPER.createObjectNode();
        ArrayNode filter = bool.putArray("filter");
        filter.add(term("status", "ACTIVE"));

        String keyword = trimToNull(request.getQ());
        if (keyword != null) {
            ObjectNode textBool = bool.putArray("must").addObject().putObject("bool");
            ArrayNode should = textBool.putArray("should");
            should.addObject().putObject("match_phrase").putObject("name")
                    .put("query", keyword).put("boost", 5.0);
            ObjectNode fuzzy = should.addObject().putObject("multi_match");
            fuzzy.put("query", keyword).put("type", "best_fields").put("fuzziness", "AUTO")
                    .put("prefix_length", 2).put("max_expansions", 25);
            fuzzy.putArray("fields").add("name^4").add("categoryName^2").add("description");
            textBool.put("minimum_should_match", 1);
        }

        String categoryId = trimToNull(request.getCategoryId());
        if (excluded != ExcludedFacet.CATEGORY && categoryId != null) filter.add(term("categoryId", categoryId));
        String sellerId = trimToNull(request.getSellerId());
        if (excluded != ExcludedFacet.SHOP && sellerId != null) filter.add(term("sellerId", sellerId));
        if (excluded != ExcludedFacet.PRICE && (request.getMinPrice() != null || request.getMaxPrice() != null)) {
            filter.add(activeVariantPriceRange(request.getMinPrice(), request.getMaxPrice()));
        }
        if (excluded != ExcludedFacet.COLOR && trimToNull(request.getColor()) != null) {
            filter.add(activeVariantSelection("color", request.getColor()));
        }
        if (excluded != ExcludedFacet.SIZE && trimToNull(request.getSizeValue()) != null) {
            filter.add(activeVariantSelection("size", request.getSizeValue()));
        }
        return MAPPER.createObjectNode().set("bool", bool);
    }

    private static ObjectNode activeVariantPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        ObjectNode nested = MAPPER.createObjectNode().put("path", "variants");
        ObjectNode innerBool = nested.putObject("query").putObject("bool");
        ArrayNode filters = innerBool.putArray("filter");
        filters.add(term("variants.status", "ACTIVE"));
        ObjectNode range = filters.addObject().putObject("range").putObject("variants.salePrice");
        if (minPrice != null) range.put("gte", minPrice);
        if (maxPrice != null) range.put("lte", maxPrice);
        return MAPPER.createObjectNode().set("nested", nested);
    }

    private static ObjectNode activeVariantSelection(String axis, String value) {
        ObjectNode variants = MAPPER.createObjectNode().put("path", "variants");
        ArrayNode variantFilters = variants.putObject("query").putObject("bool").putArray("filter");
        variantFilters.add(term("variants.status", "ACTIVE"));
        ObjectNode selections = variantFilters.addObject().putObject("nested");
        selections.put("path", "variants.selections");
        ObjectNode selectionBool = selections.putObject("query").putObject("bool");
        ArrayNode filters = selectionBool.putArray("filter");
        filters.add(axisQuery(axis));
        filters.add(match("variants.selections.value", value));
        return MAPPER.createObjectNode().set("nested", variants);
    }

    private static ObjectNode axisQuery(String axis) {
        ArrayNode aliases = MAPPER.createArrayNode();
        if ("color".equals(axis)) {
            aliases.add(match("variants.selections.axisName", "màu sắc"));
            aliases.add(match("variants.selections.axisName", "màu"));
            aliases.add(match("variants.selections.axisName", "color"));
        } else {
            aliases.add(match("variants.selections.axisName", "kích thước"));
            aliases.add(match("variants.selections.axisName", "kích cỡ"));
            aliases.add(match("variants.selections.axisName", "size"));
        }
        ObjectNode bool = MAPPER.createObjectNode();
        bool.set("should", aliases);
        bool.put("minimum_should_match", 1);
        return MAPPER.createObjectNode().set("bool", bool);
    }

    private static ObjectNode aggregations(ElasticsearchProductSearchRequest request) {
        ObjectNode aggs = MAPPER.createObjectNode();
        aggs.set("categories", filteredTerms(request, ExcludedFacet.CATEGORY, "categoryId", 50));
        aggs.set("shops", filteredTerms(request, ExcludedFacet.SHOP, "sellerId", 50));
        aggs.set("price_ranges", priceFacet(request));
        aggs.set("colors", selectionFacet(request, ExcludedFacet.COLOR, "color"));
        aggs.set("sizes", selectionFacet(request, ExcludedFacet.SIZE, "size"));
        return aggs;
    }

    private static ObjectNode filteredTerms(ElasticsearchProductSearchRequest request, ExcludedFacet excluded,
                                            String field, int size) {
        ObjectNode aggregation = MAPPER.createObjectNode();
        aggregation.set("filter", query(request, excluded));
        aggregation.putObject("aggs").putObject("values").putObject("terms")
                .put("field", field).put("size", size);
        return aggregation;
    }

    private static ObjectNode priceFacet(ElasticsearchProductSearchRequest request) {
        ObjectNode root = MAPPER.createObjectNode();
        root.set("filter", query(request, ExcludedFacet.PRICE));
        ObjectNode variants = root.putObject("aggs").putObject("variants");
        variants.putObject("nested").put("path", "variants");
        ObjectNode active = variants.putObject("aggs").putObject("active");
        active.set("filter", term("variants.status", "ACTIVE"));
        ObjectNode values = active.putObject("aggs").putObject("values");
        ObjectNode ranges = values.putObject("range");
        ranges.put("field", "variants.salePrice");
        ArrayNode definitions = ranges.putArray("ranges");
        definitions.addObject().put("key", "under_500k").put("to", 500_000);
        definitions.addObject().put("key", "500k_1m").put("from", 500_000).put("to", 1_000_000);
        definitions.addObject().put("key", "1m_5m").put("from", 1_000_000).put("to", 5_000_000);
        definitions.addObject().put("key", "over_5m").put("from", 5_000_000);
        values.putObject("aggs").putObject("products").putObject("reverse_nested");
        return root;
    }

    private static ObjectNode selectionFacet(ElasticsearchProductSearchRequest request, ExcludedFacet excluded,
                                             String axis) {
        ObjectNode root = MAPPER.createObjectNode();
        root.set("filter", query(request, excluded));
        ObjectNode variants = root.putObject("aggs").putObject("variants");
        variants.putObject("nested").put("path", "variants");
        ObjectNode active = variants.putObject("aggs").putObject("active");
        active.set("filter", term("variants.status", "ACTIVE"));
        ObjectNode selections = active.putObject("aggs").putObject("selections");
        selections.putObject("nested").put("path", "variants.selections");
        ObjectNode matchingAxis = selections.putObject("aggs").putObject("axis");
        matchingAxis.set("filter", axisQuery(axis));
        ObjectNode values = matchingAxis.putObject("aggs").putObject("values");
        values.putObject("terms").put("field", "variants.selections.value").put("size", 30);
        values.putObject("aggs").putObject("products").putObject("reverse_nested");
        return root;
    }

    private static ObjectNode highlight() {
        ObjectNode highlight = MAPPER.createObjectNode();
        highlight.putArray("pre_tags").add("<mark>");
        highlight.putArray("post_tags").add("</mark>");
        highlight.put("fragment_size", 160).put("number_of_fragments", 2);
        ObjectNode fields = highlight.putObject("fields");
        fields.putObject("name").put("number_of_fragments", 0);
        fields.putObject("categoryName").put("number_of_fragments", 0);
        fields.putObject("description");
        return highlight;
    }

    private static ArrayNode sorts(String sort) {
        ArrayNode sorts = MAPPER.createArrayNode();
        switch (sort) {
            case "relevance" -> sorts.addObject().putObject("_score").put("order", "desc");
            case "price_asc" -> sorts.add(variantPriceSort("asc"));
            case "price_desc" -> sorts.add(variantPriceSort("desc"));
            case "rating_desc" -> sorts.addObject().putObject("ratingAverage")
                    .put("order", "desc").put("missing", "_last");
            default -> sorts.addObject().putObject("createdAt")
                    .put("order", "desc").put("missing", "_last");
        }
        sorts.addObject().put("id", "asc");
        return sorts;
    }

    private static ObjectNode variantPriceSort(String order) {
        ObjectNode config = MAPPER.createObjectNode().put("order", order).put("mode", "min").put("missing", "_last");
        ObjectNode nested = config.putObject("nested").put("path", "variants");
        nested.set("filter", term("variants.status", "ACTIVE"));
        return MAPPER.createObjectNode().set("variants.salePrice", config);
    }

    private static ObjectNode term(String field, String value) {
        ObjectNode query = MAPPER.createObjectNode();
        query.putObject("term").put(field, value);
        return query;
    }

    private static ObjectNode match(String field, String value) {
        ObjectNode query = MAPPER.createObjectNode();
        query.putObject("match").put(field, value);
        return query;
    }

    private static String effectiveSort(String requestedSort, String keyword) {
        String normalized = trimToNull(requestedSort);
        if (normalized == null) return keyword == null ? "newest" : "relevance";
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static void validate(ElasticsearchProductSearchRequest request) {
        if (request == null) throw new IllegalArgumentException("SEARCH_REQUEST_REQUIRED");
        if (request.getPage() < 0) throw new IllegalArgumentException("SEARCH_PAGE_MUST_BE_NON_NEGATIVE");
        if (request.getSize() < 1 || request.getSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("SEARCH_SIZE_MUST_BE_BETWEEN_1_AND_100");
        }
        long resultEnd = (long) request.getPage() * request.getSize() + request.getSize();
        if (trimToNull(request.getCursor()) == null && resultEnd > MAX_RESULT_WINDOW) {
            throw new IllegalArgumentException("SEARCH_RESULT_WINDOW_EXCEEDED_CURSOR_REQUIRED");
        }
        if (negative(request.getMinPrice()) || negative(request.getMaxPrice())) {
            throw new IllegalArgumentException("SEARCH_PRICE_MUST_BE_NON_NEGATIVE");
        }
        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
            throw new IllegalArgumentException("SEARCH_MIN_PRICE_MUST_NOT_EXCEED_MAX_PRICE");
        }
        String sort = trimToNull(request.getSort());
        if (sort != null && !SUPPORTED_SORTS.contains(sort.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("SEARCH_SORT_UNSUPPORTED");
        }
        validateLength(request.getQ(), MAX_KEYWORD_LENGTH, "SEARCH_QUERY_TOO_LONG");
        validateLength(request.getCategoryId(), MAX_FILTER_LENGTH, "SEARCH_CATEGORY_ID_TOO_LONG");
        validateLength(request.getSellerId(), MAX_FILTER_LENGTH, "SEARCH_SELLER_ID_TOO_LONG");
        validateLength(request.getColor(), MAX_FILTER_LENGTH, "SEARCH_COLOR_TOO_LONG");
        validateLength(request.getSizeValue(), MAX_FILTER_LENGTH, "SEARCH_SIZE_VALUE_TOO_LONG");
        if (request.getCursor() != null && request.getCursor().length() > 8_192) {
            throw new IllegalArgumentException("SEARCH_CURSOR_INVALID");
        }
        if (trimToNull(request.getCursor()) != null && request.getPage() == 0) {
            throw new IllegalArgumentException("SEARCH_CURSOR_PAGE_MISMATCH");
        }
    }

    private static boolean negative(BigDecimal value) { return value != null && value.signum() < 0; }
    private static void validateLength(String value, int maxLength, String message) {
        if (value != null && value.trim().length() > maxLength) throw new IllegalArgumentException(message);
    }
    private static String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private enum ExcludedFacet { NONE, CATEGORY, SHOP, PRICE, COLOR, SIZE }
}
