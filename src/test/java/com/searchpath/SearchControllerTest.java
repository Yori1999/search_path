package com.searchpath;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class SearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void testSearch() {
        //With no query parameter
        HttpRequest<String> request = HttpRequest.GET("/search");
        String body = client.toBlocking().retrieve(request);
        Assertions.assertNotNull(body);
        Assertions.assertEquals("{\"cluster_name\":\"docker-cluster\"}", body);
        //With 1 word query parameter
        request = HttpRequest.GET("/search?query=shoes");
        body = client.toBlocking().retrieve(request);
        Assertions.assertNotNull(body);
        Assertions.assertEquals("{\"query\":\"shoes\",\"cluster_name\":\"docker-cluster\"}", body);
        //With several words query parameter
        request = HttpRequest.GET("/search?query=black%20shoes"); //%20 used for concatenating words that are separated by blank spaces
        body = client.toBlocking().retrieve(request);
        Assertions.assertNotNull(body);
        Assertions.assertEquals("{\"query\":\"black shoes\",\"cluster_name\":\"docker-cluster\"}", body);
    }

}
