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
import java.util.Arrays;
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
        //With query parameter indicating a certain type
        //With only one type
        HttpRequest<String> request = HttpRequest.GET("/search?type=tvspecial");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach( tvSpec -> Assertions.assertTrue("tvSpecial".equals(tvSpec.getType())));

        //With several types
        request = HttpRequest.GET("/search?type=tvmovie,short");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach( tvSpec -> Assertions.assertTrue("tvMovie".equals(tvSpec.getType()) || "short".equals(tvSpec.getType())));
    }

    @Test
    public void testTypeRepeated(){
        HttpRequest<String> request = HttpRequest.GET("/search?type=tvmovie,short,tvmovie");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach( tvSpec -> Assertions.assertTrue("tvMovie".equals(tvSpec.getType()) || "short".equals(tvSpec.getType())));
    }

    @Test
    public void testSearchByGenre(){
        HttpRequest<String> request = HttpRequest.GET("/search");
        long totalFilmNoir = client.toBlocking().retrieve(request, ImdbResponse.class).getAggregations().get("genres").get("film-noir");
        //With query parameter indicating a certain genre
        //With only one genre
        request = HttpRequest.GET("/search?genres=film-noir");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() == totalFilmNoir);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(Arrays.stream(movie.getGenres()).anyMatch("Film-Noir"::equals)));

        //With several types
        //Tune a lil bit so that results are more manageable

        request = HttpRequest.GET("/search?type=videogame&genres=comedy,sci-fi");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach(videogame -> Assertions.assertTrue(
                Arrays.stream(videogame.getGenres()).anyMatch("Comedy"::equals) ||
                         Arrays.stream(videogame.getGenres()).anyMatch("Sci-Fi"::equals) )
        );
    }

    @Test
    public void testGenreRepeated(){
        HttpRequest<String> request = HttpRequest.GET("/search?type=videogame&genres=comedy,sci-fi,comedy");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(10, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach(videogame -> Assertions.assertTrue(
                Arrays.stream(videogame.getGenres()).anyMatch("Comedy"::equals) ||
                        Arrays.stream(videogame.getGenres()).anyMatch("Sci-Fi"::equals) )
        );
    }

    @Test
    public void testSearchByYear(){
        //With query parameter indicating a certain range of years
        //With only one range
        HttpRequest<String> request = HttpRequest.GET("/search?year=1870/1880");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(3, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach(
                m -> Assertions.assertTrue(Integer.parseInt(m.getStart_year()) >= 1870 && Integer.parseInt(m.getStart_year()) <= 1880 )
        );

        //With several ranges
        request = HttpRequest.GET("/search?year=1870/1878,1880/1885");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(6, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach(
                m -> Assertions.assertTrue(Integer.parseInt(m.getStart_year()) >= 1870 && Integer.parseInt(m.getStart_year()) <= 1885 )
        );
    }

    @Test
    public void testYearRepeated(){
        HttpRequest<String> request = HttpRequest.GET("/search?year=1870/1878,1880/1885,1880/1885");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() > 0);
        Assertions.assertTrue(imdbResponse.getItems() != null);
        Assertions.assertEquals(6, imdbResponse.getItems().length); //It'll only return the first 10 results by default
        Arrays.asList(imdbResponse.getItems()).stream().forEach(
                m -> Assertions.assertTrue(Integer.parseInt(m.getStart_year()) >= 1870 && Integer.parseInt(m.getStart_year()) <= 1885 )
        );
    }

    @Test
    public void testSearchByYearWrongDateFormat(){
        //This doesn't get an error response, but obviously there's no match
        HttpRequest<String> request = HttpRequest.GET("/search?year=2010/2000");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertTrue(imdbResponse.getTotal() == 0);
        Assertions.assertTrue(imdbResponse.getItems() == null);
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
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&type=movie");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(27, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getTitle().toLowerCase(Locale.ROOT).contains("avengers") || movie.getOriginal_title().toLowerCase(Locale.ROOT).contains("avengers")));
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getType().equals("movie")));
    }

    @Test
    public void testSearchByTitleAndGenre(){
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&genres=action");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(107, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getTitle().toLowerCase(Locale.ROOT).contains("avengers") || movie.getOriginal_title().toLowerCase(Locale.ROOT).contains("avengers")));
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(Arrays.stream(movie.getGenres()).anyMatch("Action"::equals)));
    }

    @Test
    public void testSearchByTitleAndYear(){
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&year=1990/2020");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(883, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getTitle().toLowerCase(Locale.ROOT).contains("avengers") || movie.getOriginal_title().toLowerCase(Locale.ROOT).contains("avengers")));
        Arrays.asList(imdbResponse.getItems()).stream().forEach(
                m -> Assertions.assertTrue(Integer.parseInt(m.getStart_year()) >= 1990 && Integer.parseInt(m.getStart_year()) <= 2020 )
        );
    }

    @Test
    public void testSearchByTitleTypeGenreAndYear(){
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&type=movie&genres=action&year=1990/2020");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(10, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getTitle().toLowerCase(Locale.ROOT).contains("avengers") || movie.getOriginal_title().toLowerCase(Locale.ROOT).contains("avengers")));
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(movie.getType().equals("movie")));
        Arrays.asList(imdbResponse.getItems()).stream().forEach( movie -> Assertions.assertTrue(Arrays.stream(movie.getGenres()).anyMatch("Action"::equals)));
        Arrays.asList(imdbResponse.getItems()).stream().forEach(
                m -> Assertions.assertTrue(Integer.parseInt(m.getStart_year()) >= 1990 && Integer.parseInt(m.getStart_year()) <= 2020 )
        );
    }

}
