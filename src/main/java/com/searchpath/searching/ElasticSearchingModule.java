package com.searchpath.searching;

import com.searchpath.ClientFactory;
import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.entities.Message;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
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
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

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
        //Queries for making sure the facets are filtered ok
        BoolQueryBuilder queryForGenresFacetUpdate = QueryBuilders.boolQuery();
        BoolQueryBuilder queryForTypesFacetUpdate = QueryBuilders.boolQuery();
        BoolQueryBuilder queryForYearFacetUpdates = QueryBuilders.boolQuery();


        BoolQueryBuilder postFilters = QueryBuilders.boolQuery();
        if (query != null && !"".equals(query)){
            completeQuery.must(QueryBuilders.multiMatchQuery(query).field("originalTitle", 2).field("primaryTitle", 5).type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }
        if (genre != null){
            String[] genres = genre.replace(" ", "").split(",");
            postFilters.filter((QueryBuilders.termsQuery("genres", genres)));
            queryForTypesFacetUpdate.filter(QueryBuilders.termsQuery("genres", genres));
            queryForYearFacetUpdates.filter(QueryBuilders.termsQuery("genres", genres));
        }
        if (type != null){
            String types = type.replace(" ", "").replace(",", " ");
            postFilters.filter((QueryBuilders.matchQuery("titleType", types)));
            queryForGenresFacetUpdate.filter(QueryBuilders.matchQuery("titleType", types));
            queryForYearFacetUpdates.filter(QueryBuilders.matchQuery("titleType", types));
        }
        if (year != null){
            String[] yearRanges = year.replace(" ", "").split(","); //array of the different ranges
            BoolQueryBuilder datesQuery = QueryBuilders.boolQuery();
            RangeQueryBuilder rangeDates = null;
            for (int i = 0; i < yearRanges.length; i++){
                rangeDates = new RangeQueryBuilder("startYear").format("year");
                String[] years = yearRanges[i].split("/");
                int yearFrom = Integer.parseInt(years[0]);
                int yearTo = Integer.parseInt(years[1]);
                rangeDates.gte(yearFrom); // greater than or equal to
                rangeDates.lte(yearTo); // less tham or equal to
                datesQuery.should(rangeDates);
            }
            postFilters.filter(datesQuery);
            //queryForFacetUpdates.filter(datesQuery);
            queryForGenresFacetUpdate.filter(datesQuery);
            queryForTypesFacetUpdate.filter(datesQuery);
        }

        searchSourceBuilder.postFilter(postFilters);

        //BUILD AGGREGATES
        AggregationBuilder aggregations = AggregationBuilders.filter("agg", completeQuery);
        DateRangeAggregationBuilder rangeAggregates =
                AggregationBuilders.dateRange("year").field("startYear").format("yyyy");
        boolean firstDecade = true;
        for (int j = 1870; j < 2030; j += 10){
            if (!firstDecade){
                rangeAggregates.addRange(j + "-" + (j+9), j, j+10);
            } else {
                rangeAggregates.addRange(j + "-" + (j + 10), j, j + 11);
                j++;
                firstDecade = false;
            }
        }
        aggregations.subAggregation(
                AggregationBuilders.filter("types", queryForTypesFacetUpdate).subAggregation(
                        AggregationBuilders.terms("types").field("titleType").size(100)
                )
        );
        aggregations.subAggregation(
                AggregationBuilders.filter("genres", queryForGenresFacetUpdate).subAggregation(
                        AggregationBuilders.terms("genres").field("genres").size(100)
                )
        );
        aggregations.subAggregation(
                AggregationBuilders.filter("years", queryForYearFacetUpdates).subAggregation(
                        rangeAggregates
                )
        );

        // CREATION OF FUNCTION SCORE QUERY //
        List<FunctionScoreQueryBuilder.FilterFunctionBuilder> functions = createScoreFunctions(query); // a separate method to encapsulate the functions' creation
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders
                .functionScoreQuery(completeQuery, functions.toArray(
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[functions.size()]))
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM);
        // Sets the query for the request and the aggregates
        searchSourceBuilder.query(functionScoreQuery).aggregation(aggregations);

        //For suggesting possible related terms whenever the search is made
        if (query!=null){
             searchSourceBuilder.suggest(new SuggestBuilder().addSuggestion("suggestions",
                                SuggestBuilders.phraseSuggestion("primaryTitle").text(query)));
        }

        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
            //Prints suggestions. Just for testing
            if (response.getSuggest()!=null)
                response.getSuggest().getSuggestion("suggestions").getEntries().get(0).forEach( e -> System.out.println(e.getText()));
            return parseResponseWithAggregations(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImdbResponse();
    }

    private List<FunctionScoreQueryBuilder.FilterFunctionBuilder> createScoreFunctions(String query){
        ScoreFunctionBuilder functionWeightMovie = new WeightBuilder().setWeight(10);
        ScoreFunctionBuilder functionWeightTvSeries = new WeightBuilder().setWeight(5);
        ScoreFunctionBuilder functionWeightShorts = new WeightBuilder().setWeight(3f);
        ScoreFunctionBuilder functionWeightTvEpisodes = new WeightBuilder().setWeight(0.05f);
        ScoreFunctionBuilder functionWeightVideogames = new WeightBuilder().setWeight(3f);
        ScoreFunctionBuilder functionGaussDecayStartYear =
                new GaussDecayFunctionBuilder("startYear", "now", "9000d", "0d", 0.38);
        ScoreFunctionBuilder functionLinearDecayRating =
                new LinearDecayFunctionBuilder("averageRating", 100, 50, 0, 0.5);
        ScoreFunctionBuilder functionWeightStartYearExists = new WeightBuilder().setWeight(2);
        ScoreFunctionBuilder functionNumVotes =
                new FieldValueFactorFunctionBuilder("numVotes").factor(0.05f).missing(0).modifier(FieldValueFactorFunction.Modifier.SQRT);
        ScoreFunctionBuilder functionAverageRating =
                new FieldValueFactorFunctionBuilder("averageRating").factor(0.05f).missing(0).modifier(FieldValueFactorFunction.Modifier.SQRT);

        List<FunctionScoreQueryBuilder.FilterFunctionBuilder> functions = new ArrayList<>();
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionGaussDecayStartYear));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionLinearDecayRating));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                existsQuery("startYear"), functionWeightStartYearExists));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                termQuery("titleType", "movie"), functionWeightMovie));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                termQuery("titleType", "short"), functionWeightShorts));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                termQuery("titleType", "tvseries"), functionWeightTvSeries));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                termQuery("titleType", "tvepisode"), functionWeightTvEpisodes));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.
                termQuery("titleType", "videogame"), functionWeightVideogames));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionNumVotes));
        functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(functionAverageRating));
        if (query!=null){
            functions.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                    QueryBuilders.matchQuery("primaryTitle", query), new WeightBuilder().setWeight(40)));
        }
        return functions;
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
    public ImdbObject processTitleId(String id) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("tconst", id));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
            return parseImdbObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImdbObject();
    }

    private ImdbObject parseImdbObject(SearchResponse response) {
        if (response.getHits().getHits().length != 0){
            SearchHit hit = response.getHits().getHits()[0];
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("tconst");
            String title = (String) sourceAsMap.get("primaryTitle");
            String originalTitle = (String) sourceAsMap.get("originalTitle");
            String[] genres = new String[0];
            if (sourceAsMap.get("genres")!=null){
                genres = ((String) sourceAsMap.get("genres")).split(",");
            }
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
            Double averageRating = null;
            if (sourceAsMap.get("averageRating") != null){
                averageRating = (double)sourceAsMap.get("averageRating");
            }
            Integer numVotes = null;
            if (sourceAsMap.get("numVotes") != null){
                numVotes = (int)sourceAsMap.get("numVotes");
            }
            boolean isAdult = Integer.parseInt((String)sourceAsMap.get("isAdult"))==1;
            String runtimeMinutes = (String) sourceAsMap.get("runtimeMinutes");

            return new ImdbObject(id, title, originalTitle, genres, type, start_year, end_year, averageRating, numVotes, runtimeMinutes, isAdult);
        }
        return new ImdbObject();
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

    private ImdbResponse parseResponseWithAggregations(SearchResponse response) {
        long total = response.getHits().getTotalHits().value;
        List<ImdbObject> films = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("tconst");
            String title = (String) sourceAsMap.get("primaryTitle");
            String[] genres = new String[0];
            if (sourceAsMap.get("genres")!=null){
                genres = ((String) sourceAsMap.get("genres")).split(",");
            }
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
            Double averageRating = null;
            if (sourceAsMap.get("averageRating") != null){
                averageRating = (double)sourceAsMap.get("averageRating");
            }
            Integer numVotes = null;
            if (sourceAsMap.get("numVotes") != null){
                numVotes = (int)sourceAsMap.get("numVotes");
            }
            films.add(new ImdbObject(id, title, genres, type, start_year, end_year, averageRating, numVotes));
        }
        ImdbObject[] items = new ImdbObject[films.size()];
        return new ImdbResponse(total, films.toArray(items), mapAggregations(response.getAggregations().get("agg")));
    }

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

    //Maps the aggregations returned by ElasticSearch into a map which has entries for types, genres and years
    //The map will have 3 entries (types, genres, years) and the value for each key will be a map containing an
    //entry for each corresponding bucket and the number of results of that bucket
    private Map<String, Map<String, Long>> mapAggregations(ParsedFilter agg) {
        Map<String, Map<String, Long>> aggregations = new HashMap<>();
        ParsedFilter aggTypes = agg.getAggregations().get("types");
        Terms typesBuckets = aggTypes.getAggregations().get("types");
        SortedMap<String, Long> types = new TreeMap<>();
        for (Terms.Bucket bucket : typesBuckets.getBuckets()) {
            types.put(bucket.getKey().toString(), bucket.getDocCount());
        }
        aggregations.put("types", types);

        ParsedFilter aggGenres = agg.getAggregations().get("genres");
        Terms genresBuckets = aggGenres.getAggregations().get("genres");
        SortedMap<String, Long> genres = new TreeMap<>();
        for (Terms.Bucket bucket : genresBuckets.getBuckets()) {
            genres.put(bucket.getKey().toString(), bucket.getDocCount());
        }
        aggregations.put("genres", genres);

        ParsedFilter aggYears = agg.getAggregations().get("years");
        Range rangesBuckets = aggYears.getAggregations().get("year");
        if (rangesBuckets!=null){
            SortedMap<String, Long> dates = new TreeMap<>(Collections.reverseOrder());
            for (Range.Bucket bucket : rangesBuckets.getBuckets()){
                if (bucket.getDocCount() != 0) dates.put(bucket.getKey().toString(), bucket.getDocCount());
            }
            aggregations.put("year", dates);
        }
        return aggregations;
    }

}
