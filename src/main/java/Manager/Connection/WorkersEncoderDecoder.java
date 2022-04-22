package Manager.Connection;

import Manager.Protocol.ManagerToWorkerRequest;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;
import Manager.Protocol.WorkerToManagerRequest;
import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkersEncoderDecoder extends EncoderDecoder<Pair<String, String>, Pair<S3Location, String>> {

    @Override
    public String encode(Request<Pair<String, String>> request) throws RequestUnknownException {
        if (!(request instanceof ManagerToWorkerRequest)){
            throw new RequestUnknownException();
        }
        return null;
    }

    @Override
    public WorkerToManagerRequest decode(Message message) {
        return null;
    }

}
