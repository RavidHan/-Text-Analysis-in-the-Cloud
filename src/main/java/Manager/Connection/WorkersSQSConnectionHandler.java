package Manager.Connection;

import Manager.Protocol.AwsProtocol;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkersSQSConnectionHandler extends ConnectionHandler {

    public WorkersSQSConnectionHandler(ApplicationEncoderDecoder encoderDecoder, String sendMessageUrl, String getMessageUrl, SqsClient sqsClient, AwsProtocol protocol) {
        super(encoderDecoder, sendMessageUrl, getMessageUrl, sqsClient, protocol);
    }
}
