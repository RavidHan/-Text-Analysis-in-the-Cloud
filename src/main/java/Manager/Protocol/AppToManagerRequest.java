package Manager.Protocol;

import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * The Data here represents the request from the Local application.
 * Each file and analysing method is represented as a Pair<String htmlFile, String analysingMethod>.
 * The request is a list of the pairs described above.
 */
public class AppToManagerRequest extends Request<URL> {

    private boolean terminationMessage;
    private String id;

    public void setTerminationMessage(boolean terminationMessage) {
        this.terminationMessage = terminationMessage;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public boolean isTermination() {
        return this.terminationMessage;
    }
}
