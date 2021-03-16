package com.searchpath.searching;

import com.searchpath.entities.ImdbDocument;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.entities.Message;
import org.elasticsearch.action.search.SearchResponse;

public interface SearchingModule {

    Message processQuery(String query);

    ImdbResponse processTitleQuery(String query);

    ImdbResponse processTitleAndTypeQuery(String query);

    ImdbResponse processQuery(String query, String genre, String type, String year);

}
