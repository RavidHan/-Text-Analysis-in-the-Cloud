package Manager.Main;

import Manager.Protocol.AwsProtocol;
import Manager.Protocol.Protocol;
import Manager.Protocol.Request;
import SQS.SQSClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Manager {

//    private static String managerToAppQueueUrl;
//    private static String appToManagerQueueUrl;
//    private static String managerToWorkerQueueUrl;
//    private static String workerToManagerQueueUrl;
//    private static SqsClient sqsClient;
    private final int messagesPerWorker;
    private final ExecutorService executorService;
    private RequestSelector requestSelector;
    private final Supplier<Protocol> protocolFactory;

    public Manager(RequestSelector requestSelector, Supplier<Protocol> protocolFactory, int messagesPerWorker){
        this.messagesPerWorker = messagesPerWorker;
        this.executorService =  Executors.newFixedThreadPool(this.messagesPerWorker);
        this.requestSelector = requestSelector;
        this.protocolFactory = protocolFactory;
    }

    private void cleanExit() {
    }

//    private static void init(int n) {
////        messagesPerWorker = n;
////        sqsClient = SqsClient.builder()
////                .region(Region.US_WEST_2)
////                .build();
////        SQSClass.listQueues(sqsClient);
////        appToManagerQueueUrl = getQueueUrl(SQSClass.appToManagerQueueName);
////        managerToAppQueueUrl = getQueueUrl(SQSClass.managerToAppQueueName);
////        managerToWorkerQueueUrl = getQueueUrl(SQSClass.managerToWorkerQueueName);
////        workerToManagerQueueUrl = getQueueUrl(SQSClass.workerToManagerQueueName );
//        executorService = Executors.newFixedThreadPool(n);
//    }

    public void run(){

    }

    private static boolean handleAppRequest(Message appRequest){
        System.out.println("Printing the application request");
//        if (appRequest.body().equals(SQSClass.terminateMessage)) {
//            return false;
//        }
//        RequestHandler requestHandler = new RequestHandler(appRequest.messageId(), appRequest.body());
//        executorService.execute(requestHandler);
//        requestHandler.sendMessagesToWorkers(managerToWorkerQueueUrl);
        return true;
    }

    private void sendMessagesFromAppRequest(String appReqHtmlUrl){

    }

    private static void createWorkers(){

    }

    private void getResultsFromWorkers(){

    }

    private void createResultHtml(){

    }
}
