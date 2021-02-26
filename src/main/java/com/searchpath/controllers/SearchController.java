package com.searchpath.controllers;

import com.searchpath.SearchingModule;
import com.searchpath.entities.Message;
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


    @Get(value = "{?query}")
    public HttpResponse<Message> index(@QueryValue("query") @Nullable String query) {

        return HttpResponse.ok(searchModule.processQuery(query));


    }


}
