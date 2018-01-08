package bgu.spl181.net.api.bidi;

import bgu.spl181.net.impl.User;
import bgu.spl181.net.impl.UsersHolder;
import bgu.spl181.net.srv.SharedData;

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
        String[] messageArr=toArray((String) message);//sort the message in an array
        toCommand(messageArr);

    }

    public void toCommand(String[] messageArray){
        switch (messageArray[0]){
            case "LOGIN":{
                loginCommand(messageArray[1], messageArray[2]);
                break;}
            case "REGISTER":{
                registerCommand(messageArray[1], messageArray[2], messageArray[3]);
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



    public String[] toArray(String message){
        String[] arr=new String[10];
        int i=0;
        while (message!=""){
            int space=message.indexOf(" "); // position of the next space
            if (space!=-1) {
                arr[i] = message.substring(0, space); //Extract next word
                i++;
                message=message.substring(space+1); // saves the rest of the string
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

    protected void requestCommand(String[] messageArray){ }
}