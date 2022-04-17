package Manager;

public class RequestHandler {

    private Thread requestThread;
    private String body;
    private String id;

    public RequestHandler(String messageId, String messageBody) {
        id = messageId;
        body = messageBody;
    }
}
