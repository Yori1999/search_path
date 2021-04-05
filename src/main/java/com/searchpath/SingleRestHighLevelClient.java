package com.searchpath;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.PreDestroy;
import java.io.IOException;

public class SingleRestHighLevelClient {

    private static SingleRestHighLevelClient instance = new SingleRestHighLevelClient();

    private RestHighLevelClient client;

    private SingleRestHighLevelClient(){
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http") //not good, isn't flexible. Array of addresses or create up to N hosts
                )
        );
    }

    public static SingleRestHighLevelClient getInstance(){
        return instance;
    }

    public RestHighLevelClient getClient(){
        return client;
    }

    @PreDestroy
    public void closeClient(){
        System.out.println("Releasing resources...");
        try {
            client.close();
            System.out.println("Resources released");
        } catch (IOException e){
            System.out.println("There's been an error while attempting to release resources");
        }
    }


}
