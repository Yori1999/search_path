package com.searchpath.searching;

import com.searchpath.ClientFactory;
import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.entities.Message;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class ElasticSearchingModule implements SearchingModule {

    @Inject
    ClientFactory clientFactory;

    @Override
    public ImdbResponse processQuery(String query, String genre, String type, String year) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder completeQuery = QueryBuilders.boolQuery();
        if (query != null){
            completeQuery.must(QueryBuilders.multiMatchQuery(query, "originalTitle", "primaryTitle").type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }
        if (genre != null){
            String[] genres = genre.replace(" ", "").split(",");
            completeQuery.filter(QueryBuilders.termsQuery("genres", genres));
        }
        if (type != null){
            String types = type.replace(" ", "").replace(",", " ");
            completeQuery.filter(QueryBuilders.matchQuery("titleType", types));
        }
        DateRangeAggregationBuilder rangeAggregates = null;
        if (year != null){
            rangeAggregates = AggregationBuilders.dateRange("dates").field("startYear").format("yyyy");
            String[] yearRanges = year.replace(" ", "").split(","); //array of the different ranges
            BoolQueryBuilder datesQuery = QueryBuilders.boolQuery();
            RangeQueryBuilder rangeDates = null;
            for (int i = 0; i < yearRanges.length; i++){
                rangeDates = new RangeQueryBuilder("startYear").format("year");
                String[] years = yearRanges[i].split("/");
                int yearFrom = Integer.parseInt(years[0]);
                int yearTo = Integer.parseInt(years[1])+1; //control the exceptions
                rangeDates.gte(yearFrom);
                rangeDates.lte(yearTo);
                datesQuery.should(rangeDates);

                boolean firstDecade = true;
                for (int j = yearFrom; j < yearTo; j += 10){
                    if (!firstDecade){
                        rangeAggregates.addRange(j + "-" + (j+9), j, j+10);
                    } else {
                        rangeAggregates.addRange(j + "-" + (j + 10), j, j + 11);
                        j++;
                        firstDecade = false;
                    }
                }
            }
            completeQuery.filter(datesQuery);
        }
        //BUILD AGGREGATES
        AggregationBuilder aggregations = AggregationBuilders.filter("agg", completeQuery);
        aggregations.subAggregation(AggregationBuilders.terms("types").field("titleType"));
        aggregations.subAggregation(AggregationBuilders.terms("genres").field("genres"));
        if (rangeAggregates!=null) aggregations.subAggregation(rangeAggregates);


        // CREATION OF FUNCTION SCORE QUERY //
        ScoreFunctionBuilder functionWeightMovie = new WeightBuilder().setWeight(10);
        ScoreFunctionBuilder functionWeightTvSeries = new WeightBuilder().setWeight(3);
        ScoreFunctionBuilder functionWeightTvEpisodes = new WeightBuilder().setWeight(0.1f);
        ScoreFunctionBuilder functionWeightVideogames = new WeightBuilder().setWeight(2f);
        ScoreFunctionBuilder functionGaussDecayStartYear =
                new GaussDecayFunctionBuilder("startYear", "now", "9000d", "0d", 0.38);
        ScoreFunctionBuilder functionLinearDecayRating =
                new LinearDecayFunctionBuilder("averageRating", 100, 50, 0, 0.5);
        ScoreFunctionBuilder functionWeightStartYearExists = new WeightBuilder().setWeight(2);
        ScoreFunctionBuilder functionNumVotes =
                new FieldValueFactorFunctionBuilder("numVotes").factor(5).missing(0).modifier(FieldValueFactorFunction.Modifier.SQRT);
        ScoreFunctionBuilder functionAverageRating =
                new FieldValueFactorFunctionBuilder("averageRating").factor(2).missing(0).modifier(FieldValueFactorFunction.Modifier.SQRT);


        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionGaussDecayStartYear),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionLinearDecayRating),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                        existsQuery("startYear"), functionWeightStartYearExists),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                        termQuery("titleType", "movie"), functionWeightMovie),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                        termQuery("titleType", "tvseries"), functionWeightTvSeries),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                        termQuery("titleType", "tvepisode"), functionWeightTvEpisodes),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                        termQuery("titleType", "videogame"), functionWeightVideogames),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionNumVotes),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionAverageRating)
        };


        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(completeQuery, functions);

        searchSourceBuilder.query(functionScoreQuery).aggregation(aggregations);
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
            return parseResponseWithAggregations(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImdbResponse();
    }

    @Override
    public ImdbResponse processTitleAndTypeQuery(String query) {

        if (query == null) query = ""; //Protect the multi match query from receiving a null query

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(query, "originalTitle", "titleType")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
            return parseResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImdbResponse();
    }

    @Override
    public ImdbResponse processTitleQuery(String query) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("originalTitle", query));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
            return parseResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImdbResponse();
    }


    @Override
    public Message processQuery(String query) {
        RestHighLevelClient client = clientFactory.getClient();
        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return new Message(query, response.getClusterName());
        } catch (IOException e) {
            return new Message(query, ""); //Since the way to get here is having issues with the client, not the query
        }
    }

    // PARSING RESPONSES //

    private ImdbResponse parseResponse(SearchResponse response) {
        long total = response.getHits().getTotalHits().value;
        List<ImdbObject> films = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("tconst");
            String title = (String) sourceAsMap.get("primaryTitle");
            String[] genres = ((String) sourceAsMap.get("genres")).split(",");
            String type = (String) sourceAsMap.get("titleType");
            String start_year;
            try {
                Integer.parseInt( (String) sourceAsMap.get("startYear") );
                start_year = (String) sourceAsMap.get("startYear");
            } catch (NumberFormatException e){
                start_year = "";
            }
            String end_year;
            try {
                Integer.parseInt( (String)sourceAsMap.get("endYear") );
                end_year = (String)sourceAsMap.get("endYear");
            } catch (NumberFormatException e){
                end_year = "";
            }
            double averageRating = (double)sourceAsMap.get("averageRating");
            int numVotes = (int)sourceAsMap.get("numVotes");

            films.add(new ImdbObject(id, title, genres, type, start_year, end_year, averageRating, numVotes));
        }
        ImdbObject[] items = new ImdbObject[films.size()];
        return new ImdbResponse(total, films.toArray(items), null);
    }

    private ImdbResponse parseResponseWithAggregations(SearchResponse response) {
        long total = response.getHits().getTotalHits().value;
        List<ImdbObject> films = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("tconst");
            String title = (String) sourceAsMap.get("primaryTitle");
            String[] genres = ((String) sourceAsMap.get("genres")).split(",");
            String type = (String) sourceAsMap.get("titleType");
            String start_year;
            try {
                Integer.parseInt( (String) sourceAsMap.get("startYear") );
                start_year = (String) sourceAsMap.get("startYear");
            } catch (NumberFormatException e){
                start_year = "";
            }
            String end_year;
            try {
                Integer.parseInt( (String)sourceAsMap.get("endYear") );
                end_year = (String)sourceAsMap.get("endYear");
            } catch (NumberFormatException e){
                end_year = "";
            }
            double averageRating = (double)sourceAsMap.get("averageRating");
            int numVotes = (int)sourceAsMap.get("numVotes");
            films.add(new ImdbObject(id, title, genres, type, start_year, end_year, averageRating, numVotes));
        }
        ImdbObject[] items = new ImdbObject[films.size()];
        return new ImdbResponse(total, films.toArray(items), mapAggregations(response.getAggregations().get("agg")));
    }

    private Map<String, Map<String, Long>> mapAggregations(ParsedFilter agg) {
        Map<String, Map<String, Long>> aggregations = new HashMap<>();
        Terms typesBuckets = agg.getAggregations().get("types");
        Map<String, Long> types = new HashMap<>();
        for (Terms.Bucket bucket : typesBuckets.getBuckets()) {
            types.put(bucket.getKey().toString(), bucket.getDocCount());
        }
        aggregations.put("types", types);
        Terms genresBuckets = agg.getAggregations().get("genres");
        Map<String, Long> genres = new HashMap<>();
        for (Terms.Bucket bucket : genresBuckets.getBuckets()) {
            genres.put(bucket.getKey().toString(), bucket.getDocCount());
        }
        aggregations.put("genres", genres);
        Range rangesBuckets = agg.getAggregations().get("dates");
        if (rangesBuckets!=null){
            Map<String, Long> dates = new HashMap<>();
            for (Range.Bucket bucket : rangesBuckets.getBuckets()){
                System.out.println(bucket.getKey());
                dates.put(bucket.getKey().toString(), bucket.getDocCount());
            }
            aggregations.put("dates", dates);
        }
        return aggregations;
    }

}
