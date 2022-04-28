package Manager.Connection;

import Manager.Requests.*;
import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkersEncoderDecoder extends EncoderDecoder<Pair<AnalysisType.AnalysisTypeEnum, String>, String> {

    @Override
    public String encode(Request<Pair<AnalysisType.AnalysisTypeEnum, String>> request) throws RequestUnknownException {
        if (!(request instanceof ManagerToWorkerRequest)){
            throw new RequestUnknownException();
        }
        return ((ManagerToWorkerRequest) request).getAppMessageId() + "|"
                + request.getData().getKey().toString() + "|" + request.getData().getValue();
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
