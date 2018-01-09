package bgu.spl181.net.api.bidi;

import bgu.spl181.net.impl.Movie;
import bgu.spl181.net.impl.MovieUser;
import bgu.spl181.net.srv.MoviesSharedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MovieRentalService<T> extends userServiceTextBasedProtocol<T> {

    public MovieRentalService(MoviesSharedData SDP){
        super(SDP);
    }

    @Override
    protected void requestCommand (ArrayList<String> messageArray){
        if (!isLogin) {
            connections.send(connId, "ERROR request" + ArrayListToString(messageArray) + " failed");
            return;
        }
        String Command=messageArray.get(1);
        switch (Command){
            case "balance":{
                balanceREQ(messageArray);
                break;
            }
            case "info":{
                infoREQ(messageArray);
                break;
            }
            case "rent":{
                rentREQ(messageArray);
                break;
            }
            case "return":{
                returnREQ(messageArray);
                break;
            }
            case "addmovie":{
                addmovieREQ(messageArray);
                break;
            }
            case "remmovie":{
                remmovieREQ(messageArray);
                break;
            }
            case "changeprice":{
                changepriceREQ(messageArray);
                break;
            }
        }
    }

    private void balanceREQ(ArrayList<String> messageArray){
        if (messageArray.get(2)=="info"){                                  //REQUEST balance info
            MovieUser currUser=((MoviesSharedData) DataForAll).getUser(name);
            connections.send(connId, "ACK balance "+currUser.getBalance());
        }
        else if (messageArray.get(2)=="add"){                              //REQUEST balance add
            String newBalance=((MoviesSharedData) DataForAll).BalanceAdd(name,Integer.getInteger(messageArray.get(3)));
            connections.send(connId,"ACK balance "+newBalance+" added "+messageArray.get(3));
        }
    }

    private void infoREQ(ArrayList<String> messageArray){
        if (messageArray.get(2)!=null){                              //if the movie name was given
            Movie thisMovie=((MoviesSharedData) DataForAll).getSpecifiedMovie(messageArray.get(2));
            if (thisMovie== null)                                //if the movie does not exist
                connections.send(connId,"ERROR request info failed");
            else                                                 //if there was no movie name
                connections.send(connId,"ACK info \""+thisMovie.getName()+"\" "+thisMovie.getAvailableAmount()+" "+thisMovie.getPrice()+" "+bannedCountiesToString(thisMovie));
        }
        else
            connections.send(connId,"ACK info"+getAllMoviesString());
    }


    private void rentREQ(ArrayList<String> messageArray) {
        String movieName = messageArray.get(2);
        movieName = movieName.substring(1, movieName.length() - 1);
        Movie toRent = ((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName);
        MovieUser myUser=((MoviesSharedData) DataForAll).getUser(name);
        String country = myUser.getCountry();

        if (toRent == null || // no such movie
                toRent.getBannedCountries().contains(country) || // the movie is banned
                myUser.getMovies().contains(toRent) ) // already rented by user
        {
            connections.send(connId, "ERROR rent \"" + movieName + "\" failed");
            return;
        }
        else {
            Integer avail = Integer.getInteger(toRent.getAvailableAmount()); //No. of available copies
            String price = toRent.getPrice();
            //checks if the movie available & balance is OK
            if ((avail > 0)
                    && (((MoviesSharedData) DataForAll).decreaseBalance(name, Integer.getInteger(price)))) {
                avail--;
                ((MoviesSharedData) DataForAll).takeMovie(movieName, avail.toString()); // changes the amount
                connections.send(connId, "ACK rent \"" + movieName + "\" secceeded");
                connections.broadcast("BROADCAST movie \"" + movieName + "\"" + avail.toString() + price);
            }
            else{
                connections.send(connId, "ERROR rent \"" + movieName + "\" failed");
                return;
            }


        }
    }
    public void registerCommand(String userName, String password, String country){
        if (userName!=null
                && password!=null
                && country!=null
                && isLogin==false
                && ((MoviesSharedData) DataForAll).getUser(userName)==null) {// checks if this user isnt exist
            MovieUser newUser=new MovieUser(userName, "normal", password, country);
            ((MoviesSharedData) DataForAll).addUser(newUser);
            this.name=userName;
            connections.send(connId, "ACK registration succeeded");
        }
        else {
            connections.send(connId, "ERROR registration failed");
        }
    }

    private void returnREQ(ArrayList<String> messageArray){
        HashMap<String,Movie> allMovies=((MoviesSharedData) DataForAll).getMovieHolder();
        MovieUser thisUser= ((MoviesSharedData) DataForAll).getUser(name);
        String movieName=messageArray.get(2);
        //if the user is currently not renting the movie OR the movie doesn't exist
        if (!allMovies.containsKey(movieName) || !thisUser.getMovies().contains(movieName) ){
            connections.send(connId, "ERROR return \"" + movieName + "\" failed");
        }
        else{//if the user has the movie
            ((MoviesSharedData) DataForAll).returnMovie(name , movieName);
            connections.send(connId,"ACK return \"" + movieName + "\" success");
            Movie thisMovie=((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName);
            connections.broadcast("BROADCAST movie \"" + movieName + " " + thisMovie.getAvailableAmount() + " " + thisMovie.getPrice());
        }
    }


    private void addmovieREQ(ArrayList<String> messageArray){
        String movieName=messageArray.get(2);
        Integer amount=Integer.getInteger(messageArray.get(3));
        Integer price=Integer.getInteger(messageArray.get(4));
        MovieUser thisUser=((MoviesSharedData) DataForAll).getUser(name);
        //reasons for failure
        if (thisUser.getType()!="admin" || ((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName)!=null || price<=0 || amount<=0){
            connections.send(connId,"ERROR addmovie failed");
        }
        else{
            ((MoviesSharedData) DataForAll).addMovie(movieName,amount,price, getBannedCountriesList(messageArray));
            connections.send(connId,"ACK addmovie \"" + movieName + "\" success");
            connections.broadcast("BROADCAST movie \"" + movieName + "\" " + ((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName).getAvailableAmount() + " " + price);
        }
    }

    private void remmovieREQ(ArrayList<String> messageArray){
        MovieUser myUser=((MoviesSharedData) DataForAll).getUser(name);
        String movName=messageArray.get(2);
        Movie toRemove = ((MoviesSharedData) DataForAll).getSpecifiedMovie(movName);

        if (!(myUser.getType()).equals("admin") ||  // user is not admin
                toRemove==null ||  // no such movie
                !((toRemove.getAvailableAmount()).equals(toRemove.getTotalAmount()))){ // the movie is rented
            connections.send(connId, "ERROR remmovie failed");
            return;
        }
        else{
            ((MoviesSharedData) DataForAll).getMovieHolder().remove(movName);
            connections.send(connId, "ACK remmovie \"" + movName + "\"secceeded");
            connections.broadcast("BROADCAST movie \"" + movName + "\"removed");
        }

    }

    private void changepriceREQ(ArrayList<String> messageArray){
        String movieName=messageArray.get(2);
        MovieUser thisUser=((MoviesSharedData) DataForAll).getUser(name);
        Integer price=Integer.getInteger(messageArray.get(3));
        if (thisUser.getType()!="admin" || ((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName)==null || price<=0){
            connections.send(connId,"ERROR changeprice failed");
        }
        else{
            ((MoviesSharedData) DataForAll).changePrice(movieName,price.toString());
            connections.send(connId,"ACK changeprice \"" + movieName + "\" success");
            connections.broadcast("BROADCAST movie \"" + movieName + "\" " + getAvailableAmountForMovie(movieName));
        }
    }

    ///////////////////////////////////////////////commands for printing//////////////////////////////////////
    private String bannedCountiesToString(Movie movie){
        List<String> bannedCountries=movie.getBannedCountries();
        String ans="\""+bannedCountries.get(0)+"\"";
        for (int i=1;i<bannedCountries.size();i++){
            ans=ans+" \""+bannedCountries.get(i)+"\"";
        }
        return ans;
    }

    private String getAllMoviesString(){
        HashMap<String,Movie> allMovies= ((MoviesSharedData) DataForAll).getMovieHolder();
        String ans="";
        for (String currMovie:allMovies.keySet()) {
            ans = ans + " \"" + currMovie + "\"";
        }
        return ans;
    }

    private List<String> getBannedCountriesList(ArrayList<String> messageArray){
        List <String> ans = new LinkedList<String>();
        for (int i=5;i<messageArray.size();i++){
            if (messageArray.get(i)!=null)
                ans.add(messageArray.get(i));
        }
        return ans;
    }

    private String getAvailableAmountForMovie (String movieName){
        return ((MoviesSharedData) DataForAll).getSpecifiedMovie(movieName).getAvailableAmount();
    }

    private String ArrayListToString(ArrayList<String> messageArray){
        String ans="";
        for (int i=1;i<messageArray.size();i++){
            ans=ans+ " " + messageArray.get(i);
        }
        return ans;
    }

}
