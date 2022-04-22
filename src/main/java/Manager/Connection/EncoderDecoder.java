package Manager.Connection;

import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;
import software.amazon.awssdk.services.sqs.model.Message;

public abstract class EncoderDecoder<T, V> {
    public abstract String encode(Request<T> request) throws RequestUnknownException;
    public abstract Request<V> decode(Message message);
}
