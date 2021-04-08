package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ImdbResponse {

    private long total;
    private ImdbObject[] items;
    private Map<String,Map<String,Long>> aggregations;

    @JsonCreator
    public ImdbResponse(@JsonProperty("total") long total, @JsonProperty("items") ImdbObject[] items,
                        @JsonProperty("aggregations") Map<String,Map<String,Long>> aggregations){
        this.total = total;
        this.items = items;

        this.aggregations = aggregations;
    }

    public ImdbResponse() {}

    public long getTotal() { return total; }

    public ImdbObject[] getItems() { return items; }

    public Map<String,Map<String,Long>> getAggregations(){ return aggregations; }
}
