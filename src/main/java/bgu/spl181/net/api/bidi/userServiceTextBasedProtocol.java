package bgu.spl181.net.api.bidi;

import bgu.spl181.net.impl.User;
import bgu.spl181.net.impl.UsersHolder;
import bgu.spl181.net.srv.SharedData;

import java.util.ArrayList;

public abstract class userServiceTextBasedProtocol<T> implements BidiMessagingProtocol<T> {

    protected SharedData DataForAll;
    protected ConnectionsImpl<String> connections;
    protected int connId;
    protected String name;
    protected boolean shouldTerminate= false;
    protected boolean isLogin=false;

    //TODO check if default constructor is okey
    public userServiceTextBasedProtocol(){}

    public userServiceTextBasedProtocol(SharedData shared){
        this.DataForAll=shared;
    }

    //TODO: who calls the stat function

    public void start(int connectionId, Connections<T> connections){
        this.connections=(ConnectionsImpl<String>)connections;
        this.connId=connectionId;
    }

    public void process(T message){
        //TODO signout - how to chane the souldterminate
        ArrayList<String> messageArr=toArray((String) message);//sort the message in an array
        toCommand(messageArr);
    }

    public void toCommand(ArrayList<String> messageArray){
        switch (messageArray.get(0)){
            case "LOGIN":{
                loginCommand(messageArray.get(1), messageArray.get(2));
                break;}
            case "REGISTER":{
                registerCommand(messageArray.get(1), messageArray.get(2), messageArray.get(3));
                break;
            }
            case "SIGNOUT":{
                signoutCommand();
                break;
            }
            case "REQUEST":{
                requestCommand(messageArray);
                break;
            }
        }
    }



    public ArrayList<String> toArray(String message){

        ArrayList<String> arr=new ArrayList<String>();

        while (message!="")
        {
            int ind=message.indexOf("\"");
            int space = message.indexOf(" ");
            if (ind==-1 && space==-1){ // message is a single word (no spaces, no quotation)
                arr.add(message);
                message=""; // saves the rest
            }
            else if ((space != -1 && space < ind) || ind==-1) { // space comes before quotation
                arr.add(message.substring(0, space)); // add to arr
                message=message.substring(space+1); // // saves the rest of the string
            }

            else if ((ind!=-1 && ind<space) || space==-1){ // next word is with quotation
                message=message.substring(ind+1); //cut from first quotation mark
                ind=message.indexOf("\""); // end of quotation
                arr.add(message.substring(0,ind));
                message=message.substring(ind+2); // saves the rest
            }
        }
        return arr;
    }

    public boolean shouldTerminate(){
        return shouldTerminate;
    }

    public void loginCommand(String userName, String password){

        User myUser= DataForAll.getUser(userName);
        if (myUser!=null &&
                myUser.getUsername()==userName &
                        myUser.getPassword()==password &
                        (isLogin==false)){
            connections.send(connId, "ACK login succeeded");
            isLogin=true;
        }
        else
            connections.send(connId, "ERROR login failed");
    }

    protected void registerCommand(String userName, String password, String country){
        if (userName!=null
                && password!=null
                && isLogin==false
                && DataForAll.getUser(userName)==null) {// checks if this user isnt exist

            User newUser=new User(userName, "normal", password, country);
            DataForAll.addUser(newUser);
            this.name=userName;
            connections.send(connId, "ACK register succeeded");

        }
        else {
            connections.send(connId, "ERROR Register failed");
        }
    }

    public void signoutCommand(){
        if (isLogin){
            User myUser=DataForAll.getUser(this.name);
            this.isLogin=false;
            this.shouldTerminate=true;
            connections.send(connId, "ACK signout succeeded");
            connections.disconnect(connId);
        }
        else{
            connections.send(connId, "ERROR signout failed");
        }
    }

    protected void requestCommand(ArrayList<String> messageArray){ }
}