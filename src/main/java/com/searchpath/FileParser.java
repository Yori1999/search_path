package com.searchpath;

import com.searchpath.entities.FilmDocument;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Singleton
public class FileParser {

    //FLEXIBLE
    public List<FilmDocument> parseFilms(String filename, String separator){
        List<FilmDocument> films = new ArrayList<>();

        try {
            File file = new File(filename);
            Scanner reader = new Scanner(file);

            //TO REUSE
            String line;
            String[] data;

            String tconst, primaryTitle, originalTitle, isAdult, titleType, startYear, endYear, runtimeMinutes, genres;

            //String[] genres;
            //LocalDate start_year, end_year;
            //int start_year, end_year;

            reader.nextLine(); //Because we're not interested in the first line
            while (reader.hasNextLine()){
                line = reader.nextLine();
                data = line.split(separator);

                tconst = data[0];
                titleType = data[1];
                primaryTitle = data[2];
                originalTitle = data[3];
                isAdult = data[4];
                startYear = data[5];
                endYear = data[6];
                runtimeMinutes = data[7];
                genres = data[8];


                //TRY-CATCH FOR (PREVIOUS) LOCALDATE TYPE
                /*try {
                    start_year = LocalDate.of(Integer.parseInt(data[5]), Month.JANUARY, 1);
                } catch (NumberFormatException e){
                    start_year = null;
                }
                try {
                    end_year = LocalDate.of(Integer.parseInt(data[6]), Month.JANUARY, 1);
                } catch (NumberFormatException e){
                    end_year = null;
                }*/
                //genres = data[8].split(",");

                films.add( new FilmDocument(tconst, primaryTitle, titleType, originalTitle, isAdult, genres,
                        startYear, endYear, runtimeMinutes) );
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); //Do something else
        }

        return films;
    }

}
