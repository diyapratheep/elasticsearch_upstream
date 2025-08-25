/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */
package org.elasticsearch.action.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.List;

//his class is our "scorer". It looks at a query, checks for complex parts like bool and wildcard, and assigns points accordingly.
public class QueryComplexityAnalyzer {

    public int calculate(QueryBuilder query, List<AggregationBuilder> aggregations) {
        int complexity = 0;
        // We start by checking the main query part.
        complexity += getQueryComplexity(query);
        // Then we add the complexity of any aggregations (like calculating averages, sums etc).
        for (AggregationBuilder agg : aggregations) {
            complexity += getAggregationComplexity(agg);
        }
        return complexity;
    }

    // This function recursively calculates the score for a query.
    private int getQueryComplexity(QueryBuilder query) {
        if (query == null) {
            return 0;
        }

        int score = 1; // Every query part gets at least 1 point.

        // Is it a "bool" query? (a query with "must", "should", "must_not")
        // These can contain other queries inside them, so we need to check those too.
        if (query instanceof BoolQueryBuilder) {
            BoolQueryBuilder boolQuery = (BoolQueryBuilder) query;
            // Recursively add the complexity of inner queries.
            for (QueryBuilder innerQuery : boolQuery.must()) {
                score += getQueryComplexity(innerQuery);
            }
            for (QueryBuilder innerQuery : boolQuery.should()) {
                score += getQueryComplexity(innerQuery);
            }
            for (QueryBuilder innerQuery : boolQuery.mustNot()) {
                score += getQueryComplexity(innerQuery);
            }
        }

        if (query instanceof WildcardQueryBuilder) {
            score += 20;
        }

        return score;
    }

    // This function calculates the score for aggregations.
    // Deeply nested aggregations are complex, so we score them recursively.
    private int getAggregationComplexity(AggregationBuilder aggregation) {
        if (aggregation == null) {
            return 0;
        }

        int score = 5; // Aggregations are more expensive, so they start at 5 points.

        // Add complexity for any sub-aggregations.
        for (AggregationBuilder subAgg : aggregation.getSubAggregations()) {
            score += getAggregationComplexity(subAgg);
        }

        return score;
    }
}
