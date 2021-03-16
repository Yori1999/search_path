package com.searchpath;

import com.searchpath.entities.Message;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class DockerNameSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void testSearch() {

        //With no query parameter
        HttpRequest<String> request = HttpRequest.GET("/search");
        Message msgRetrieved = client.toBlocking().retrieve(request, Message.class);
        Assertions.assertNotNull(msgRetrieved);
        Assertions.assertEquals(null, msgRetrieved.getQuery());
        Assertions.assertEquals("docker-cluster", msgRetrieved.getCluster_name());

        //With an empty query
        request = HttpRequest.GET("/search?query=");
        msgRetrieved = client.toBlocking().retrieve(request, Message.class);
        Assertions.assertNotNull(msgRetrieved);
        Assertions.assertEquals(null, msgRetrieved.getQuery());
        Assertions.assertEquals("docker-cluster", msgRetrieved.getCluster_name());

        //With 1 word query parameter
        request = HttpRequest.GET("/search?query=shoes");
        msgRetrieved = client.toBlocking().retrieve(request, Message.class);
        Assertions.assertNotNull(msgRetrieved);
        Assertions.assertEquals("shoes", msgRetrieved.getQuery());
        Assertions.assertEquals("docker-cluster", msgRetrieved.getCluster_name());

        //With several words query parameter
        request = HttpRequest.GET("/search?query=black%20shoes"); //%20 used for concatenating words that are separated by blank spaces
        msgRetrieved = client.toBlocking().retrieve(request, Message.class);
        Assertions.assertNotNull(msgRetrieved);
        Assertions.assertEquals("black shoes", msgRetrieved.getQuery());
        Assertions.assertEquals("docker-cluster", msgRetrieved.getCluster_name());
    }


    //TEST DONDE NO HAYA UN HOST CORRIENDO

}
