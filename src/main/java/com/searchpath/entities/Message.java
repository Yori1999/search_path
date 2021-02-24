package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

    private String query, cluster_name;

    @JsonCreator
    public Message(@JsonProperty("query") String query, @JsonProperty("cluster_name") String cluster_name){
        this.query = query;
        this.cluster_name = cluster_name;
    }

    public String getQuery(){
        return query;
    }

    public String getCluster_name(){
        return cluster_name;
    }



}
