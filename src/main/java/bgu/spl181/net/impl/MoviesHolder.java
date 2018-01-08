package bgu.spl181.net.impl;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MoviesHolder {

    @SerializedName("movies")
    @Expose
    private List<Movie> movies = null;

    public List<Movie> getMovies() {
        return movies;
    }

    public void addMovie(Movie toAdd) {
        movies.add(toAdd);
    }

}