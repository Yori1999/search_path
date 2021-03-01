package com.searchpath;

import com.searchpath.entities.Message;

public interface SearchingModule {

    Message processQuery(String query);

}
