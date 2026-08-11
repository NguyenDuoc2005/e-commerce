package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.document.ProductDocument;
import com.ecommerce.catalog.service.ProductSearchService;
import com.ecommerce.common.base.ResponseObject;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final int DEFAULT_SEARCH_SIZE = 50;

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchServiceImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public ResponseObject<?> search(String keyword, String category, Double minPrice, Double maxPrice) {
        try {
            ensureIndex();
            Query query = buildQuery(keyword, category, minPrice, maxPrice);
            query.setPageable(PageRequest.of(0, DEFAULT_SEARCH_SIZE));
            SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
            List<ProductDocument> rows = hits.stream().map(SearchHit::getContent).toList();
            return new ResponseObject<>(rows, HttpStatus.OK, "Tim kiem san pham thanh cong");
        } catch (DataAccessResourceFailureException ex) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Elasticsearch chua san sang, vui long kiem tra service elasticsearch"
            );
        } catch (RuntimeException ex) {
            return new ResponseObject<>(
                    List.of(),
                    HttpStatus.OK,
                    "Chua co du lieu index Elasticsearch cho san pham"
            );
        }
    }

    private void ensureIndex() {
        var indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
        }
    }

    private Query buildQuery(String keyword, String category, Double minPrice, Double maxPrice) {
        if (StringUtils.hasText(keyword)
                && !StringUtils.hasText(category)
                && minPrice == null
                && maxPrice == null) {
            return NativeQuery.builder()
                    .withQuery(q -> q.queryString(queryString -> queryString
                            .query("*" + escapeQueryString(keyword) + "*")
                            .fields("name", "description", "brand", "category")
                            .analyzeWildcard(true)
                            .defaultOperator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)))
                    .build();
        }
        return new CriteriaQuery(buildCriteria(keyword, category, minPrice, maxPrice));
    }

    private Criteria buildCriteria(String keyword, String category, Double minPrice, Double maxPrice) {
        Criteria criteria = new Criteria();
        if (StringUtils.hasText(keyword)) {
            criteria = new Criteria("name").matches(keyword)
                    .or(new Criteria("description").matches(keyword))
                    .or(new Criteria("brand").matches(keyword));
        }
        if (StringUtils.hasText(category)) {
            Criteria categoryCriteria = new Criteria("categoryId").is(category)
                    .or(new Criteria("category").is(category));
            criteria = criteria.and(categoryCriteria);
        }
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = new Criteria("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.greaterThanEqual(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lessThanEqual(maxPrice);
            }
            criteria = criteria.and(priceCriteria);
        }
        return criteria;
    }

    private String escapeQueryString(String keyword) {
        return keyword.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("&&", "\\&&")
                .replace("||", "\\||")
                .replace(">", "\\>")
                .replace("<", "\\<")
                .replace("!", "\\!")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("^", "\\^")
                .replace("~", "\\~")
                .replace("?", "\\?")
                .replace(":", "\\:")
                .replace("/", "\\/");
    }
}
