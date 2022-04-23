package Manager.Protocol;

import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class ManagerToWorkerRequest extends Request<Pair<String, String>> {

    private String appMessageId;
    private int responsesAmount;

    public String getAppMessageId() {
        return this.appMessageId;
    }

    public void setAppMessageId(String id) {
        this.appMessageId = id;
    }

    public void setResponsesAmount(int responsesAmount) {
        this.responsesAmount = responsesAmount;
    }

    public int getResponsesAmount() {
        return this.responsesAmount;
    }
}
