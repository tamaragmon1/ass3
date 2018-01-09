package bgu.spl181.net.srv;
import bgu.spl181.net.impl.Movie;
import bgu.spl181.net.impl.MovieUser;
import bgu.spl181.net.impl.User;
import com.google.gson.*;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MoviesSharedData extends SharedData{
    //TODO add Json files

    private String MoviesLocation;
    private JsonObject MoviesObject;
    private JsonArray MoviesArray;
    private HashMap <String,Movie> MovieHolder;
    private HashMap<String,MovieUser> UserHolder;


    public MoviesSharedData(String Movies, String Users) {
        try {
            this.MoviesLocation = Movies;
            this.UsersLocation = Users;
            gson = new Gson();
            readLock.lock();
            try {
                JsonParser parser = new JsonParser();
                UsersObject = (JsonObject) parser.parse(new FileReader(UsersLocation));
                MoviesObject = (JsonObject) parser.parse(new FileReader(MoviesLocation));
                UsersArray = UsersObject.getAsJsonArray("users");
                MoviesArray = MoviesObject.getAsJsonArray("movies");
            }
            finally {
                readLock.unlock();
            }
            this.UserHolder=new HashMap<String,MovieUser>();
            this.MovieHolder=new HashMap<String,Movie>();
            for (int i=0;i<UsersArray.size();i++){
                MovieUser toAdd=gson.fromJson(UsersArray.get(i),MovieUser.class);
                UserHolder.put(toAdd.getUsername(),toAdd);
            }
            for (int i=0;i<MoviesArray.size();i++){
                Movie toAdd=gson.fromJson(MoviesArray.get(i),Movie.class);
                MovieHolder.put(toAdd.getName(),toAdd);
            }
        }catch (FileNotFoundException e){ e.printStackTrace();}

    }

    /////////////////////////// movies //////////////////////////////////////


    public Movie getSpecifiedMovie(String Moviename){
        return MovieHolder.get(Moviename);
    }

    public HashMap <String,Movie> getMovieHolder(){
        return MovieHolder;
    }

    public void takeMovie(String toRent, String newAmount){
        MovieHolder.get(toRent).setAvailableAmount(newAmount);
        updateMovies();
    }

    public void returnMovie(String userName , String movieName){
        getUser(userName).getMovies().remove(movieName);
        Integer newAvailableAmount = Integer.getInteger(getSpecifiedMovie(movieName).getAvailableAmount());
        newAvailableAmount=newAvailableAmount+1;
        getSpecifiedMovie(movieName).setAvailableAmount(newAvailableAmount.toString());
        updateMovies();
    }

    public void addMovie(String movieName,Integer amount,Integer price, List<String> messageArray){
        String nextID=getNextMovieID();
        Movie toInsert= new Movie(getNextMovieID(), movieName, amount.toString(), price.toString(), messageArray);
        MovieHolder.put(movieName,toInsert);
        updateMovies();
    }

    public String getNextMovieID(){
        Integer maxID=1;
        for (String currMovie : MovieHolder.keySet()){
            Integer currID=Integer.getInteger(MovieHolder.get(currMovie).getId());
            if (currID>maxID){
                maxID=currID;
            }
        }
        maxID=maxID+1;
        return maxID.toString();
    }

    public void changePrice (String movie,String price){
        MovieHolder.get(movie).setPrice(price);
        updateMovies();
    }

    protected void updateMovies() {
        writeLock.lock();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer write;
            try {
                MovieList Movies = new MovieList();
                write = new FileWriter("Database/Movies.json");
                Movies.getMovies().addAll(MovieHolder.values());
                gson.toJson(Movies, write);
                write.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    private class MovieList{
        public List<Movie> movies;

        public List<Movie> getMovies() {
            return movies;
        }
    }

    ////////////////////////////////////// Users ///////////////////////////////////

    public MovieUser getUser (String name){
        return UserHolder.get(name);
    }

    public String BalanceAdd (String name,Integer Amount){
        Integer Balance =Integer.getInteger(UserHolder.get(name).getBalance());
        Balance=Balance+Amount;
        UserHolder.get(name).setBalance(Balance.toString());
        updateUsers();
        return Balance.toString();
    }

    public boolean decreaseBalance (String userName, Integer Amount){
        Integer Balance =Integer.getInteger(UserHolder.get(userName).getBalance());
        Balance=Balance-Amount;
        if (Balance<0)
            return false;
        else {
            UserHolder.get(userName).setBalance(Balance.toString());
            updateUsers();
            return true;
        }
    }

    public void addUser (MovieUser toAdd){
        String Uname= toAdd.getUsername();
        UserHolder.put(Uname, toAdd);
        updateUsers();
    }

    protected void updateUsers() {
        writeLock.lock();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer write;
            try {
                UserList Users = new UserList();
                write = new FileWriter("Database/Users.json");
                Users.getUsers().addAll(UserHolder.values());
                gson.toJson(Users, write);
                write.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    private class UserList{
        public List<MovieUser> users=new LinkedList<MovieUser>();

        public List<MovieUser> getUsers() {
            return users;
        }
    }
}
