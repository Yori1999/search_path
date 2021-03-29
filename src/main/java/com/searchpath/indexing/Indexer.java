package com.searchpath.indexing;

import java.io.IOException;

public interface Indexer {

    void index(String filename, String separator);

    void updateIndex() throws IOException;

}
