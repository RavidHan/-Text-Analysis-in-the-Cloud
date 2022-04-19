package Manager.Connection;

import Manager.Protocol.AppToManagerRequest;
import Manager.Protocol.ManagerToAppRequest;
import Manager.Protocol.Request;
import software.amazon.awssdk.services.sqs.model.Message;

public class ApplicationEncoderDecoder implements EncoderDecoder {
    @Override
    public AppToManagerRequest decode(Message message) {
        return null;
    }

    @Override
    public ManagerToAppRequest encode(Message message) {
        return null;
    }
}
