package Manager.Main;

import Manager.Connection.ApplicationEncoderDecoder;
import Manager.Connection.SQSConnectionHandler;
import Manager.Job.WorkerExecutor;
import Manager.Protocol.AwsProtocol;
import SQS.SQSClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class ManagerMain {

    public static void main(String[] args) {

        /**if (args.length < 1) {
         System.out.println("Missing arguments");
         return;
         }*/

        RequestSelector requestSelector = new RequestSelector();
        int messagesPerWorker = 10;
        String sendAppMessagesSQSName = "sendAppMessagesSQS";
        String getAppMessagesName = "getAppMessagesSQS";
        String sendWorkerMessagesSQSName = "sendWorkerMessagesSQS";
        String getWorkerMessagesName = "getWorkerMessagesSQS";

        System.out.println("Initializing Manager!");

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();
        SQSConnectionHandler appSQSConnectionHandler = new SQSConnectionHandler(
                new ApplicationEncoderDecoder(),
                requestSelector,
                sendAppMessagesSQSName,
                getAppMessagesName,
                sqsClient);
        SQSConnectionHandler workerSQSConnectionHandler = new SQSConnectionHandler(
                new ApplicationEncoderDecoder(),
                requestSelector,
                sendWorkerMessagesSQSName,
                getWorkerMessagesName,
                sqsClient);
        WorkerExecutor workerExecutor = new WorkerExecutor(sendWorkerMessagesSQSName, getWorkerMessagesName, sqsClient, messagesPerWorker);
        Manager manager = new Manager(
                requestSelector,
                () -> new AwsProtocol(appSQSConnectionHandler, workerSQSConnectionHandler, workerExecutor),
                10);


        System.out.println("Starting manager applications listener loop!");

        Thread appConnectionThread = new Thread(appSQSConnectionHandler);
        Thread workerConnectionThread = new Thread(workerSQSConnectionHandler);
        Thread managerThread = new Thread(manager);
        appConnectionThread.start();
        workerConnectionThread.start();
        managerThread.start();

        try {
            managerThread.join();

            appConnectionThread.join();
            workerConnectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            SQSClass.deleteSQSQueue(sqsClient, getAppMessagesName);
            SQSClass.deleteSQSQueue(sqsClient, sendAppMessagesSQSName);
            SQSClass.deleteSQSQueue(sqsClient, getWorkerMessagesName);
            SQSClass.deleteSQSQueue(sqsClient, sendWorkerMessagesSQSName);
            workerExecutor.deleteJobExecutors();
        }


        System.out.println("Manager closed!");

//        SendMessageRequest messageRequest = SendMessageRequest.builder()
//                .queueUrl(url)
//                .messageBody("Timeout Testing")
//                .delaySeconds(5)
//                .build();
//        String msgID = sqsClient.sendMessage(messageRequest).messageId();
//        System.out.println();
    }
}
