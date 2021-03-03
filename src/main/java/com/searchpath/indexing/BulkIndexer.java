package com.searchpath.indexing;

import com.searchpath.ClientFactory;
import com.searchpath.FileParser;
import com.searchpath.entities.Film;
import com.searchpath.entities.MovieType;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.Nullable;
import javax.inject.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Singleton
public class BulkIndexer implements Indexer {

    //@Inject FileParser fileParser;

    //@Inject ClientFactory clientFactory;

    @Override
    public void index(String filename, String separator) {
        //get all the movies
        FileParser fileParser = new FileParser();
        List<Film> filmList = fileParser.parseFilms("src/main/resources/" + filename, separator);
        int filmListSize = filmList.size();
        ClientFactory clientFactory = new ClientFactory();
        RestHighLevelClient client = clientFactory.getClient();

        //INDEX IN BULKS
        int processSize = 100000;
        int counter = 0;

        Film film; Map<String, Object> jsonMap;

        BulkRequest bulk = new BulkRequest();
        System.out.println(bulk.hashCode());

        for (int i = 0; i < filmListSize; i++){
            film = filmList.get(i);
            jsonMap = jsonMapping(film.getId(), film.getPrimaryTitle(), film.getTitleType(), film.getGenres(), film.getStart_year(), film.getEnd_year());
            bulk.add(new IndexRequest("imdb").id(film.getId()).source(jsonMap));
            counter++;
            if ( counter == processSize || i == filmListSize-1){
                try {
                    client.bulk(bulk, RequestOptions.DEFAULT);

                } catch (IOException e){
                    e.printStackTrace(); //Just to test
                }
                bulk.requests().clear();
                counter = 0;
            }
        }

        System.out.println("FINISHED INDEXING PROCESS");
    }

    private Map<String, Object> jsonMapping(String id, String primaryTitle, String titleType,
                                            String[] genres, @Nullable LocalDate start_year, @Nullable LocalDate end_year){
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", id);
        jsonMap.put("title", primaryTitle);
        jsonMap.put("titleType", titleType);
        jsonMap.put("genres", genres);
        jsonMap.put("start_year", start_year);
        jsonMap.put("end_year", end_year);
        return jsonMap;
    }

}
