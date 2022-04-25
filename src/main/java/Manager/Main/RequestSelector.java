package Manager.Main;

import Manager.Requests.Request;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestSelector {
    private LinkedBlockingQueue<Request> allRequestsQueue;

    public RequestSelector(){
        this.allRequestsQueue = new LinkedBlockingQueue<>();
    }

    public boolean isEmpty(){
        return allRequestsQueue.isEmpty();
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
