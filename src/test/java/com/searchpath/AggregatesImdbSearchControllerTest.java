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
public class AggregatesImdbSearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    // Collection of tests for mainly checking the aggregations //

    @Test
    public void testAggregationsSearchRangeDates() {
        //Don't fix anything (but the query)
        HttpRequest<String> request = HttpRequest.GET("/search?query=Tron");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(127, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 7);
        Map<String, Long> dates = map.get("year");
        //only intervals with some results show
        Assertions.assertEquals(1, dates.get("1951-1960"));
        Assertions.assertEquals(3, dates.get("1971-1980"));
        Assertions.assertEquals(8, dates.get("1981-1990"));
        Assertions.assertEquals(2, dates.get("1991-2000"));
        Assertions.assertEquals(35, dates.get("2001-2010"));
        Assertions.assertEquals(71, dates.get("2011-2020"));
        Assertions.assertEquals(1, dates.get("2021-2030"));

        //Fix only the year: the facet will fix, but results will be filtered and only titles from 1980 till 2000 will
        //be returned
        request = HttpRequest.GET("/search?query=Tron&year=1980/2000");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(10, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 7);
        dates = map.get("year");
        Assertions.assertEquals(1, dates.get("1951-1960"));
        Assertions.assertEquals(3, dates.get("1971-1980"));
        Assertions.assertEquals(8, dates.get("1981-1990"));
        Assertions.assertEquals(2, dates.get("1991-2000"));
        Assertions.assertEquals(35, dates.get("2001-2010"));
        Assertions.assertEquals(71, dates.get("2011-2020"));
        Assertions.assertEquals(1, dates.get("2021-2030"));

        //Fix another filter: the year facet will change according to the results
        request = HttpRequest.GET("/search?query=Tron&type=movie");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(5, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 3);
        Map<String, Long> dates2 = map.get("year");
        //Beware: if a title doesn't have a start_year attribute, then it cannot be included in any range date
        Assertions.assertTrue(dates2.entrySet().size() <= dates.entrySet().size());
        Assertions.assertEquals(1, dates2.get("1981-1990"));
        Assertions.assertEquals(1, dates2.get("2001-2010"));
        Assertions.assertEquals(1, dates2.get("2011-2020"));

        //Fix another filter + the year
        //Facets will get fixed, but returned titles will only be movies between 1980 and 2020
        request = HttpRequest.GET("/search?query=Tron&type=movie&year=1980/2020");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(3, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 3);
        dates = map.get("year");
        //Beware: if a title doesn't have a start_year attribute, then it cannot be included in any range date
        Assertions.assertTrue(dates2.entrySet().size() <= dates.entrySet().size());
        Assertions.assertEquals(1, dates.get("1981-1990"));
        Assertions.assertEquals(1, dates.get("2001-2010"));
        Assertions.assertEquals(1, dates.get("2011-2020"));
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
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 5);
        Map<String, Long> dates = map.get("year");
        Assertions.assertEquals(2, dates.get("1891-1900"));
        Assertions.assertEquals(1, dates.get("1941-1950"));
        Assertions.assertEquals(1, dates.get("1981-1990"));
        Assertions.assertEquals(1, dates.get("1991-2000"));
        Assertions.assertEquals(2, dates.get("2011-2020"));
    }

    @Test
    public void testAggregationsSearchGenres() {
        //Don't fix anything (but the query)
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(921, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 25);
        Map<String, Long> genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 9);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 9);

        //Pass a genre parameter
        //Fixes the facet for type, so that facet will show all possible genres for titles that match the query
        request = HttpRequest.GET("/search?query=Avengers&genres=comedy");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(354, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 25);
        genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 8);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 5);
        request = HttpRequest.GET("/search?query=Avengers&genres=comedy,action");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(437, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 25);
        genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        Assertions.assertTrue(genres.containsKey("action"));
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 8);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 6);

        //Fix another filter
        request = HttpRequest.GET("/search?query=Avengers&type=movie");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(27, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 10);
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 9);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 8);

        //Fix another filter + the genre
        request = HttpRequest.GET("/search?query=Avengers&type=movie&genres=comedy");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(5, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 10);
        genres = map.get("genres");
        Assertions.assertTrue(genres.containsKey("comedy"));
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 8); //this filter, although it's fixed, is modified by the genres filte
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 2);
    }

    @Test
    public void testAggregationsSearchType() {
        //Don't fix anything (but the query)
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(921, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 9);
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 25);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 9);

        //Fix the type
        request = HttpRequest.GET("/search?query=Avengers&type=movie");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(27, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 9);
        Assertions.assertTrue(map.get("types").containsKey("movie"));
        Assertions.assertTrue(map.get("types").get("movie") == imdbResponse.getTotal());
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 10);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 8);

        request = HttpRequest.GET("/search?query=Avengers&type=movie,videogame,tvEpisode");
        imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(815, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types"));
        Assertions.assertTrue(map.get("types").size() == 9);
        Assertions.assertTrue(map.get("types").containsKey("movie"));
        Assertions.assertTrue(map.get("types").containsKey("videogame"));
        Assertions.assertTrue(map.get("types").containsKey("tvepisode"));
        Assertions.assertTrue(map.containsKey("genres"));
        Assertions.assertTrue(map.get("genres").size() == 24);
        Assertions.assertTrue(map.containsKey("year"));
        Assertions.assertTrue(map.get("year").size() == 9);
    }

    @Test
    public void testAggregationsSearchTitleGenresTypeYear() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Tron&type=videoGame&genres=sci-fi,action,adventure&year=1970/2020");
        ImdbResponse imdbResponse = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse);
        Assertions.assertEquals(13, imdbResponse.getTotal());
        Assertions.assertNotNull(imdbResponse.getItems());
        Assertions.assertNotNull(imdbResponse.getAggregations());
        Map<String, Map<String,Long>> map = imdbResponse.getAggregations();
        Assertions.assertTrue(map.containsKey("types")); //affected by genres & years filters
        Assertions.assertTrue(map.containsKey("genres")); //affected by types and years filters
        Assertions.assertTrue(map.containsKey("year")); //affected by types and genres filters
        Assertions.assertTrue(map.get("types").size() == 6);
        Assertions.assertTrue(map.get("genres").size() == 6);
        Assertions.assertTrue(map.get("year").size() == 4);

        //We take out a filter, the returned results get modified
        //The facet results will be the same as before because it fixed in the previous example -> affected by other filters
        //but itself
        request = HttpRequest.GET("/search?query=Tron&genres=sci-fi,action,adventure&year=1970/2020");
        ImdbResponse imdbResponse2 = client.toBlocking().retrieve(request, ImdbResponse.class);
        Assertions.assertNotNull(imdbResponse2);
        Assertions.assertTrue(imdbResponse.getTotal() <= imdbResponse2.getTotal());
        Assertions.assertEquals(33, imdbResponse2.getTotal());
        Assertions.assertNotNull(imdbResponse2.getItems());
        Assertions.assertNotNull(imdbResponse2.getAggregations());
        Map<String, Map<String,Long>> map2 = imdbResponse2.getAggregations();
        Assertions.assertTrue(map2.containsKey("types")); //affected by genres & years filters
        Assertions.assertTrue(map2.containsKey("genres")); //affected by types and years filters
        Assertions.assertTrue(map2.containsKey("year")); //affected by types and genres filters
        Assertions.assertTrue(map2.get("types").size() == map.get("types").size());
        Assertions.assertTrue(map2.get("genres").size() >= map.get("genres").size());
        Assertions.assertTrue(Long.compare(map2.get("genres").values().stream().reduce(Long::sum).get(),
                map.get("genres").values().stream().reduce(Long::sum).get()) == 1);
        Assertions.assertTrue(map2.get("year").size() >= map.get("year").size());
        Assertions.assertTrue(Long.compare(map2.get("year").values().stream().reduce(Long::sum).get(),
                map.get("year").values().stream().reduce(Long::sum).get()) == 1);
        Assertions.assertTrue(map2.get("types").size() == 6);
        Assertions.assertTrue(map2.get("genres").size() == 16);
        Assertions.assertTrue(map2.get("year").size() == 4);
    }


}
