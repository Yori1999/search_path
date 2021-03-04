package com.searchpath;

import com.searchpath.entities.ImdbResponse;
import com.searchpath.entities.Message;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class ImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void testSearch() {

        //With no query parameter
        HttpRequest<String> request = HttpRequest.GET("/search");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertEquals(null, imdbResponse.getItems());

        //With empty query parameter
        request = HttpRequest.GET("/search?query=");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertEquals(null, imdbResponse.getItems());

        //With empty query parameter
        request = HttpRequest.GET("/search?query=Spiderman");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertNotEquals(null, imdbResponse.getItems());
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default

    }


}
