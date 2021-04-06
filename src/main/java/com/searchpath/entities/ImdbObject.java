package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class ImdbObject {

    private String id, title, type, start_year, end_year;
    private String[] genres;
    private Integer num_votes;
    private Double average_rating;

    @JsonCreator
    public ImdbObject(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("genres") String[] genres,
                      @JsonProperty("type") String type, @JsonProperty("start_year") String start_year,
                      @JsonProperty("end_year") String end_year, @JsonProperty("average_rating") @Nullable Double average_rating,
                      @JsonProperty("num_votes") @Nullable Integer num_votes){
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

    @Nullable
    public Integer getNum_votes() { return num_votes; }

    @Nullable
    public Double getAverage_rating() { return average_rating; }
}
