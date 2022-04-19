package Manager.Connection;

import SQS.SQSClass;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class ApplicationSQSConnectionHandler implements ConnectionHandler {

    private SqsClient sqsClient;
    private String getMessageUrl;
    private String sendMessageUrl;

    public ApplicationSQSConnectionHandler(SqsClient sqsClient, String getMessageSqsUrl, String sendMessageSqsUrl){
        this.sqsClient = sqsClient;
        this.getMessageUrl = getMessageSqsUrl;
        this.sendMessageUrl = sendMessageSqsUrl;
    }
    @Override
    public void sendMessage(Message message) {

    }

    @Override
    public Runnable getMessage(){
        return null;
    }
}
