package com.searchpath.entities;

public class ImdbDocument {

    private String tconst, primaryTitle;
    private String titleType;
    private String genres;
    private String start_year;
    private String end_year;
    private String originalTitle;
    private String isAdult;
    private String runtimeMinutes;
    private double averageRating;
    private int numVotes;

    public ImdbDocument(String tconst, String primaryTitle, String titleType, String originalTitle, String isAdult,
                        String genres, String startYear, String endYear, String runtimeMinutes) {
        this.tconst = tconst;
        this.primaryTitle = primaryTitle;
        this.titleType = titleType;
        this.originalTitle = originalTitle;
        this.isAdult = isAdult;
        this.genres = genres;
        this.start_year = startYear;
        this.end_year = endYear;
        this.runtimeMinutes = runtimeMinutes;
    }

    public ImdbDocument(String tconst, String primaryTitle, String titleType, String originalTitle, String isAdult,
                        String genres, String startYear, String endYear, String runtimeMinutes, double averageRating,
                        int numVotes) {
        this.tconst = tconst;
        this.primaryTitle = primaryTitle;
        this.titleType = titleType;
        this.originalTitle = originalTitle;
        this.isAdult = isAdult;
        this.genres = genres;
        this.start_year = startYear;
        this.end_year = endYear;
        this.runtimeMinutes = runtimeMinutes;
        this.averageRating = averageRating;
        this.numVotes = numVotes;
    }


    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public String getTitleType() {
        return titleType;
    }

    public String getGenres() {
        return genres;
    }

    public String getStart_year() {
        return start_year;
    }

    public String getEnd_year() {
        return end_year;
    }

    public String getTconst() {
        return tconst;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getIsAdult() {
        return isAdult;
    }

    public String getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public double getAverageRating() { return averageRating; }

    public int getNumVotes() { return numVotes; }

    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public void setNumVotes(int numVotes) { this.numVotes = numVotes; }
}
