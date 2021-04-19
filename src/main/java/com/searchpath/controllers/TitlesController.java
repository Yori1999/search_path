package com.searchpath.controllers;

import com.searchpath.entities.ImdbObject;
import com.searchpath.searching.SearchingModule;
import io.micronaut.http.annotation.*;

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
