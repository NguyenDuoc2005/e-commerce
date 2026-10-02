package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.ecommerce.catalog.model.request.ElasticsearchProductSearchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Component
final class SearchCursorCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
    private static final int CURSOR_VERSION = 2;
    private static final int MAX_CURSOR_LENGTH = 8_192;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec signingKey;

    SearchCursorCodec(@Value("${catalog.search.elasticsearch.cursor.signing-key}") String signingKey) {
        if (signingKey == null || signingKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CATALOG_SEARCH_CURSOR_SIGNING_KEY_MUST_BE_AT_LEAST_32_BYTES");
        }
        this.signingKey = new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    String encode(String pitId, String sort, String fingerprint, int nextPage, int pageSize, long totalHits,
                  List<FieldValue> values, long issuedAtEpochMillis) {
        if (pitId == null || pitId.isBlank() || values == null || values.isEmpty()) return null;
        ObjectNode payload = MAPPER.createObjectNode()
                .put("version", CURSOR_VERSION)
                .put("pitId", pitId)
                .put("sort", sort)
                .put("fingerprint", fingerprint)
                .put("nextPage", nextPage)
                .put("pageSize", pageSize)
                .put("totalHits", totalHits)
                .put("issuedAt", issuedAtEpochMillis);
        ArrayNode array = payload.putArray("values");
        for (FieldValue value : values) {
            if (value.isString()) array.add(value.stringValue());
            else if (value.isLong()) array.add(value.longValue());
            else if (value.isDouble()) array.add(value.doubleValue());
            else if (value.isBoolean()) array.add(value.booleanValue());
            else if (value.isNull()) array.addNull();
            else array.add(value.toString());
        }
        try {
            String encodedPayload = BASE64_ENCODER.encodeToString(MAPPER.writeValueAsBytes(payload));
            return encodedPayload + "." + BASE64_ENCODER.encodeToString(sign(encodedPayload));
        } catch (Exception exception) {
            throw new IllegalStateException("SEARCH_CURSOR_ENCODING_FAILED", exception);
        }
    }

    DecodedCursor decode(String cursor, String expectedSort, String expectedFingerprint,
                         int expectedPage, int expectedPageSize) {
        DecodedCursor decoded = decode(cursor);
        if (!expectedSort.equals(decoded.sort())
                || !expectedFingerprint.equals(decoded.fingerprint())
                || expectedPage != decoded.nextPage()
                || expectedPageSize != decoded.pageSize()) {
            throw invalidCursor();
        }
        return decoded;
    }

    DecodedCursor decode(String cursor) {
        try {
            if (cursor == null || cursor.isBlank() || cursor.length() > MAX_CURSOR_LENGTH) throw invalidCursor();
            String[] parts = cursor.split("\\.", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) throw invalidCursor();
            byte[] suppliedSignature = BASE64_DECODER.decode(parts[1]);
            if (!MessageDigest.isEqual(sign(parts[0]), suppliedSignature)) throw invalidCursor();

            JsonNode payload = MAPPER.readTree(BASE64_DECODER.decode(parts[0]));
            String pitId = payload.path("pitId").asText();
            String sort = payload.path("sort").asText();
            String fingerprint = payload.path("fingerprint").asText();
            int nextPage = payload.path("nextPage").asInt(-1);
            int pageSize = payload.path("pageSize").asInt(0);
            long totalHits = payload.path("totalHits").asLong(-1L);
            long issuedAt = payload.path("issuedAt").asLong(0L);
            if (payload.path("version").asInt() != CURSOR_VERSION || pitId.isBlank() || sort.isBlank()
                    || fingerprint.isBlank() || nextPage < 1 || pageSize < 1 || totalHits < 0L || issuedAt <= 0L
                    || !payload.path("values").isArray() || payload.path("values").isEmpty()) {
                throw invalidCursor();
            }
            return new DecodedCursor(pitId, sort, fingerprint, nextPage, pageSize, totalHits,
                    (ArrayNode) payload.path("values"), issuedAt);
        } catch (IllegalArgumentException exception) {
            throw invalidCursor();
        } catch (Exception exception) {
            throw new IllegalArgumentException("SEARCH_CURSOR_INVALID", exception);
        }
    }

    String fingerprint(ElasticsearchProductSearchRequest request, String effectiveSort) {
        ObjectNode criteria = MAPPER.createObjectNode()
                .put("schema", 1)
                .put("q", normalized(request.getQ()))
                .put("categoryId", normalized(request.getCategoryId()))
                .put("sellerId", normalized(request.getSellerId()))
                .put("color", normalized(request.getColor()))
                .put("sizeValue", normalized(request.getSizeValue()))
                .put("minPrice", normalized(request.getMinPrice()))
                .put("maxPrice", normalized(request.getMaxPrice()))
                .put("sort", effectiveSort);
        try {
            return BASE64_ENCODER.encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(MAPPER.writeValueAsBytes(criteria)));
        } catch (Exception exception) {
            throw new IllegalStateException("SEARCH_CURSOR_FINGERPRINT_FAILED", exception);
        }
    }

    private byte[] sign(String encodedPayload) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(encodedPayload.getBytes(StandardCharsets.US_ASCII));
    }

    private static IllegalArgumentException invalidCursor() {
        return new IllegalArgumentException("SEARCH_CURSOR_INVALID");
    }

    private static String normalized(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private static String normalized(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    record DecodedCursor(String pitId, String sort, String fingerprint, int nextPage, int pageSize, long totalHits,
                         ArrayNode values, long issuedAtEpochMillis) {}
}
