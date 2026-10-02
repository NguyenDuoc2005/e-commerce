package com.ecommerce.catalog.model.response;

import java.util.List;

public record StorefrontSearchResponse(
        List<StorefrontProductSearchItem> items,
        int page,
        int size,
        long totalElements,
        long totalPages,
        String nextCursor,
        SearchFacets facets
) {
    public record SearchFacets(
            List<FacetBucket> categories,
            List<FacetBucket> shops,
            List<RangeFacetBucket> priceRanges,
            List<FacetBucket> colors,
            List<FacetBucket> sizes
    ) {
        public static SearchFacets empty() {
            return new SearchFacets(List.of(), List.of(), List.of(), List.of(), List.of());
        }
    }

    public record FacetBucket(String value, long count) {}
    public record RangeFacetBucket(String key, Double from, Double to, long count) {}
}
