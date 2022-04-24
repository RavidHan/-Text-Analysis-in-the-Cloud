package Manager.Connection;

import Manager.Main.RequestSelector;
import Manager.Requests.Request;
import Manager.Requests.RequestUnknownException;
import SQS.SQSClass;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.URL;
import java.util.List;

public class SQSConnectionHandler extends ConnectionHandler {

    private ApplicationEncoderDecoder encoderDecoder;
    private String sendMessageUrl;
    private String getMessageUrl;
    private SqsClient sqsClient;
    private RequestSelector requestSelector;
    private boolean active;

    public SQSConnectionHandler(ApplicationEncoderDecoder encoderDecoder, RequestSelector requestSelector, String sendMessageName, String getMessageName, SqsClient sqsClient) {
        this.encoderDecoder = encoderDecoder;
        this.sendMessageUrl = this.getQueueUrl(sendMessageName);
        this.getMessageUrl = this.getQueueUrl(getMessageName);
        this.sqsClient = sqsClient;
        this.requestSelector = requestSelector;
        this.active = true;
    }

    private String getQueueUrl(String queueName) {
        String queueUrl = SQSClass.getQueueByName(this.sqsClient, queueName);
        // Create queue if not exist
        if (queueUrl == null) {
            queueUrl = SQSClass.createQueue(this.sqsClient, queueName);
        }
        return queueUrl;
    }

    @Override
    public String sendMessage(Request request) throws RequestUnknownException {
            try {
                String message = this.encoderDecoder.encode(request);
                return SQSClass.sendMessageFromString(this.sqsClient, this.sendMessageUrl, message);
            } catch (RequestUnknownException | Exception e){
                e.printStackTrace();
            }
        return null;
    }

    @Override
    public void listener() {
            while (true) {
                if (Thread.interrupted()){
                    this.terminate();
                    return;
                }
                List<Message> appMessages = SQSClass.receiveOneMessage(this.sqsClient, this.getMessageUrl);
                if (appMessages == null){
                    System.err.println("Error with receiving messages in SQS: " + this.getMessageUrl);
                    return;
                }
                if (appMessages.isEmpty()){
                    continue;
                }
                Request<URL> req = this.encoderDecoder.decode(appMessages.get(0));
                if (req != null) {
                    this.requestSelector.putMessage(req);
                    SQSClass.deleteMessages(this.sqsClient, this.getMessageUrl, appMessages);
                }
            }
    }

    @Override
    public void terminate(){
        this.active = false;
        SQSClass.deleteQueue(sqsClient, this.sendMessageUrl);
        SQSClass.deleteQueue(sqsClient, this.getMessageUrl);
    }

    @Override
    public void run() {
        this.listener();
    }
}
