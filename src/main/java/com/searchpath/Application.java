package com.searchpath;

import io.micronaut.runtime.Micronaut;

import javax.inject.Inject;

public class Application {

    @Inject static BulkIndexer bulkIndexer;


    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        //System.out.println("Hello");
        bulkIndexer.index();

    }

}
