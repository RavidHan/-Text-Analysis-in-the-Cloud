package Manager.Main;

import Manager.Protocol.Request;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestSelector {
    private LinkedBlockingQueue<Request> allRequestsQueue;

    private static class RequestSelectorInstance{
        private static RequestSelector instance = new RequestSelector();
    }

    private RequestSelector(){
        this.allRequestsQueue = new LinkedBlockingQueue<Request>();
    }

    public static RequestSelector getInstance(){
        return RequestSelectorInstance.instance;
    }

    public Request getRequest(){
        return this.allRequestsQueue.poll();
    }

    public void putMessage(Request request){
        while (true) {
            try {
                this.allRequestsQueue.put(request);
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
