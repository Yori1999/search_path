package com.searchpath.controllers;

import com.searchpath.searching.SearchingModule;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

@Controller("/search")
public class SearchController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Get(value = "{?query,genre,type,year}")
    public HttpResponse index(@QueryValue("query") @Nullable String query, @QueryValue("genre") @Nullable String genre,
                              @QueryValue("type") @Nullable String type, @QueryValue("year") @Nullable String year) {

        return HttpResponse.ok(searchModule.processQuery(query, genre, type, year));

    }



}
