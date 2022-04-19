package Manager.Connection;

import Manager.Protocol.ManagerToWorkerRequest;
import Manager.Protocol.Request;
import Manager.Protocol.WorkerToManagerRequest;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkersEncoderDecoder implements EncoderDecoder {
    @Override
    public WorkerToManagerRequest decode(Message message) {
        return null;
    }

    @Override
    public ManagerToWorkerRequest encode(Message message) {
        return null;
    }
}
