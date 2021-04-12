package com.searchpath;

import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Locale;

@MicronautTest
public class ImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    //Collection of tests for checking the returned items when searching

    @Test
    public void testSearchAll() {
        //With no query parameter
        HttpRequest<String> request = HttpRequest.GET("/search");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);

        //With empty query parameter. Works as if the query was null -> better integration with frontend
        request = HttpRequest.GET("/search?query=");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
    }

    @Test
    public void testSearchByTitle(){
        //With query parameter indicating a certain movie
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Assertions.assertTrue(imdbResponse.getItems()[0].getTitle().toLowerCase(Locale.ROOT).contains("avengers"));
        Assertions.assertEquals("movie", imdbResponse.getItems()[0].getType());
    }

    @Test
    public void testSearchByType(){

    }

    @Test
    public void testSearchByGenre(){

    }

    @Test
    public void testSearchByYear(){

    }

    @Test
    public void testSearchByYearWrongDateFormat(){
        //with from year being less than until year
    }

    @Test
    public void testSearchByIdExists(){
        HttpRequest<String> request = HttpRequest.GET("/titles/tt0084881");
        ImdbObject movie = client.toBlocking().retrieve(request, ImdbObject.class);
        Assertions.assertNotNull(movie);
        Assertions.assertTrue(movie.getTitle().equals("Arcadia of My Youth"));
    }

    @Test
    public void testSearchByIdNotExists(){
        HttpRequest<String> request = HttpRequest.GET("/titles/tt0084e");
        ImdbObject movie = client.toBlocking().retrieve(request, ImdbObject.class);
        Assertions.assertNotNull(movie); //The object itself is not null, it's just completely empty
        //All the fields are set to null
        Assertions.assertTrue(movie.getId() == null);
        Assertions.assertTrue(movie.getTitle() == null);
        Assertions.assertTrue(movie.getOriginal_title() == null);
        Assertions.assertTrue(movie.getType() == null);
        Assertions.assertTrue(movie.getGenres() == null);
        Assertions.assertTrue(movie.getStart_year() == null);
        Assertions.assertTrue(movie.getEnd_year() == null);
        Assertions.assertTrue(movie.getRuntime_minutes() == null);
        Assertions.assertTrue(movie.getNum_votes() == null);
        Assertions.assertTrue(movie.getAverage_rating() == null);
    }

    @Test
    public void testSearchByTitleAndType() {

    }

    @Test
    public void testSearchByTitleAndGenre(){

    }

    @Test
    public void testSearchByTitleAndYear(){

    }

    @Test
    public void testSearchByTitleTypeGenreAndYear(){

    }

}
