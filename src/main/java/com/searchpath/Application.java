package com.searchpath;

import com.searchpath.indexing.BulkIndexer;
import com.searchpath.indexing.Indexer;
import io.micronaut.runtime.Micronaut;

import javax.inject.Inject;
import javax.inject.Named;

public class Application {

   // @Inject @Named("Bulk") static Indexer bulkIndexer;


    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        //System.out.println("Hello");
        //Indexer bulkIndexer = new BulkIndexer();
        //bulkIndexer.index("data.tsv", "\t");

    }

}
