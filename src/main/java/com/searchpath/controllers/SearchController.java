package com.searchpath.controllers;

import com.searchpath.entities.ImdbResponse;
import com.searchpath.searching.SearchingModule;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.annotation.Error;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

@Controller("/search")
public class SearchController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Get(value = "{?query,genre,type,year}")
    public ImdbResponse index(@QueryValue("query") @Nullable String query, @QueryValue("genre") @Nullable String genre,
                              @QueryValue("type") @Nullable String type, @QueryValue("year") @Nullable String year) {

       return searchModule.processQuery(query, genre, type, year); //returns directly the response type, not an HTTPResponse
    }

    //make this a lil more personal for each type of error/exception (with ExceptionHandler annotation)
    @Error (global = true)
    public HttpResponse<JsonError> error(HttpRequest request, Throwable e) {
        JsonError error = new JsonError("Bad Things Happened: " + e.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>serverError()
                .body(error);
    }


}
