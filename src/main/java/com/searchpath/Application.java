package com.searchpath;

import com.searchpath.indexing.ImdbBulkIndexer;
import com.searchpath.indexing.Indexer;
import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);

        Indexer bulkIndexer = new ImdbBulkIndexer();
        bulkIndexer.index("data.tsv", "\t");

    }

}
