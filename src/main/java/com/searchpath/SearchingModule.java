package com.searchpath;

import com.searchpath.entities.Message;
import io.micronaut.http.HttpResponse;

public interface SearchingModule {

    Message processQuery(String query);

}
