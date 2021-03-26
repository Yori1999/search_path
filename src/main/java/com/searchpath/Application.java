package com.searchpath;

import com.searchpath.indexing.ImdbBulkIndexer;
import com.searchpath.indexing.Indexer;
import io.micronaut.runtime.Micronaut;
import org.elasticsearch.ElasticsearchStatusException;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        try { //I think this is dirty as hell but I was trying to move fast and fix this later
            Indexer bulkIndexer = new ImdbBulkIndexer();
            bulkIndexer.index("data.tsv", "\t"); //Also, this may be dirty. Externalize this
        } catch (ElasticsearchStatusException e){
            System.out.println("The index is already created");
        }
    }

}
