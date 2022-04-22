package Manager.Connection;

import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;

public abstract class ConnectionHandler implements Runnable {

    public abstract void listener();
    public abstract void sendMessage(Request request) throws RequestUnknownException;

}
