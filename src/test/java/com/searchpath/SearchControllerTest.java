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
        HttpRequest<String> request = HttpRequest.GET("/search");
        String body = client.toBlocking().retrieve(request);
        Assertions.assertNotNull(body);
        //Assertions.assertEquals(body);
    }

}
