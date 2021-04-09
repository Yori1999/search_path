package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class ImdbObject {

    private String id, title, type, start_year, end_year, original_title, runtime_minutes;
    private String[] genres;
    private Integer num_votes;
    private Double average_rating;
    private Boolean is_adult;

    public ImdbObject(){ }

    @JsonCreator
    public ImdbObject(@JsonProperty("id") String id,
                      @JsonProperty("title") String title,
                      @JsonProperty("original_title") String original_title,
                      @JsonProperty("genres") String[] genres,
                      @JsonProperty("type") String type,
                      @JsonProperty("start_year") String start_year,
                      @JsonProperty("end_year") String end_year,
                      @JsonProperty("average_rating") @Nullable Double average_rating,
                      @JsonProperty("num_votes") @Nullable Integer num_votes,
                      @JsonProperty("runtime_minutes") String runtime_minutes,
                      @JsonProperty("is_adult") Boolean is_adult){
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.type = type;
        this.start_year = start_year;
        this.end_year = end_year;
        this.average_rating = average_rating;
        this.num_votes = num_votes;
        this.original_title = original_title;
        this.is_adult = is_adult;
        this.runtime_minutes = runtime_minutes;
    }

    public ImdbObject(String id,
                      String title,
                      String[] genres,
                      String type,
                      String start_year,
                      String end_year,
                      Double average_rating,
                      Integer num_votes){
        this(id, title, null, genres, type, start_year, end_year, average_rating, num_votes, null, null);
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

    public String getOriginal_title() { return original_title; }

    public String getRuntime_minutes() { return runtime_minutes; }

    public Boolean isIs_adult() { return is_adult; }
}
