package com.searchpath;

import com.searchpath.entities.Film;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Singleton
public class FileParser {

    //FLEXIBLE
    public List<Film> parseFilms(String filename, String separator){
        List<Film> films = new ArrayList<>();

        try {
            File file = new File(filename);
            Scanner reader = new Scanner(file);

            //TO REUSE
            String line;
            String[] data;
            String id, primaryTitle, titleType;
            String[] genres;
            LocalDate start_year, end_year;

            reader.nextLine(); //Because we're not interested in the first line
            while (reader.hasNextLine()){
                line = reader.nextLine();
                data = line.split(separator);
                id = data[0];
                primaryTitle = data[2];
                titleType = data[1];
                start_year = LocalDate.of(Integer.parseInt(data[5]), Month.JANUARY, 1);
                try {
                    end_year = LocalDate.of(Integer.parseInt(data[6]), Month.JANUARY, 1);
                } catch (NumberFormatException e){
                    end_year = null;
                }
                genres = data[8].split(",");


                films.add( new Film(id, primaryTitle, titleType, genres, start_year, end_year) );
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); //Do something else
        }

        return films;
    }

}
