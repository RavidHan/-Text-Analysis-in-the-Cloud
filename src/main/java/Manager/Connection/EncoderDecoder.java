package Manager.Connection;

import Manager.Protocol.Request;
import software.amazon.awssdk.services.sqs.model.Message;

public interface EncoderDecoder {
    Request encode(Message message);
    String decode(Request request);
}
