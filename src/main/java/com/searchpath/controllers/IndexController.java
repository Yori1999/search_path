package com.searchpath.controllers;

import com.searchpath.entities.ImdbResponse;
import com.searchpath.indexing.ImdbBulkIndexer;
import com.searchpath.indexing.Indexer;
import com.searchpath.searching.SearchingModule;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import org.elasticsearch.ElasticsearchStatusException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Controller("/index")
public class IndexController {
    @Inject
    @Named("ImdbBulk")
    Indexer bulkIndexer;

    @Get
    public void index() {
        try { //I think this is dirty as hell but I was trying to move fast and fix this later
            Indexer bulkIndexer = new ImdbBulkIndexer();
            bulkIndexer.index("src/main/resources/data.tsv", "\t"); //Also, this may be dirty. Externalize this
            bulkIndexer.updateIndex();
        } catch (ElasticsearchStatusException e){
            System.out.println("The index is already created");
        } catch (IOException e){
            System.out.println("There's a problem while updating the index");
        }
    }


}
