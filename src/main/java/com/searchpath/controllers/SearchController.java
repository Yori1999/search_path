package com.searchpath.controllers;
import com.searchpath.Message;
import io.micronaut.elasticsearch.ElasticsearchSettings;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import org.apache.http.HttpHost;
import io.micronaut.http.HttpResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import javax.annotation.Nullable;
import java.io.IOException;

@Controller("/search")
public class SearchController {

    @Get(value = "{?query}")
    public HttpResponse index(@QueryValue("query") @Nullable String query) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")
                )
        );
        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return HttpResponse.ok( new Message(query, response.getClusterName()) );
        } catch (IOException e) {
            return HttpResponse.ok("There's been a problem");
        }
    }


}
