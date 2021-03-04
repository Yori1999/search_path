package com.searchpath;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);

        /*Indexer bulkIndexer = new BulkIndexer();
        bulkIndexer.index("data.tsv", "\t");*/

    }

}
