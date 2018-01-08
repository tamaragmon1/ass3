package bgu.spl181.net.api.bidi;

import bgu.spl181.net.impl.Movie;
import bgu.spl181.net.impl.User;
import bgu.spl181.net.srv.SharedData;
import org.omg.CORBA.DATA_CONVERSION;

import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

public class MovieRentalService extends userServiceTextBasedProtocol {

    public MovieRentalService(SharedData SDP){
        super(SDP);
    }

    public void requestCommand (String[] messageArray){
        if (!isLogin) {
            connections.send(connId, "ERROR "); //todo complete this
            return;
        }
        String Command=messageArray[1];
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
        }
    }

    private void balanceREQ(String[] messageArray){
        if (messageArray[2]=="info"){                                  //REQUEST balance info
            User currUser=DataForAll.getUser(name);
            connections.send(connId, "ACK balance "+currUser.getBalance());
        }
        else if (messageArray[2]=="add"){                              //REQUEST balance add
            String newBalance=DataForAll.BalanceAdd(name,Integer.getInteger(messageArray[3]));
            connections.send(connId,"ACK balance "+newBalance+" added "+messageArray[3]);
        }
    }

    private void infoREQ(String[] messageArray){
        if (messageArray[2]!=null){                              //if the movie name was given
            Movie thisMovie=DataForAll.getSpecifiedMovie(messageArray[2]);
            if (thisMovie== null)                                //if the movie does not exist
                connections.send(connId,"ERROR request info failed");
            else                                                 //if there was no movie name
                connections.send(connId,"ACK info \""+thisMovie.getName()+"\" "+thisMovie.getAvailableAmount()+" "+thisMovie.getPrice()+" "+bannedCountiesToString(thisMovie));
        }
        else
            connections.send(connId,"ACK info"+getAllMoviesString());
    }


    private void rentREQ(String[] messageArray) {
        String movieName = messageArray[2];
        movieName = movieName.substring(1, movieName.length() - 1);
        Movie toRent = DataForAll.getSpecifiedMovie(movieName);
        User myUser=DataForAll.getUser(name);
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
                    && (DataForAll.decreaseBalance(name, Integer.getInteger(price)))) {
                avail--;
                DataForAll.takeMovie(movieName, avail.toString()); // changes the amount
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
                && DataForAll.getUser(userName)==null) {// checks if this user isnt exist
            int ind=country.indexOf("=");
            country=country.substring(ind+2,country.length()-2); // saves the country name
            User newUser=new User(userName, "normal", password, country);
            DataForAll.addUser(newUser);
            this.name=userName;
            connections.send(connId, "ACK register succeeded");
        }
        else {
            connections.send(connId, "ERROR Register failed");
        }
    }

    private void returnREQ(String[] messageArray){
        HashMap<String,Movie> allMovies=DataForAll.getMovieHolder();
        User thisUser= DataForAll.getUser(name);
        String movieName=messageArray[2];
        //if the user is currently not renting the movie OR the movie doesn't exist
        if (!allMovies.containsKey(movieName) || !thisUser.getMovies().contains(movieName) ){
            connections.send(connId, "ERROR return \"" + movieName + "\" failed");
        }
        else{//if the user has the movie
            DataForAll.returnMovie(name , movieName);
            connections.send(connId,"ACK return \"" + movieName + "\" success");
            Movie thisMovie=DataForAll.getSpecifiedMovie(movieName);
            connections.broadcast("BROADCAST movie \"" + movieName + " " + thisMovie.getAvailableAmount() + " " + thisMovie.getPrice());
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
        HashMap<String,Movie> allMovies= DataForAll.getMovieHolder();
        String ans="";
        for (String currMovie:allMovies.keySet()) {
            ans = ans + " \"" + currMovie + "\"";
        }
        return ans;
    }


}
