import Manager.ManagerCreator;
import SQS.SQSClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class LocalApplication {
    static String managerName = "Manager_EC2";
    static String bucketName = "diamlior321";

    static class ResultEntry{
        public String job;
        public String inputLink;
        public String outputLink;
        public boolean hasFailed;

        public ResultEntry(String job, String inputLink, String outputLink, boolean hasFailed){
            this.job = job;
            this.inputLink = inputLink;
            this.outputLink = outputLink;
            this.hasFailed = hasFailed;
        }
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.printf("Note: The credentials are taken from %s, make sure it is the right path.\n", ManagerCreator.credentialsPath);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.print("Enter path to input file: ");
        String filePath = reader.readLine();
        createManagerIfNeeded();
        String fileKey = uploadFile(filePath, bucketName);
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();
        System.out.println("Waiting for output SQS... This might take a while...");
        String outputURL = waitForQueue(sqsClient, "getAppMessagesSQS");
        System.out.println("Output SQS is on!");
        System.out.println("Waiting for input SQS...");
        String inputURL = waitForQueue(sqsClient, "sendAppMessagesSQS");
        System.out.println("Input SQS is on!");
        System.out.println(String.format("Sending %s to outputSQS\n", fileKey));
        String id = SQSClass.sendMessageFromString(sqsClient, outputURL, fileKey);
        while(true) {
            List<Message> msgs = SQSClass.receiveMessages(sqsClient, inputURL);
            if(!msgs.isEmpty())
                for(Message msg : msgs) {
                    String s = msg.body();
                    if (s.equals(id)) {
                        ResultEntry[] resultsArray = parseResults(id);
                        HTMLCreator.createHTML(resultsArray, id);
                    }
                }
            TimeUnit.SECONDS.sleep(1);
        }

    }

    public static ResultEntry[] parseResults(String id){
        // TODO: finish parsing function
        String data = S3Helper.getFileData(id + "/ID-INFO.json");
        JsonObject json = Json.createReader(new StringReader(data)).readObject();
        if(json.get("files").toString() == "[]")
            return null;

        return new ResultEntry[2];
    }

    public static boolean isManagerOn( Ec2Client ec2){
        boolean done = false;
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations())
                    for (Instance instance : reservation.instances())
                        if(instance.hasTags() && instance.state().name() == InstanceStateName.RUNNING)
                            for(Tag t : instance.tags())
                                if(t.key().equals("Name") && t.value().equals(managerName))
                                    return true;
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return false;
    }

    private static void createManagerIfNeeded() throws IOException {
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_WEST_2)
                .build();
        if (isManagerOn(ec2)){
            System.out.println("Manager EC2 found! No need to create a new one.");
            return;
        }
        System.out.println("Creating a Manager EC2 instance. This can take a while as we need to wait for the instance to start.");
        ManagerCreator.createManagerInstance(managerName);
    }
    private static String waitForQueue(SqsClient sqsClient, String queueName) throws InterruptedException {
        try {
            String name = SQSClass.getQueueByName(sqsClient, queueName);
            while (name == null) {
                TimeUnit.SECONDS.sleep(1);
                name = SQSClass.getQueueByName(sqsClient, queueName);
            }
            return name;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    private static String uploadFile(String filePath, String bucketName){
        String[] s = filePath.split("/");
        String fileName = s[s.length - 1];


        if(!S3Helper.doesObjectExists(bucketName, fileName)){
            S3Helper.putS3Object(bucketName, fileName, filePath);
            System.out.printf("Uploading %s succeeded!\n", fileName);
            return fileName;
        }
        else{
            int counter = 0;
            String tempFileName = String.format("%s%d", fileName, counter);
            while(S3Helper.doesObjectExists(bucketName, tempFileName)) {
                counter++;
                tempFileName = String.format("%s%d", fileName, counter);
            }
            S3Helper.putS3Object(bucketName, tempFileName, filePath);
            System.out.printf("Uploading %s succeeded under the name: %s!\n", fileName, tempFileName);
            return tempFileName;
        }
    }


}