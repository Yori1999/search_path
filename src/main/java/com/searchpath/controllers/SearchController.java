package com.searchpath.controllers;
import com.searchpath.SingleRestHighLevelClient;
import com.searchpath.entities.Message;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import javax.annotation.Nullable;
import java.io.IOException;

@Controller("/search")
public class SearchController {

    @Get(value = "{?query}")
    public HttpResponse<Message> index(@QueryValue("query") @Nullable String query) {


        RestHighLevelClient client = SingleRestHighLevelClient.getInstance().getClient();

        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return HttpResponse.ok( new Message(query, response.getClusterName()) );
        } catch (IOException e) {
            return HttpResponse.ok( new Message("", "") );
        }
    }


}
