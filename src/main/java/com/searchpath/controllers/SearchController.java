package com.searchpath.controllers;

import com.searchpath.ClientFactory;
import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.searching.SearchingModule;
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
import java.util.List;
import java.util.Map;

@Controller("/search")
public class SearchController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Inject
    ClientFactory clientFactory;

    @Get(value = "{?query}")
    public HttpResponse index(@QueryValue("query") @Nullable String query) throws IOException {

        //return HttpResponse.ok(searchModule.processQuery(query));

        //return HttpResponse.ok(searchModule.processTitleQuery(query));
        return HttpResponse.ok(searchModule.processTitleAndTypeQuery(query));

    }




}
