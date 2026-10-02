package com.ecommerce.catalog.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ElasticsearchProductSearchClient {
    private final ElasticsearchClient client;

    public ElasticsearchProductSearchClient(ElasticsearchClient client) {
        this.client = client;
    }

    @SuppressWarnings("rawtypes")
    public SearchResponse<Map> search(SearchRequest request) throws IOException {
        return client.search(request, Map.class);
    }

    public String openPointInTime(String index, String keepAlive) throws IOException {
        return client.openPointInTime(request -> request
                .index(index)
                .keepAlive(time -> time.time(keepAlive))).id();
    }

    public boolean closePointInTime(String pitId) throws IOException {
        return client.closePointInTime(request -> request.id(pitId)).succeeded();
    }
}
