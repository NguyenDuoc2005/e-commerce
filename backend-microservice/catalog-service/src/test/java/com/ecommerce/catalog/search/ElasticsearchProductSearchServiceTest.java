package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.ecommerce.catalog.controller.CatalogExceptionHandler;
import com.ecommerce.catalog.model.request.ElasticsearchProductSearchRequest;
import com.ecommerce.catalog.model.response.StorefrontSearchResponse;
import com.ecommerce.common.base.ResponseObject;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElasticsearchProductSearchServiceTest {
    private static final String TEST_SIGNING_KEY = "test-search-cursor-signing-key-000000000000";

    @Test
    void unavailableElasticsearchBecomesExplicit503InsteadOfMysqlFallback() throws Exception {
        ElasticsearchProductSearchClient client = mock(ElasticsearchProductSearchClient.class);
        when(client.openPointInTime(any(), any())).thenReturn("pit-test");
        when(client.search(any())).thenThrow(new IOException("connection refused"));
        ElasticsearchProductSearchService service = new ElasticsearchProductSearchService(
                new ElasticsearchProductSearchQueryBuilder("products"), client, codec(), new SimpleMeterRegistry(),
                "2m", 120000L);

        ElasticsearchSearchUnavailableException exception = assertThrows(
                ElasticsearchSearchUnavailableException.class,
                () -> service.search(new ElasticsearchProductSearchRequest()));

        assertEquals("PRODUCT_SEARCH_TEMPORARILY_UNAVAILABLE", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                new CatalogExceptionHandler().handleSearchUnavailable(exception).getStatusCode());
        verify(client).closePointInTime("pit-test");
    }

    @Test
    void opensPitOnFirstPageAndEncodesRefreshedPitInNextCursor() throws Exception {
        ElasticsearchProductSearchClient client = mock(ElasticsearchProductSearchClient.class);
        when(client.openPointInTime("products", "2m")).thenReturn("pit-opened");
        SearchResponse<Map> searchResponse = response("pit-refreshed", 2L);
        when(client.search(any())).thenReturn(searchResponse);
        ElasticsearchProductSearchService service = service(client, 120000L);
        ElasticsearchProductSearchRequest request = new ElasticsearchProductSearchRequest();
        request.setSize(1);

        StorefrontSearchResponse result = service.search(request);

        assertNotNull(result.nextCursor());
        assertEquals("pit-refreshed", codec().decode(result.nextCursor()).pitId());
        verify(client).openPointInTime("products", "2m");
        verify(client, never()).closePointInTime(any());
    }

    @Test
    void reusesPitFromCursorAndClosesItOnLastPage() throws Exception {
        ElasticsearchProductSearchClient client = mock(ElasticsearchProductSearchClient.class);
        SearchResponse<Map> searchResponse = response("pit-last", 2L);
        when(client.search(any())).thenReturn(searchResponse);
        when(client.closePointInTime("pit-last")).thenReturn(true);
        ElasticsearchProductSearchService service = service(client, 120000L);
        ElasticsearchProductSearchRequest request = new ElasticsearchProductSearchRequest();
        request.setPage(1);
        request.setSize(1);
        request.setCursor(cursor(request, "pit-current", 2L,
                List.of(FieldValue.of(0L), FieldValue.of("product-0")), System.currentTimeMillis()));

        StorefrontSearchResponse result = service.search(request);

        assertNull(result.nextCursor());
        verify(client, never()).openPointInTime(any(), any());
        verify(client).closePointInTime("pit-last");
    }

    @Test
    void expiredCursorIsClosedAndRejectedBeforeSearch() throws Exception {
        ElasticsearchProductSearchClient client = mock(ElasticsearchProductSearchClient.class);
        ElasticsearchProductSearchService service = service(client, 10L);
        ElasticsearchProductSearchRequest request = new ElasticsearchProductSearchRequest();
        request.setPage(1);
        request.setCursor(cursor(request, "pit-old", 2L,
                List.of(FieldValue.of(0L), FieldValue.of("product-0")), System.currentTimeMillis() - 1000L));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.search(request));

        assertEquals("SEARCH_CURSOR_EXPIRED", exception.getMessage());
        verify(client).closePointInTime("pit-old");
        verify(client, never()).search(any());
    }

    @Test
    void expiredPitFromElasticsearchBecomesExplicit400SearchCursorExpired() throws Exception {
        ElasticsearchProductSearchClient client = mock(ElasticsearchProductSearchClient.class);
        ElasticsearchException pitMissing = mock(ElasticsearchException.class);
        when(pitMissing.status()).thenReturn(404);
        when(client.search(any())).thenThrow(pitMissing);
        ElasticsearchProductSearchService service = service(client, 120000L);
        ElasticsearchProductSearchRequest request = new ElasticsearchProductSearchRequest();
        request.setPage(2);
        request.setCursor(cursor(request, "pit-expired", 3L,
                List.of(FieldValue.of(0L), FieldValue.of("product-1")), System.currentTimeMillis()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.search(request));
        ResponseEntity<ResponseObject<?>> response = new CatalogExceptionHandler().handleValidation(exception);

        assertEquals("SEARCH_CURSOR_EXPIRED", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SEARCH_CURSOR_EXPIRED", response.getBody().getMessage());
        verify(client, never()).openPointInTime(any(), any());
    }

    private static ElasticsearchProductSearchService service(ElasticsearchProductSearchClient client, long maxAge) {
        return new ElasticsearchProductSearchService(
                new ElasticsearchProductSearchQueryBuilder("products"), client, codec(),
                new SimpleMeterRegistry(), "2m", maxAge);
    }

    private static SearchCursorCodec codec() {
        return new SearchCursorCodec(TEST_SIGNING_KEY);
    }

    private static String cursor(ElasticsearchProductSearchRequest request, String pitId, long total,
                                 List<FieldValue> sortValues, long issuedAt) {
        SearchCursorCodec codec = codec();
        String sort = "newest";
        return codec.encode(pitId, sort, codec.fingerprint(request, sort), request.getPage(), request.getSize(),
                total, sortValues, issuedAt);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SearchResponse<Map> response(String pitId, long totalValue) {
        SearchResponse<Map> response = mock(SearchResponse.class);
        HitsMetadata<Map> hits = mock(HitsMetadata.class);
        Hit<Map> hit = mock(Hit.class);
        TotalHits total = mock(TotalHits.class);
        when(response.hits()).thenReturn(hits);
        when(response.pitId()).thenReturn(pitId);
        when(response.aggregations()).thenReturn(Map.of());
        when(hits.hits()).thenReturn(List.of(hit));
        when(hits.total()).thenReturn(total);
        when(total.value()).thenReturn(totalValue);
        when(hit.source()).thenReturn(Map.of("id", "product-1", "status", "ACTIVE", "variants", List.of()));
        when(hit.highlight()).thenReturn(Map.of());
        when(hit.sort()).thenReturn(List.of(FieldValue.of(0L), FieldValue.of("product-1")));
        return response;
    }
}
