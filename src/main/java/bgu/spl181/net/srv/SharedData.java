package bgu.spl181.net.srv;

import bgu.spl181.net.impl.User;
import com.google.gson.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class SharedData {
    protected String UsersLocation;
    protected Gson gson;
    protected JsonObject UsersObject;
    protected JsonArray UsersArray;
    protected HashMap<String,User> UserHolder;

    protected ReadWriteLock RWlock=new ReentrantReadWriteLock();
    protected final Lock readLock=RWlock.readLock();
    protected final Lock writeLock=RWlock.writeLock();

    public SharedData(){}
    public SharedData(String Users) {
        try {
            this.UsersLocation = Users;
            gson = new Gson();
            readLock.lock();
            try {
                JsonParser parser = new JsonParser();
                UsersObject = (JsonObject) parser.parse(new FileReader(UsersLocation));
                UsersArray = UsersObject.getAsJsonArray("users");
            }
            finally {
                readLock.unlock();
            }
            this.UserHolder=new HashMap<String,User>();
            for (int i=0;i<UsersArray.size();i++){
                User toAdd=gson.fromJson(UsersArray.get(i),User.class);
                UserHolder.put(toAdd.getUsername(),toAdd);
            }
        }catch (FileNotFoundException e){ e.printStackTrace();}
    }

    public User getUser (String name){
        return UserHolder.get(name);
    }

    public void addUser (User toAdd){
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
        public List<User> users;

        public List<User> getUsers() {
            return users;
        }
    }
}
