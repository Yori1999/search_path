package com.searchpath;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Factory
public class ClientFactory {

    @Bean(preDestroy = "close")
    @Singleton
    public RestHighLevelClient getClient(){
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http") //not good, isn't flexible. Array of addresses or create up to N hosts
                ));
    }

}
