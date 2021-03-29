package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImdbObject {

    private String id, title, type, start_year, end_year;
    private String[] genres;
    private int num_votes;
    private double average_rating;

    @JsonCreator
    public ImdbObject(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("genres") String[] genres,
                      @JsonProperty("type") String type, @JsonProperty("start_year") String start_year,
                      @JsonProperty("end_year") String end_year, @JsonProperty("average_rating") double average_rating,
                      @JsonProperty("num_votes") int num_votes){
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.type = type;
        this.start_year = start_year;
        this.end_year = end_year;
        this.average_rating = average_rating;
        this.num_votes = num_votes;
    }

    public String getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getStart_year() {
        return start_year;
    }

    public String getEnd_year() {
        return end_year;
    }

    public String[] getGenres() {
        return genres;
    }

    public int getNum_votes() { return num_votes; }

    public double getAverage_rating() { return average_rating; }
}
