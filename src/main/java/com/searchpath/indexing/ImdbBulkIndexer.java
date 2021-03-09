package com.searchpath.indexing;

import com.searchpath.ClientFactory;
import com.searchpath.FileParser;
import com.searchpath.entities.ImdbDocument;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

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

        ///// CREATE THE INDEX
        /////
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

    private void createIndex(RestHighLevelClient client) throws IOException {
        CreateIndexRequest create = new CreateIndexRequest("imdb");

        create.mapping("{}", XContentType.JSON);
        create.settings("{}", XContentType.JSON);

        client.indices().create(create, RequestOptions.DEFAULT);
    }


    //REDO THIS, the mapping is not correct
    private void createIndexHashMap(RestHighLevelClient client) throws IOException {
        CreateIndexRequest create = new CreateIndexRequest("imdb"); //we tell it to specifically create an index, not create one because we need to index and it doesn't exist

        //Indicate types of the different fields or properties of the documents to index
        Map<String, Object> originalTitle = new HashMap<>();
        originalTitle.put("type", "text");
        originalTitle.put("search_analyzer", "title_search_analyzer");
        originalTitle.put("analyzer", "title_analyzer");

        //Properties has an entry per field/property
        Map<String, Object> properties = new HashMap<>();
        properties.put("originalTitle", originalTitle);

        Map<String, Object> mappings = new HashMap<>();
        mappings.put("properties", properties);
        // Adding settings and mapppings work in the same way, you are
        // pretty much composing JSON using Java Maps.

        //BUILDING THE SEARCH ANALYZER
        Map<String, Object> title_search_analyzer = new HashMap<>();
        title_search_analyzer.put("type", "custom");
        title_search_analyzer.put("tokenizer", "standard");
        title_search_analyzer.put("char_filter", new String[]{"html_strip"});
        title_search_analyzer.put("filter", new String[]{"lowercase", "asciifolding"});
        //BUILDING THE ANALYZER FOR INDEXING
        Map<String, Object> title_analyzer = new HashMap<>();
        title_analyzer.put("type", "custom");
        title_analyzer.put("tokenizer", "standard");
        title_analyzer.put("char_filter", new String[]{"html_strip"});
        title_analyzer.put("filter", new String[]{"lowercase", "asciifolding"});

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("title_search_analyzer", title_search_analyzer);
        analysis.put("title_analyzer", title_analyzer);

        Map<String, Object> settings = new HashMap<>();
        settings.put("analysis", analysis);

        Map<String, Object> mapping = new HashMap<>();
        mapping.put("mappings", mappings);
        mapping.put("settings", settings);

        create.mapping(mapping);

        client.indices().create(create, RequestOptions.DEFAULT);
    }

}
