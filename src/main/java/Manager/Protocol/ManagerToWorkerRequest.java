package Manager.Protocol;

import javafx.util.Pair;
import sun.plugin2.message.Message;

public class ManagerToWorkerRequest extends Request<Pair<String, String>> {
    private String appMessageId;

    @Override
    public void setData(Message message) {

    }
}
