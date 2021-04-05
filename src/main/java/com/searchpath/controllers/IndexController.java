package com.searchpath.controllers;

import com.searchpath.indexing.ImdbBulkIndexer;
import com.searchpath.indexing.Indexer;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.elasticsearch.ElasticsearchStatusException;

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
        try {
            Indexer bulkIndexer = new ImdbBulkIndexer();
            bulkIndexer.index("src/main/resources/data.tsv", "\t");
            bulkIndexer.updateIndex();
        } catch (ElasticsearchStatusException e){
            System.out.println("The index is already created");
        } catch (IOException e){
            System.out.println("There's a problem while updating the index");
        }
    }


}
