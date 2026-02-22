package org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.catalog.ProductStockStatus;
import org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomElasticsearchProductRepositoryImpl implements
    CustomElasticsearchProductRepository {

  private final ElasticsearchOperations elasticsearchOperations;

  public SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria,
      Pageable pageable) {
    // Build dynamic search query using NativeQuery with Elasticsearch Java client
    NativeQueryBuilder queryBuilder = NativeQuery.builder();

    // Build the main bool query
    BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();

    // Add strong preference for IN_STOCK products (boost their relevance score significantly)
    boolQueryBuilder.should(QueryBuilders.term(t -> t
        .field("stockStatus")
        .value(FieldValue.of(ProductStockStatus.IN_STOCK))
        .boost(5.0f)  // High boost to strongly prefer in-stock products
    ));

    // Add keyword search (fulltext search across multiple fields with boosting)
    if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
      String keyword = criteria.getKeyword().trim();

      // Create multi-match query with field boosting for relevance scoring
      // Product name gets highest boost (3.0) - most important for relevance
      // Brand name and category name get high boost (2.5) - brand is very important
      // Description gets medium boost (2.0)
      // Attributes (name and value) get lower boost (1.0) - less important for main relevance
      MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
          .query(keyword)
          .fields(
              "name^3.0",
              "brand.name^2.5",
              "categories.name^2.5",
              "details.description^2.0",
              "attributes.name^1.0",
              "attributes.value^1.0")
          .type(TextQueryType.BestFields)
          .fuzziness("AUTO")
      );

      boolQueryBuilder.must(multiMatchQuery._toQuery());
    }

    // Add price range filter (exact filters)
    if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
      RangeQuery rangeQuery = RangeQuery.of(r -> r
          .number(n -> {
            n.field("price");
            if (criteria.getMinPrice() != null) {
              n.gte(criteria.getMinPrice().doubleValue());
            }
            if (criteria.getMaxPrice() != null) {
              n.lte(criteria.getMaxPrice().doubleValue());
            }
            return n;
          })
      );

      boolQueryBuilder.filter(rangeQuery._toQuery());
    }

    // Add brand filter (exact match using nested query)
    if (criteria.getBrandId() != null) {
      NestedQuery brandQuery = NestedQuery.of(n -> n
          .path("brand")
          .query(QueryBuilders.term(t -> t
              .field("brand.id")
              .value(FieldValue.of(criteria.getBrandId()))
          ))
      );

      boolQueryBuilder.filter(brandQuery._toQuery());
    }

    // Add category filter (exact match for any of the specified categories using nested query)
    if (criteria.getCategoryIds() != null && !criteria.getCategoryIds().isEmpty()) {
      NestedQuery categoryQuery = NestedQuery.of(n -> n
          .path("categories")
          .query(QueryBuilders.terms(t -> t
              .field("categories.id")
              .terms(terms -> terms.value(
                  criteria.getCategoryIds()
                      .stream()
                      .map(FieldValue::of)
                      .toList()
              ))
          ))
      );

      boolQueryBuilder.filter(categoryQuery._toQuery());
    }

    // Build the final native query
    NativeQuery searchQuery = queryBuilder
        .withQuery(boolQueryBuilder.build()._toQuery())
        .withPageable(pageable)
        .build();

    // Execute search
    return elasticsearchOperations.search(searchQuery, ElasticsearchProduct.class);
  }
}
