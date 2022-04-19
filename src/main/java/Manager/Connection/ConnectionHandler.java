package Manager.Connection;

import software.amazon.awssdk.services.sqs.model.Message;

public interface ConnectionHandler {
    void sendMessage(Message message);
    Runnable getMessage() throws SqsCommunicationException;
}
