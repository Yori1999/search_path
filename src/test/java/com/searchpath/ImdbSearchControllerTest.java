package com.searchpath;

import com.searchpath.entities.ImdbResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
public class ImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    //Collection of tests for checking the returned items when searching

    @Test
    public void testSearch() {
        //With no query parameter
        HttpRequest<String> request = HttpRequest.GET("/search");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertTrue(imdbResponse.getTotal() > 0); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertTrue(imdbResponse.getItems() != null);

        //With empty query parameter
        request = HttpRequest.GET("/search?query=");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertEquals(null, imdbResponse.getItems());

        //With query parameter indicating a certain movie
        request = HttpRequest.GET("/search?query=Spiderman");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertNotEquals(null, imdbResponse.getItems());
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default

        //With query parameter indicating a certain movie
        request = HttpRequest.GET("/search?query=Avengers");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotEquals(0, imdbResponse.getTotal()); //right now it doesn't find anything, maybe in the future should retrieve everything? With a wildcard??
        Assertions.assertNotEquals(null, imdbResponse.getItems());
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Assertions.assertNotEquals("movie", imdbResponse.getItems()[0].getType()); //if we search by title

        //With query parameter indicating a certain movie and specifying we want a movie
        request = HttpRequest.GET("/search?query=Avengers&type=movie");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotEquals(0, imdbResponse.getTotal());
        Assertions.assertNotEquals(null, imdbResponse.getItems());
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Assertions.assertEquals("movie", imdbResponse.getItems()[0].getType());
    }

    //Test searc


}
