package Manager.Protocol;

import Manager.Connection.S3Location;
import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class WorkerToManagerRequest extends Request<Pair<S3Location, String>> {

    @Override
    public void setData(Message message) {

    }
}
