package Manager;

import SQS.SQSClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {

    private static String managerToAppQueueUrl;
    private static String appToManagerQueueUrl;
    private static String managerToWorkerQueueUrl;
    private static String workerToManagerQueueUrl;
    private static SqsClient sqsClient;
    private static int messagesPerWorker;
    private static ExecutorService executorService;

    public static void main(String[] args) {

        /**if (args.length < 1) {
            System.out.println("Missing arguments");
            return;
        }*/

        System.out.println("Initializing Manager.Manager!");
//        init(Integer.parseInt(args[0]));
        init(2);

        System.out.println("Starting manager applications listener loop!");
        listen();

        System.out.println("Closing Manager.Manager!");
        cleanExit();
//        SendMessageRequest messageRequest = SendMessageRequest.builder()
//                .queueUrl(url)
//                .messageBody("Timeout Testing")
//                .delaySeconds(5)
//                .build();
//        String msgID = sqsClient.sendMessage(messageRequest).messageId();
//        System.out.println();
    }

    private static void cleanExit() {
    }

    private static void init(int n) {
        messagesPerWorker = n;
        sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();
        SQSClass.listQueues(sqsClient);
        appToManagerQueueUrl = getQueueUrl(SQSClass.appToManagerQueueName);
        managerToAppQueueUrl = getQueueUrl(SQSClass.managerToAppQueueName);
        managerToWorkerQueueUrl = getQueueUrl(SQSClass.managerToWorkerQueueName);
        workerToManagerQueueUrl = getQueueUrl(SQSClass.workerToManagerQueueName );
        executorService = Executors.newFixedThreadPool(n);
    }

    private static String getQueueUrl(String queueName) {
        String queueUrl = SQSClass.getQueueByName(sqsClient, queueName);
        // Create queue if not exist
        if (queueUrl == null) {
            queueUrl = SQSClass.createQueue(sqsClient, queueName);
        }
        return queueUrl;
    }

    private static void listen(){

            while (true) {
                createWorkers();
                List<Message> appMessages = SQSClass.receiveMessages(sqsClient, appToManagerQueueUrl);
                if (appMessages == null) {
                    System.out.println("Error is receiving message from app");
                    continue;
                }
                if (appMessages.isEmpty()) {
                    continue;
                }
                if (!handleAppRequest(appMessages.get(0))) {
                    return;
                }
            }
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
