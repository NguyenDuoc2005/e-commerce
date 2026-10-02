package com.ecommerce.catalog.model.response;

import java.util.List;

public record ProductAutocompleteResponse(List<Suggestion> suggestions) {
    public record Suggestion(String id, String name, String categoryName) {}
}
