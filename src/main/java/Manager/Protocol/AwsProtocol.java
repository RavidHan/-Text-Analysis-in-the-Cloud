package Manager.Protocol;

import javafx.util.Pair;
import software.amazon.awssdk.services.sqs.model.Message;

public class AwsProtocol implements Protocol {

    private boolean shouldTerminate;

    @Override
    public Request process(Request req) {
        if (req instanceof AppToManagerRequest){
            if (((AppToManagerRequest) req).isTermination()){
                return null;
            }
            return this.processApplicationRequest(req);
        }
        if (req instanceof ManagerToAppRequest){
            // TODO
            return null;
        }
        if (req instanceof WorkerToManagerRequest){
            return this.processWorkerRequest();
        }
        if (req instanceof  ManagerToWorkerRequest){
            // TODO
            return null;
        }

        return null;
    }

    private Request processWorkerRequest() {
        // TODO
        return null;
    }

    private Request processApplicationRequest(Request req) {
        // TODO
        return null;
    }

    public boolean shouldTerminate(){
        return this.shouldTerminate;
    }
}
