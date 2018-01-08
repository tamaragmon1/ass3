package bgu.spl181.net.impl;

import java.util.HashMap;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsersHolder {

    @SerializedName("users")
    @Expose
    private HashMap<String,User> users = null;

    public HashMap<String,User> getUsersMap() {
        return users;
    }

    public void addUsers(String connId,User toAdd) {
        users.put(connId, toAdd);
    }

    public boolean isUserExist(String connId){
        if (users.containsKey(connId))
            return false;
        else
            return true;
    }

    public User getSpecifiedUser(String user){
        return users.get(user);
    }

}