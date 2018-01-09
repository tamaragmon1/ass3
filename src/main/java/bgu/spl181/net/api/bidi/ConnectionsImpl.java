package bgu.spl181.net.api.bidi;

import bgu.spl181.net.srv.bidi.ConnectionHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ConnectionsImpl<T> implements Connections<T> {

    public HashMap<Integer, ConnectionHandler<T>> ConnectionsMapping = new HashMap<Integer, ConnectionHandler<T>>();

    public boolean send(int connectionId, T msg){
        ConnectionHandler<T> CH = ConnectionsMapping.get(connectionId);
        if (!ConnectionsMapping.containsKey(connectionId))  //if the connection handler is not exist
            return false;
        CH.send(msg);
        System.out.println(msg); //TODO remove!!!!!!!
        return true;
    }

    public void broadcast(T msg){
        Set<Integer> SetOfIDs = ConnectionsMapping.keySet();
        for (Integer iter: SetOfIDs){ // Sends the message for each CH in the map
            send(iter, msg);
        }
    }

    public void disconnect(int connectionId){
        ConnectionsMapping.remove(connectionId);
    }

    public HashMap<Integer, ConnectionHandler<T>> getConnectionsMapping(){
        return ConnectionsMapping;
    }

    public void addConnection (Integer id,ConnectionHandler user){
        ConnectionsMapping.put(id, user);
    }
}
