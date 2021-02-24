package com.searchpath.entities;

public class Message {
    private String query, cluster_name;

    public Message(String query, String cluster_name){
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
