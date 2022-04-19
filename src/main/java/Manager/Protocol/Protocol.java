package Manager.Protocol;

public interface Protocol {
    /**
     * process the given message
     * @param msg the received message
     * @return the response to send or null if no response is expected by the client
     */
    Request process(Request req) throws RequestUnkownException;

    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
