package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchCursorCodecTest {
    private static final String TEST_SIGNING_KEY = "test-search-cursor-signing-key-000000000000";
    private final SearchCursorCodec codec = new SearchCursorCodec(TEST_SIGNING_KEY);

    @Test
    void roundTripPreservesPitSortValuesAndIssueTime() {
        long issuedAt = 1_700_000_000_000L;
        String encoded = codec.encode("pit-id-123", "price_asc", "fingerprint", 3, 20, 45L, List.of(
                FieldValue.of(1250000.0), FieldValue.of("product-42")), issuedAt);

        SearchCursorCodec.DecodedCursor decoded = codec.decode(encoded, "price_asc", "fingerprint", 3, 20);

        assertEquals("pit-id-123", decoded.pitId());
        assertEquals("price_asc", decoded.sort());
        assertEquals(1250000.0, decoded.values().get(0).asDouble());
        assertEquals("product-42", decoded.values().get(1).asText());
        assertEquals(issuedAt, decoded.issuedAtEpochMillis());
        assertEquals(45L, decoded.totalHits());
    }

    @Test
    void rejectsCursorForDifferentSortOrWithoutPit() {
        String encoded = codec.encode("pit-id-123", "newest", "fingerprint", 1, 20, 2L,
                List.of(FieldValue.of(1L), FieldValue.of("product-1")), System.currentTimeMillis());

        assertThrows(IllegalArgumentException.class,
                () -> codec.decode(encoded, "price_desc", "fingerprint", 1, 20));
        assertThrows(IllegalArgumentException.class, () -> codec.decode("e30"));
    }

    @Test
    void rejectsTamperingAndCursorReuseWithDifferentQueryOrPage() {
        String encoded = codec.encode("pit-id-123", "newest", "query-a", 2, 20, 100L,
                List.of(FieldValue.of(1L), FieldValue.of("product-1")), System.currentTimeMillis());
        String tampered = (encoded.charAt(0) == 'A' ? 'B' : 'A') + encoded.substring(1);

        assertThrows(IllegalArgumentException.class, () -> codec.decode(tampered));
        assertThrows(IllegalArgumentException.class,
                () -> codec.decode(encoded, "newest", "query-b", 2, 20));
        assertThrows(IllegalArgumentException.class,
                () -> codec.decode(encoded, "newest", "query-a", 3, 20));
    }
}
