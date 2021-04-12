package com.searchpath.controllers;

import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.searching.SearchingModule;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

@Controller("/titles")
public class TitlesController {

    @Inject @Named("Elastic") SearchingModule searchModule;

    @Get(value = "/{titleId}")
    public ImdbObject searchTitle(@PathVariable String titleId){
        return searchModule.processTitleId(titleId);
    }



}
