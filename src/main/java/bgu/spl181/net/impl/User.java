package bgu.spl181.net.impl;

import java.util.*;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("movies")
    @Expose
    private List<Movie> movies = null;
    @SerializedName("balance")
    @Expose
    private String balance;
    private boolean isLogin=false;

    public User(String username, String type, String password, String country){ // TODO country?
        this.username=username;
        this.password=password;
        this.movies=new LinkedList<Movie>(); //TODO is initiation needed here?
        this.balance="0";
        this.country=country;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLogedin(){
        return isLogin;
    }

    public void logOut(){
        this.isLogin=false;
    }

    public void logIn(){
        this.isLogin=true;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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