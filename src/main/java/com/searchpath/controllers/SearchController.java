package com.searchpath.controllers;

import com.searchpath.ClientFactory;
import com.searchpath.entities.Film;
import com.searchpath.entities.FilmResponse;
import com.searchpath.searching.SearchingModule;
import com.searchpath.entities.Message;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller("/search")
public class SearchController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Inject
    ClientFactory clientFactory;

    @Get(value = "{?query}")
    public HttpResponse<FilmResponse> index(@QueryValue("query") @Nullable String query) throws IOException {

        //return HttpResponse.ok(searchModule.processQuery(query));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("originalTitle", query));
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
        return HttpResponse.ok(parseResponse(response));
    }

    private FilmResponse parseResponse(SearchResponse response) {
        long total = response.getHits().getTotalHits().value;
        List<Film> films = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String id = (String) sourceAsMap.get("tconst");
            String title = (String) sourceAsMap.get("primaryTitle");
            String[] genres = ((String) sourceAsMap.get("genres")).split(",");
            String type = (String) sourceAsMap.get("titleType");
            String start_year = (String) sourceAsMap.get("startYear");
            String end_year;
            try {
                Integer.parseInt( (String)sourceAsMap.get("endYear") );
                end_year = (String)sourceAsMap.get("endYear");
            } catch (NumberFormatException e){
                end_year = "";
            }
            films.add(new Film(id, title, genres, type, start_year, end_year));
        }
        Film[] items = new Film[films.size()];
        return new FilmResponse(total, films.toArray(items));
    }


}
