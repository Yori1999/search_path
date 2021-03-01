package com.searchpath;

import com.searchpath.entities.Film;
import com.searchpath.entities.MovieType;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public class BulkIndexer {

    @Inject FileParser fileParser;

    public void index(String filename, String separator) {

        //get all the movies
        List<Film> filmList = fileParser.parseFilms(filename, separator);

        //INDEX IN BULKS
        BulkRequest bulk = new BulkRequest();

        //jsonMapping();

        //bulk.add(new IndexRequest("imdb").id(id).source(jsonMap));
        //client.bulk(bulk, RequestOptions.DEFAULT);

    }

    private Map<String, Object> jsonMapping(String id, String primaryTitle, String titleType,
                                            String[] genres, Date start_year, @Nullable Date end_year){
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", id);
        jsonMap.put("title", primaryTitle);
        jsonMap.put("titleType", titleType);
        jsonMap.put("genres", genres);
        jsonMap.put("start_year", start_year);
        jsonMap.put("end_year", end_year)
        return jsonMap;
    }

}
