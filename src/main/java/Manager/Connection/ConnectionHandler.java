package Manager.Connection;

import Manager.Protocol.AwsProtocol;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnkownException;
import SQS.SQSClass;
import org.omg.PortableServer.THREAD_POLICY_ID;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class ConnectionHandler {

    protected ApplicationEncoderDecoder encoderDecoder;
    protected String sendMessageUrl;
    protected String getMessageUrl;
    protected SqsClient sqsClient;
    protected AwsProtocol protocol;

    public ConnectionHandler(ApplicationEncoderDecoder encoderDecoder, String sendMessageUrl, String getMessageUrl, SqsClient sqsClient, AwsProtocol protocol) {
        this.encoderDecoder = encoderDecoder;
        this.sendMessageUrl = sendMessageUrl;
        this.getMessageUrl = getMessageUrl;
        this.sqsClient = sqsClient;
        this.protocol = protocol;
    }


    public Runnable sendMessage(Request request) {
        return () -> {
            String message = this.encoderDecoder.decode(request);
            SQSClass.sendMessageFromString(this.sqsClient, this.sendMessageUrl, message);
        };
    }

    public Runnable getMessage() {
        return () -> {
            List<Message> appMessages = SQSClass.receiveMessages(this.sqsClient, this.getMessageUrl);
            while (true) {
                if (appMessages.isEmpty()){
                    continue;
                }
                appMessages = SQSClass.receiveMessages(sqsClient, this.getMessageUrl);
                Request req = this.encoderDecoder.encode(appMessages.get(0));
                SQSClass.deleteMessages(this.sqsClient, this.getMessageUrl, appMessages);
                if (req != null) {
                    try {
                        Request ans = this.protocol.process(req);
//                        reactor.update(ans);
                        break;
                    } catch (RequestUnkownException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
