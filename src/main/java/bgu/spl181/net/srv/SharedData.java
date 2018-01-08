package bgu.spl181.net.srv;
import bgu.spl181.net.impl.Movie;
import bgu.spl181.net.impl.User;
import bgu.spl181.net.impl.MoviesHolder;
import bgu.spl181.net.impl.UsersHolder;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.HashMap;

public class SharedData {
    //TODO add Json files

    private String MoviesLocation;
    private String UsersLocation;
    private Gson gson;
    private JsonObject UsersObject;
    private JsonObject MoviesObject;
    private JsonArray UsersArray;
    private JsonArray MoviesArray;
    private HashMap <String,User> UserHolder;
    private HashMap <String,Movie> MovieHolder;

    public SharedData(String Movies, String Users) {
        try {
            this.MoviesLocation = Movies;
            this.UsersLocation = Users;
            gson = new Gson();
            JsonParser parser = new JsonParser();
            UsersObject=(JsonObject)parser.parse(new FileReader(UsersLocation));
            MoviesObject=(JsonObject)parser.parse(new FileReader(MoviesLocation));
            UsersArray=UsersObject.getAsJsonArray("users");
            MoviesArray=MoviesObject.getAsJsonArray("movies");
            for (int i=0;i<UsersArray.size();i++){
                User toAdd=gson.fromJson(UsersArray.get(i),User.class);
                UserHolder.put(toAdd.getUsername(),toAdd);
            }
            for (int i=0;i<MoviesArray.size();i++){
                Movie toAdd=gson.fromJson(MoviesArray.get(i),Movie.class);
                MovieHolder.put(toAdd.getName(),toAdd);
            }
        }catch (FileNotFoundException e){ e.printStackTrace();}

    }

    public User getUser (String name){
        return UserHolder.get(name);
    }

    public Movie getSpecifiedMovie(String Moviename){
        return MovieHolder.get(Moviename);
    }

    public HashMap <String,Movie> getMovieHolder(){
        return MovieHolder;
    }

    public String BalanceAdd (String name,Integer Amount){
        Integer Balance =Integer.getInteger(UserHolder.get(name).getBalance());
        Balance=Balance+Amount;
        UserHolder.get(name).setBalance(Balance.toString());
        updateJson();
        return Balance.toString();
    }

    public boolean decreaseBalance (String userName, Integer Amount){
        Integer Balance =Integer.getInteger(UserHolder.get(userName).getBalance());
        Balance=Balance-Amount;
        if (Balance<0)
            return false;
        else {
            UserHolder.get(userName).setBalance(Balance.toString());
            updateJson();
            return true;
        }
    }

    public void addUser (User toAdd){
        String Uname= toAdd.getUsername();
        UserHolder.put(Uname, toAdd);
        updateJson();
    }

    public void takeMovie(String toRent, String newAmount){
        MovieHolder.get(toRent).setAvailableAmount(newAmount);
        updateJson();
    }

    public void returnMovie(String userName , String movieName){
        getUser(userName).getMovies().remove(movieName);
        Integer newAvailableAmount = Integer.getInteger(getSpecifiedMovie(movieName).getAvailableAmount());
        newAvailableAmount=newAvailableAmount+1;
        getSpecifiedMovie(movieName).setAvailableAmount(newAvailableAmount.toString());
        updateJson();
    }

    public void updateJson(){}




}
