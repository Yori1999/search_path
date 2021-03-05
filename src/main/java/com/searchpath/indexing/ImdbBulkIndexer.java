package com.searchpath.indexing;

import com.searchpath.ClientFactory;
import com.searchpath.FileParser;
import com.searchpath.entities.ImdbDocument;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import javax.inject.*;
import java.io.IOException;
import java.util.*;

@Singleton
public class ImdbBulkIndexer implements Indexer {

    //@Inject FileParser fileParser;

    //@Inject ClientFactory clientFactory;

    @Override
    public void index(String filename, String separator) {
        //get all the movies
        FileParser fileParser = new FileParser();
        List<ImdbDocument> filmList = fileParser.parseFilms("src/main/resources/" + filename, separator);
        int filmListSize = filmList.size();
        ClientFactory clientFactory = new ClientFactory();
        RestHighLevelClient client = clientFactory.getClient();

        //INDEX IN BULKS
        int processSize = 10000;
        int counter = 0;

        ImdbDocument film; Map<String, Object> jsonMap;

        BulkRequest bulk = new BulkRequest();

        for (int i = 0; i < filmListSize; i++){
            counter++;
            System.out.println(counter);
            film = filmList.get(i);
            jsonMap = jsonMapping(film.getTconst(), film.getTitleType(), film.getPrimaryTitle(), film.getOriginalTitle(),
                    film.getIsAdult(), film.getStart_year(), film.getEnd_year(), film.getRuntimeMinutes(), film.getGenres());
            bulk.add(new IndexRequest("imdb").id(film.getTconst()).source(jsonMap));
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

    private Map<String, Object> jsonMapping(String tconst, String titleType, String primaryTitle, String originalTitle,
                                            String isAdult, String startYear, String endYear, String runtimeMinutes,
                                            String genres){
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("tconst", tconst);
        jsonMap.put("titleType", titleType);
        jsonMap.put("primaryTitle", primaryTitle);
        jsonMap.put("originalTitle", originalTitle);
        jsonMap.put("isAdult", isAdult);
        jsonMap.put("startYear", startYear);
        jsonMap.put("endYear", endYear);
        jsonMap.put("runtimeMinutes", runtimeMinutes);
        jsonMap.put("genres", genres);
        return jsonMap;
    }

}
