package com.ecommerce.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SearchMappingContractTest {
    @Test
    void searchMappingUsesStrictNestedAttributesVariantsAndSelections() throws Exception {
        Path mappingPath = Path.of("..", "search-pipeline", "elasticsearch", "products-v3-index-mapping.json");
        JsonNode mappings = new ObjectMapper().readTree(Files.readString(mappingPath)).get("mappings");
        assertEquals("strict", mappings.get("dynamic").asText());
        assertEquals(3, mappings.at("/_meta/schema_version").asInt());
        assertEquals("vi_search", mappings.at("/properties/name/analyzer").asText());
        assertEquals("search_as_you_type", mappings.at("/properties/name/fields/autocomplete/type").asText());
        assertEquals("nested", mappings.at("/properties/attributes/type").asText());
        assertEquals("nested", mappings.at("/properties/variants/type").asText());
        assertEquals("nested", mappings.at("/properties/variants/properties/selections/type").asText());
        assertEquals("long", mappings.at("/properties/createdAt/type").asText());
        assertFalse(mappings.get("properties").has("brand"));
        assertFalse(mappings.get("properties").has("brandId"));
    }
}
