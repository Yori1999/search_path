package com.searchpath;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class SingleRestHighLevelClient {

    private static SingleRestHighLevelClient instance = new SingleRestHighLevelClient();

    private RestHighLevelClient client;

    private SingleRestHighLevelClient(){
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")
                )
        );
    }

    public static SingleRestHighLevelClient getInstance(){
        return instance;
    }

    public RestHighLevelClient getClient(){
        return client;
    }


}
