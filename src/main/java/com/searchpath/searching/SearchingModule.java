package com.searchpath.searching;

import com.searchpath.entities.ImdbObject;
import com.searchpath.entities.ImdbResponse;
import com.searchpath.entities.Message;

public interface SearchingModule {

    //TODO: ADD JAVADOC TO SEARCHINGMODULE SIGNATURES

    ImdbResponse processQuery(String query, String genre, String type, String year);


    ImdbResponse processTitleQuery(String query);


    ImdbResponse processTitleAndTypeQuery(String query);

    ImdbObject processTitleId(String query);

    /***
     * Returns a message with the text introduced in the query parameter and the cluster name.
     * As of this current version of the project, for testing purposes
     * @param query The string to be returned as part of the response message
     * @return A message with the query passed as a parameter and the name of the cluster
     */
    Message processQuery(String query);

}
