package com.searchpath;

import com.searchpath.entities.ImdbResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class SuggestionsImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    // Collection of tests for mainly checking the suggestions //

    @Test
    public void testSuggestionsWellWrittenTitle() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avenger");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(921, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Assertions.assertNull(imdbResponse.getSuggestions()); //the term is well written so no suggestions will be returned

        request = HttpRequest.GET("/search?query=avengers%20assemble");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(992, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Assertions.assertNull(imdbResponse.getSuggestions()); //the terms are well written so no suggestions will be returned
    }

    @Test
    public void testSuggestionsBadWrittenTitle() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=avnger");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(0, imdbResponse.getTotal()); //no document matches
        Assertions.assertNull(imdbResponse.getItems());
        Assertions.assertNull(imdbResponse.getAggregations());
        Assertions.assertNotNull(imdbResponse.getSuggestions()); //the term is bad written so suggestions will be provided
        for (String suggestion : imdbResponse.getSuggestions()){
            request = HttpRequest.GET("/search?query=" + suggestion);
            imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
            Assertions.assertTrue(imdbResponse.getTotal()>0); //the suggestions suggests us queries that'll return matching documents
        }

        request = HttpRequest.GET("/search?query=avngers%20asemble");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(0, imdbResponse.getTotal());
        Assertions.assertNull(imdbResponse.getItems());
        Assertions.assertNull(imdbResponse.getAggregations());
        Assertions.assertNotNull(imdbResponse.getSuggestions()); //the terms are bad written so suggestions will be provided
        for (String suggestion : imdbResponse.getSuggestions()){
            request = HttpRequest.GET("/search?query=" + suggestion.replace(" ", "%20"));
            imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
            Assertions.assertTrue(imdbResponse.getTotal()>0); //the suggestions suggests us queries that'll return matching documents
        }
    }

    //For when the title is well written but the suggester identifies it as badly written
    @Test
    public void testSuggestionsWellWrittenTitleMisspelledWord() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Tron");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(127, imdbResponse.getTotal()); //no document matches
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Assertions.assertNotNull(imdbResponse.getSuggestions()); //the term is bad written so suggestions will be provided
        for (String suggestion : imdbResponse.getSuggestions()){
            request = HttpRequest.GET("/search?query=" + suggestion);
            imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
            Assertions.assertTrue(imdbResponse.getTotal()>0); //the suggestions suggests us queries that'll return matching documents
        }
    }

}
