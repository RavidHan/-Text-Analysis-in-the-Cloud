package Manager.Main;

import Manager.Protocol.*;
import Manager.Requests.Request;
import Manager.Requests.RequestUnknownException;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class Manager implements Runnable{

//    private static String managerToAppQueueUrl;
//    private static String appToManagerQueueUrl;
//    private static String managerToWorkerQueueUrl;
//    private static String workerToManagerQueueUrl;
//    private static SqsClient sqsClient;
    private final ExecutorService executorService;
    private RequestSelector requestSelector;
    private final Supplier<Protocol> protocolFactory;

    public Manager(RequestSelector requestSelector, Supplier<Protocol> protocolFactory, int threadAmount){
        this.executorService =  Executors.newFixedThreadPool(threadAmount);
        this.requestSelector = requestSelector;
        this.protocolFactory = protocolFactory;
    }

    private void cleanExit() {
    }

    public void run(){
        Boolean finished = false;
        while (true) {
            Request request = requestSelector.getRequest();
            Protocol protocol = protocolFactory.get();
            try {
                executorService.execute(protocol.process(request));
            } catch (RequestUnknownException e) {
                e.printStackTrace();
                continue;
            } catch (NotifyFinishedException e) {
                finished = true;
            }
            if (finished && protocol.shouldTerminate()) {
                break;
            }
        }
    }
}
