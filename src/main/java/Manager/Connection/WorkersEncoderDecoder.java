package Manager.Connection;

import Manager.Protocol.ManagerToWorkerRequest;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;
import Manager.Protocol.WorkerToManagerRequest;
import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkersEncoderDecoder extends EncoderDecoder<Pair<String, String>, String> {

    @Override
    public String encode(Request<Pair<String, String>> request) throws RequestUnknownException {
        if (!(request instanceof ManagerToWorkerRequest)){
            throw new RequestUnknownException();
        }
        String message = ((ManagerToWorkerRequest) request).getAppMessageId() + " "
                + request.getData().getKey() + " " + request.getData().getValue();
        return message;
    }

    @Override
    public Request<String> decode(Message message) throws RequestUnknownException {
        String[] strings = message.body().split(" ");
        if (strings.length != 1){
            throw new RequestUnknownException();
        }
        WorkerToManagerRequest request = new WorkerToManagerRequest();
        request.setData(strings[0]);
        return request;
    }

}
