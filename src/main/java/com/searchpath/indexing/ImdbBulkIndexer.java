package com.searchpath.indexing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.ClientFactory;
import com.searchpath.FileParser;
import com.searchpath.entities.ImdbDocument;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;

import javax.inject.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Singleton
public class ImdbBulkIndexer implements Indexer {

    @Override
    public void index(String filename, String separator) {
        //Gets all the movies with their corresponding ratings' information if they have it available
        FileParser fileParser = new FileParser();
        List<ImdbDocument> filmList = fileParser.parseFilmsWithRatings("src/main/resources/" + filename, "src/main/resources/dataRatings.tsv", separator);
        int filmListSize = filmList.size();

        ClientFactory clientFactory = new ClientFactory();
        RestHighLevelClient client = clientFactory.getClient();

        ///// CREATE THE INDEX
        try {
            createIndex(client);
            System.out.println("Index creation complete");
        } catch (IOException e){
            e.printStackTrace();
        }

        //INDEX IN BULKS
        int processSize = 10000;
        int counter = 0;
        ImdbDocument film; Map<String, Object> jsonMap;
        BulkRequest bulk = new BulkRequest();
        for (int i = 0; i < filmListSize; i++){
            counter++;
            System.out.println(counter);
            film = filmList.get(i);
            jsonMap = jsonMappingWithRatings(film.getTconst(), film.getTitleType(), film.getPrimaryTitle(), film.getOriginalTitle(),
                    film.getIsAdult(), film.getStart_year(), film.getEnd_year(), film.getRuntimeMinutes(), film.getGenres(),
                    film.getAverageRating(), film.getNumVotes());
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

    private Map<String, Object> jsonMappingWithRatings(String tconst, String titleType, String primaryTitle, String originalTitle,
                                            String isAdult, String startYear, String endYear, String runtimeMinutes,
                                            String genres, double averageRating, int numVotes){
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("tconst", tconst);
        jsonMap.put("titleType", titleType);
        jsonMap.put("primaryTitle", primaryTitle);
        jsonMap.put("originalTitle", originalTitle);
        jsonMap.put("isAdult", isAdult);
        if (startYear.equals("\\N")) startYear = null;
        jsonMap.put("startYear", startYear);
        if (endYear.equals("\\N")) endYear = null;
        jsonMap.put("endYear", endYear);
        jsonMap.put("runtimeMinutes", runtimeMinutes);
        jsonMap.put("genres", genres);
        jsonMap.put("averageRating", averageRating);
        jsonMap.put("numVotes", numVotes);
        return jsonMap;
    }


    private void createIndex(RestHighLevelClient client) throws IOException {
        CreateIndexRequest create = new CreateIndexRequest("imdb");
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> mappingMap = objectMapper.readValue(
                this.getClass().getClassLoader().getResourceAsStream("mappingImdbIndexWithRatings.json")
                , Map.class);
        Map<String, Object> settingsMap = objectMapper.readValue(
                this.getClass().getClassLoader().getResourceAsStream("settingsImdbIndex.json")
                , Map.class);
        create.settings(settingsMap);
        create.mapping(mappingMap);
        client.indices().create(create, RequestOptions.DEFAULT);
    }

}
