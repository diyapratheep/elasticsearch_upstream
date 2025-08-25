/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */
package org.elasticsearch.action.search;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static java.util.Collections.emptyList;

public class QueryComplexityAnalyzerTests extends ESTestCase {

    public void testSimpleQueryComplexity() {
        QueryComplexityAnalyzer analyzer = new QueryComplexityAnalyzer();
        
        // A very simple query.
        int score = analyzer.calculate(QueryBuilders.termQuery("user", "kimchy"), emptyList());
        // We expect its score to be 1.f
        assertEquals(1, score);
    }
    
    public void testComplexQueryComplexity() {
        QueryComplexityAnalyzer analyzer = new QueryComplexityAnalyzer();

        // A more complex, nested query.
        BoolQueryBuilder complexQuery = QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("field1", "value1"))
            .should(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("field2", "value2"))
                .must(QueryBuilders.wildcardQuery("field3", "val*")));
                
        // Let's calculate what the score should be:
        // bool = 1
        //   must(term) = 1
        //   should(bool) = 1
        //     must(term) = 1
        //     must(wildcard) = 1 (for the term part) + 20 (for the wildcard penalty)
        // Total = 1 + 1 + 1 + 1 + 21 = 25
        int score = analyzer.calculate(complexQuery, emptyList());
        assertEquals(25, score);
    }
}