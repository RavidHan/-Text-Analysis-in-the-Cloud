package Manager.Requests;

import javafx.util.Pair;

public class ManagerToWorkerRequest extends Request<Pair<AnalysisType.AnalysisTypeEnum, String>> {

    private String appMessageId;

    public String getAppMessageId() {
        return this.appMessageId;
    }

    public void setAppMessageId(String id) {
        this.appMessageId = id;
    }
}
