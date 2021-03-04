package com.searchpath.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.annotation.Nullable;
import java.time.LocalDate;

//OLD CLASS, NOT USED ANYMORE UNTIL FURTHER NOTICED
public class FilmOld {

    private String id, primaryTitle;
    private String titleType; //We have an enum just to make sure, but if we're directly parsing...
    private String[] genres;
    private LocalDate start_year;
    @Nullable private LocalDate end_year;

    @JsonCreator
    public FilmOld(String id, String primaryTitle, String titleType,
                   String[] genres, LocalDate start_year, @Nullable LocalDate end_year) {
        this.id = id;
        this.primaryTitle = primaryTitle;
        this.titleType = titleType;
        this.genres = genres;
        this.start_year = start_year;
        this.end_year = end_year;
    }


    public String getId() {
        return id;
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public String getTitleType() {
        return titleType;
    }

    public String[] getGenres() {
        return genres;
    }

    public LocalDate getStart_year() {
        return start_year;
    }

    @Nullable
    public LocalDate getEnd_year() {
        return end_year;
    }
}
