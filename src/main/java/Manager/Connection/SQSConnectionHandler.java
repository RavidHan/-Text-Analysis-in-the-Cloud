package Manager.Connection;

import Manager.Main.RequestSelector;
import Manager.Protocol.AwsProtocol;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;
import SQS.SQSClass;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class SQSConnectionHandler extends ConnectionHandler {

    protected ApplicationEncoderDecoder encoderDecoder;
    protected String sendMessageUrl;
    protected String getMessageUrl;
    protected SqsClient sqsClient;
    protected AwsProtocol protocol;
    protected RequestSelector requestSelector;

    public SQSConnectionHandler(ApplicationEncoderDecoder encoderDecoder, String sendMessageUrl, String getMessageUrl, SqsClient sqsClient, AwsProtocol protocol) {
        this.encoderDecoder = encoderDecoder;
        this.sendMessageUrl = sendMessageUrl;
        this.getMessageUrl = getMessageUrl;
        this.sqsClient = sqsClient;
        this.protocol = protocol;
    }

    @Override
    public void sendMessage(Request request) throws RequestUnknownException {
            try {
                String message = this.encoderDecoder.encode(request);
                SQSClass.sendMessageFromString(this.sqsClient, this.sendMessageUrl, message);
            } catch (RequestUnknownException | Exception e){
                e.printStackTrace();
            }
    }

    @Override
    public void listener() {

            while (true) {
                List<Message> appMessages = SQSClass.receiveOneMessage(this.sqsClient, this.getMessageUrl);
                if (appMessages.isEmpty()){
                    continue;
                }
                Request req = this.encoderDecoder.decode(appMessages.get(0));
                if (req != null) {
                    requestSelector.putMessage(req);
                    SQSClass.deleteMessages(this.sqsClient, this.getMessageUrl, appMessages);
                }
            }

    }
}
