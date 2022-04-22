package Manager.Protocol;

import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class ManagerToWorkerRequest extends Request<Pair<String, String>> {
    private String appMessageId;

    @Override
    public void setData(Message message) {

    }
}
