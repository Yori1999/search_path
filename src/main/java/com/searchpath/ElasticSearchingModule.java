package com.searchpath;

import com.searchpath.entities.Message;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ElasticSearchingModule implements SearchingModule {

    @Inject ClientFactory clientFactory;

    @Override
    public Message processQuery(String query) {
        //RestHighLevelClient client = SingleRestHighLevelClient.getInstance().getClient();
        RestHighLevelClient client = clientFactory.getClient(); //parameterize???
        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return new Message(query, response.getClusterName());
        } catch (IOException e) {
            return new Message(query, ""); //Since the way to get here is having issues with the client, not the query
        }
    }

}
