package Manager.Protocol;

import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;

/**
 * The Data here represents the request from the Local application.
 * Each file and analysing method is represented as a Pair<String htmlFile, String analysingMethod>.
 * The request is a list of the pairs described above.
 */
public class AppToManagerRequest extends Request<List<Pair<String, String>>> {
    private String htmlFile;
    private boolean terminationMessage;
    private Map<String, String> answers;

    @Override
    public void setData(Message message) {

    }

    public boolean isTermination() {
        return this.terminationMessage;
    }
}
