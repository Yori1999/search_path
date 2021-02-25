package com.searchpath;

import com.searchpath.entities.Message;
import io.micronaut.http.HttpResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ElasticSearchingModule implements SearchingModule {
    @Override
    public HttpResponse<Message> processQuery(String query) {
        RestHighLevelClient client = SingleRestHighLevelClient.getInstance().getClient();
        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return HttpResponse.ok( new Message(query, response.getClusterName()) ); //try not to use the object creation directly
        } catch (IOException e) {
            return HttpResponse.ok( new Message(query, "") ); //Since the way to get here is having issues with the client, not the query
        }
    }
}
