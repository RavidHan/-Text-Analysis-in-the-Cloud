package Manager.Connection;

import Manager.Protocol.AppToManagerRequest;
import Manager.Protocol.ManagerToAppRequest;
import Manager.Protocol.Request;
import Manager.Protocol.RequestUnknownException;
import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class ApplicationEncoderDecoder extends EncoderDecoder<S3Location, List<Pair<String, String>>> {

    @Override
    public String encode(Request<S3Location> request) throws RequestUnknownException {
        if (!(request instanceof  ManagerToAppRequest)){
            throw new RequestUnknownException();
        }
        return null;
    }

    @Override
    public AppToManagerRequest decode(Message message) {
        return null;
    }
}
