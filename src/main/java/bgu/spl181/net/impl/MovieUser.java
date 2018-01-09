package bgu.spl181.net.impl;

import java.util.*;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieUser extends User{

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("movies")
    @Expose
    private List<Movie> movies = null;
    @SerializedName("balance")
    @Expose
    private String balance;


    public MovieUser(String username, String type, String password, String country){
        super(username,password,country);
        this.movies=new LinkedList<Movie>();
        this.balance="0";
        this.country=country;
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

}