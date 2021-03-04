package com.searchpath.searching;

import com.searchpath.entities.FilmResponse;
import com.searchpath.entities.Message;
import org.elasticsearch.action.search.SearchResponse;

public interface SearchingModule {

    Message processQuery(String query);


}
