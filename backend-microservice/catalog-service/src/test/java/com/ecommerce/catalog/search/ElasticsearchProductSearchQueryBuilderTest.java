package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch._types.SortMode;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.ecommerce.catalog.model.request.ElasticsearchProductSearchRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticsearchProductSearchQueryBuilderTest {
    private static final String TEST_SIGNING_KEY = "test-search-cursor-signing-key-000000000000";
    private final ElasticsearchProductSearchQueryBuilder builder =
            new ElasticsearchProductSearchQueryBuilder("products");
    private final SearchCursorCodec cursorCodec = new SearchCursorCodec(TEST_SIGNING_KEY);

    @Test
    void keywordSearchUsesFullTextFieldsAndActiveFilter() {
        ElasticsearchProductSearchRequest request = request();
        request.setQ("running shoe");

        SearchRequest search = builder.build(request);

        assertEquals("products", search.index().get(0));
        Query textQuery = search.query().bool().must().get(0);
        assertEquals("running shoe", textQuery.bool().should().get(0).matchPhrase().query());
        assertEquals("AUTO", textQuery.bool().should().get(1).multiMatch().fuzziness());
        assertEquals(List.of("name^4", "categoryName^2", "description"),
                textQuery.bool().should().get(1).multiMatch().fields());
        assertTrue(hasTerm(search.query().bool().filter(), "status", "ACTIVE"));
        assertEquals(SortOrder.Desc, search.sort().get(0).score().order());
        assertEquals("id", search.sort().get(1).field().field());
        assertTrue(search.highlight().fields().containsKey("name"));
        assertTrue(search.aggregations().containsKey("categories"));
        assertTrue(search.aggregations().containsKey("colors"));
    }

    @Test
    void priceRangeUsesOneNestedActiveVariantQuery() {
        ElasticsearchProductSearchRequest request = request();
        request.setMinPrice(new BigDecimal("500000"));
        request.setMaxPrice(new BigDecimal("2000000"));

        SearchRequest search = builder.build(request);
        Query nested = search.query().bool().filter().stream().filter(Query::isNested).findFirst().orElseThrow();
        List<Query> filters = nested.nested().query().bool().filter();

        assertEquals("variants", nested.nested().path());
        assertTrue(hasTerm(filters, "variants.status", "ACTIVE"));
        Query range = filters.stream().filter(Query::isRange).findFirst().orElseThrow();
        assertEquals("variants.salePrice", range.range().untyped().field());
        assertEquals(500000D, range.range().untyped().gte().to(Double.class));
        assertEquals(2000000D, range.range().untyped().lte().to(Double.class));
    }

    @Test
    void categoryAndSellerUseExactFilterContext() {
        ElasticsearchProductSearchRequest request = request();
        request.setCategoryId("category-1");
        request.setSellerId("seller-1");

        List<Query> filters = builder.build(request).query().bool().filter();

        assertTrue(hasTerm(filters, "categoryId", "category-1"));
        assertTrue(hasTerm(filters, "sellerId", "seller-1"));
    }

    @Test
    void priceSortsUseNestedMinimumActiveVariantPriceAndStableTieBreaker() {
        ElasticsearchProductSearchRequest ascending = request();
        ascending.setSort("price_asc");
        ElasticsearchProductSearchRequest descending = request();
        descending.setSort("price_desc");

        SearchRequest ascSearch = builder.build(ascending);
        SearchRequest descSearch = builder.build(descending);

        assertPriceSort(ascSearch, SortOrder.Asc);
        assertPriceSort(descSearch, SortOrder.Desc);
    }

    @Test
    void rejectsInvalidPageSizePriceSortAndDeepResultWindow() {
        ElasticsearchProductSearchRequest negativePage = request();
        negativePage.setPage(-1);
        assertError("SEARCH_PAGE_MUST_BE_NON_NEGATIVE", negativePage);

        ElasticsearchProductSearchRequest zeroSize = request();
        zeroSize.setSize(0);
        assertError("SEARCH_SIZE_MUST_BE_BETWEEN_1_AND_100", zeroSize);

        ElasticsearchProductSearchRequest largeSize = request();
        largeSize.setSize(101);
        assertError("SEARCH_SIZE_MUST_BE_BETWEEN_1_AND_100", largeSize);

        ElasticsearchProductSearchRequest invalidRange = request();
        invalidRange.setMinPrice(BigDecimal.TEN);
        invalidRange.setMaxPrice(BigDecimal.ONE);
        assertError("SEARCH_MIN_PRICE_MUST_NOT_EXCEED_MAX_PRICE", invalidRange);

        ElasticsearchProductSearchRequest negativePrice = request();
        negativePrice.setMinPrice(BigDecimal.valueOf(-1));
        assertError("SEARCH_PRICE_MUST_BE_NON_NEGATIVE", negativePrice);

        ElasticsearchProductSearchRequest unsupportedSort = request();
        unsupportedSort.setSort("unknown");
        assertError("SEARCH_SORT_UNSUPPORTED", unsupportedSort);

        ElasticsearchProductSearchRequest deepPage = request();
        deepPage.setPage(500);
        assertError("SEARCH_RESULT_WINDOW_EXCEEDED_CURSOR_REQUIRED", deepPage);
    }

    @Test
    void cursorUsesSearchAfterAndDoesNotUseFrom() {
        ElasticsearchProductSearchRequest first = request();
        String cursor = cursorCodec.encode("pit-123", "newest", "fingerprint", 900, 20, 20_000L, List.of(
                co.elastic.clients.elasticsearch._types.FieldValue.of(123L),
                co.elastic.clients.elasticsearch._types.FieldValue.of("product-1")), System.currentTimeMillis());
        first.setCursor(cursor);
        first.setPage(900);

        SearchCursorCodec.DecodedCursor decoded = cursorCodec.decode(cursor);
        SearchRequest search = builder.build(first, decoded.pitId(), decoded.values(), "2m");

        assertEquals(null, search.from());
        assertTrue(search.index().isEmpty());
        assertEquals("pit-123", search.pit().id());
        assertEquals("2m", search.pit().keepAlive().time());
        assertEquals(123L, search.searchAfter().get(0).longValue());
        assertEquals("product-1", search.searchAfter().get(1).stringValue());
        assertEquals(false, search.trackTotalHits().enabled());
        assertTrue(search.aggregations().isEmpty());
        assertTrue(search.source().filter().includes().contains("variants.salePrice"));
    }

    @Test
    void autocompleteUsesDedicatedSearchAsYouTypeFields() {
        SearchRequest search = builder.buildAutocomplete("dien thoai", 8);

        Query query = search.query().bool().must().get(0);
        assertEquals("bool_prefix", query.multiMatch().type().jsonValue());
        assertTrue(query.multiMatch().fields().contains("name.autocomplete._2gram"));
        assertTrue(hasTerm(search.query().bool().filter(), "status", "ACTIVE"));
    }

    @Test
    void facetsAreSingleSelectAndKeepNestedVariantBoundaries() {
        ElasticsearchProductSearchRequest request = request();
        request.setCategoryId("category-1");
        request.setSellerId("seller-1");
        request.setColor("trang");
        request.setSizeValue("40");

        SearchRequest search = builder.build(request);
        Aggregation categories = search.aggregations().get("categories");
        assertTrue(hasTerm(categories.filter().bool().filter(), "sellerId", "seller-1"));
        assertTrue(categories.filter().bool().filter().stream()
                .noneMatch(query -> query.isTerm() && "categoryId".equals(query.term().field())));

        Aggregation sizes = search.aggregations().get("sizes");
        Aggregation variants = sizes.aggregations().get("variants");
        Aggregation active = variants.aggregations().get("active");
        Aggregation selections = active.aggregations().get("selections");
        Aggregation axis = selections.aggregations().get("axis");
        assertEquals("variants", variants.nested().path());
        assertEquals("variants.selections", selections.nested().path());
        assertEquals(3, axis.filter().bool().should().size());
        assertTrue(axis.aggregations().get("values").aggregations().get("products").isReverseNested());
    }

    private static ElasticsearchProductSearchRequest request() {
        return new ElasticsearchProductSearchRequest();
    }

    private static boolean hasTerm(List<Query> queries, String field, String value) {
        return queries.stream().filter(Query::isTerm)
                .anyMatch(query -> field.equals(query.term().field())
                        && value.equals(query.term().value().stringValue()));
    }

    private static void assertPriceSort(SearchRequest search, SortOrder order) {
        assertEquals("variants.salePrice", search.sort().get(0).field().field());
        assertEquals(order, search.sort().get(0).field().order());
        assertEquals(SortMode.Min, search.sort().get(0).field().mode());
        assertEquals("variants", search.sort().get(0).field().nested().path());
        assertEquals("variants.status", search.sort().get(0).field().nested().filter().term().field());
        assertEquals("ACTIVE", search.sort().get(0).field().nested().filter().term().value().stringValue());
        assertEquals("id", search.sort().get(1).field().field());
        assertEquals(SortOrder.Asc, search.sort().get(1).field().order());
    }

    private void assertError(String message, ElasticsearchProductSearchRequest request) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.build(request));
        assertEquals(message, exception.getMessage());
    }
}
