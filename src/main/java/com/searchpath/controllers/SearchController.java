package com.searchpath.controllers;

import com.searchpath.ClientFactory;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Controller("/search")
public class SearchController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Inject
    ClientFactory clientFactory;

    @Get(value = "{?query}")
    public HttpResponse<Message> index(@QueryValue("query") @Nullable String query) throws IOException {

        //return HttpResponse.ok(searchModule.processQuery(query));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("imdb");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title", query));
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = clientFactory.getClient().search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response);
        return HttpResponse.ok();
    }


}
