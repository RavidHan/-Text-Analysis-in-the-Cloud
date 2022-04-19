package Manager.Connection;

import Manager.Protocol.AwsProtocol;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnkownException;
import SQS.SQSClass;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class ApplicationSQSConnectionHandler extends ConnectionHandler {

    public ApplicationSQSConnectionHandler(ApplicationEncoderDecoder encoderDecoder, String sendMessageUrl, String getMessageUrl, SqsClient sqsClient, AwsProtocol protocol) {
        super(encoderDecoder, sendMessageUrl, getMessageUrl, sqsClient, protocol);
    }
}
