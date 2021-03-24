package com.searchpath;

import com.searchpath.entities.ImdbResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

@MicronautTest
public class AggregatesImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    // Collection of tests for mainly checking the aggregations //

    @Test
    public void testAggregationsSearchRangeDates() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Tron&type=movie&year=1980/2000");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(1, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("dates"));
        Assertions.assertTrue(map.get("dates").size() == 2);
        Map<String, Long> dates = map.get("dates");
        Assertions.assertEquals(1, dates.get("1980-1990"));
        Assertions.assertEquals(0, dates.get("1991-2000"));

        request = HttpRequest.GET("/search?query=Tron&type=movie&year=1980/2020");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(3, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("dates"));
        Assertions.assertTrue(map.get("dates").size() == 4);
        dates = map.get("dates");
        Assertions.assertEquals(1, dates.get("1980-1990"));
        Assertions.assertEquals(0, dates.get("1991-2000"));
        Assertions.assertEquals(1, dates.get("2001-2010"));
        Assertions.assertEquals(1, dates.get("2011-2020"));
    }


    @Test
    public void testAggregationsSearchTitleGenresType() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Tron&type=videoGame&genre=sci-fi,action,adventure");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(13, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("types").size() != 0);
        Assertions.assertTrue(map.get("genres").size() != 0);
        Assertions.assertTrue(map.get("types").containsKey("videogame"));
        Assertions.assertEquals(13, (Long)map.get("types").get("videogame"));
        Arrays.stream(map.get("genres").keySet().toArray()).iterator().forEachRemaining(o -> System.out.println(o.toString()));
        Map<String, Long> genres = map.get("genres");
        Assertions.assertEquals(5, genres.size());
        Assertions.assertEquals(5, genres.get("adventure"));
        Assertions.assertEquals(1, genres.get("fantasy"));
        Assertions.assertEquals(1, genres.get("comedy"));
        Assertions.assertEquals(7, genres.get("sci-fi"));
        Assertions.assertEquals(10, genres.get("action"));

    }

    @Test
    public void testAggregationsSearchTitle() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Carmencita");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(7, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("types").size() != 0);
        Assertions.assertTrue(map.get("genres").size() != 0);
        Assertions.assertTrue(map.get("types").containsKey("movie"));
        Assertions.assertTrue(map.get("types").containsKey("tvepisode"));
        Assertions.assertTrue(map.get("types").containsKey("short"));
        Assertions.assertEquals(1, (Long)map.get("types").get("movie"));
        Assertions.assertEquals(3, (Long)map.get("types").get("tvepisode"));
        Assertions.assertEquals(3, (Long)map.get("types").get("short"));
        Map<String, Long> genres = map.get("genres");
        Assertions.assertEquals(8, genres.size());
        Assertions.assertEquals(3, genres.get("drama"));
        Assertions.assertEquals(3, genres.get("romance"));
        Assertions.assertEquals(1, genres.get("musical"));
        Assertions.assertEquals(3, genres.get("comedy"));
        Assertions.assertEquals(3, genres.get("short"));
        Assertions.assertEquals(1, genres.get("action"));
        Assertions.assertEquals(1, genres.get("documentary"));
        Assertions.assertEquals(1, genres.get("animation"));
    }

    @Test
    public void testAggregationsSearchGenres() {
        HttpRequest<String> request = HttpRequest.GET("/search?genre=comedy");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() != 0);
        Map<String, Long> genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        long comedyDocs = genres.get("comedy");
        genres.entrySet().stream().forEach( e -> Assertions.assertTrue( e.getValue() <= comedyDocs));

        request = HttpRequest.GET("/search?query=Avengers?genre=comedy,action");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() != 0);
        genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        Assertions.assertTrue(genres.containsKey("action"));
    }

    @Test
    public void testAggregationsSearchType() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&type=movie");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 1);
        Assertions.assertTrue(map.get("types").containsKey("movie"));
        Assertions.assertTrue(map.get("types").get("movie") == imdbResponse.getTotal());

        request = HttpRequest.GET("/search?query=Avengers&type=movie,videogame,tvEpisode");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 3);
        Assertions.assertTrue(map.get("types").containsKey("movie"));
        Assertions.assertTrue(map.get("types").containsKey("videogame"));
        Assertions.assertTrue(map.get("types").containsKey("tvepisode"));
        Assertions.assertTrue(map.get("types").values().stream().reduce(0L, Long :: sum) == imdbResponse.getTotal());
    }


}
