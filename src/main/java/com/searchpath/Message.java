package com.searchpath;

public class Message {
    public String query, cluster_name;
    public Message(String query, String cluster_name){
        this.query = query;
        this.cluster_name = cluster_name;
    }

}
