import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

enum ParsingJob {POS, CONSTITUENCY, DEPENDENCY}


class Msg {
    public String container;
    public ParsingJob job;
    public String inputLink;

    private Msg(String container, ParsingJob job, String inputLink){
        this.container = container;
        this.job = job;
        this.inputLink = inputLink;
    }

    public static Msg parseMsg(String st) throws Exception {
        // Assuming that the message will be MSGID|JOB|INPUTLINK

        String[] result = st.split("[|]");
        if(result.length != 3){
            throw new Exception("Message is of an unknown pattern");
        }
        String id = result[0];

        ParsingJob job;
        switch(result[1]){
            case "POS":
                job = ParsingJob.POS;
                break;
            case "CONSTITUENCY":
                job = ParsingJob.CONSTITUENCY;
                break;
            case "DEPENDENCY":
                job = ParsingJob.DEPENDENCY;
                break;
            default:
                throw new Exception("Unknown parsing job: " + result[1]);

        }
        String inputLink = result[2];
        return new Msg(id, job, inputLink);
    }
}

public class Worker {

    static String inputSQS;
    static String outputSQS;
    static String bucketName;
    static String messageId;
    static String receiptHandle;

    public static void main(String[] args) throws Exception {
        if(args.length < 3)
            return;

        Worker.inputSQS = args[0];
        Worker.outputSQS = args[1];
        Worker.bucketName = args[2];
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();

        while(true) {
            String msg = getMsg(sqsClient);
            if(!msg.equals("")) {
                Msg m = Msg.parseMsg(msg);
                String answer = process(m);
                sendResult(answer, sqsClient);
                deleteMessage(sqsClient);
                deleteAllFiles(m);
            }
            else{
                TimeUnit.SECONDS.sleep(5);
            }
        }
    }

    private static void deleteAllFiles(Msg m) throws IOException {
        Runtime.getRuntime().exec(String.format("rm -rf %s_*", messageId));
        System.out.printf("Deleting all files: rm -rf %s_*", messageId);
    }

    private static void deleteMessage(SqsClient sqsClient){
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(inputSQS)
                .receiptHandle(Worker.receiptHandle)
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    private static void sendResult(String resultURL, SqsClient sqsClient){
        // Sends the result URL to the output SQS

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(outputSQS)
                .messageBody(resultURL).build();
        sqsClient.sendMessage(sendMessageRequest);
        System.out.printf("Result was sent to: %s", resultURL);
    }

    private static String getMsg(SqsClient sqsClient){
        // Receives the message from the input SQS

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(inputSQS)
                .maxNumberOfMessages(1)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
        if(messages.isEmpty())
            return "";
        Worker.messageId = messages.get(0).messageId();
        Worker.receiptHandle = messages.get(0).receiptHandle();
        String body = messages.get(0).body();
        System.out.printf("Received msg!\nBody: %s\n%n", body);
        ChangeVisibility(sqsClient, receiptHandle, 3600);
        return messages.get(0).body();
    }

    private static void ChangeVisibility(SqsClient sqsClient, String receiptHandle, int timeout){
        try {
            // Get the receipt handle for the first message in the queue.
            ChangeMessageVisibilityRequest visibilityRequest = ChangeMessageVisibilityRequest.builder()
                    .queueUrl(inputSQS)
                    .receiptHandle(receiptHandle)
                    .visibilityTimeout(timeout)
                    .build();
            sqsClient.changeMessageVisibility(visibilityRequest);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

    }
    private static String process(Msg m) throws IOException, InterruptedException {
        // Processes the message from the manager and returns the URL to S3
        String file_path = saveFileFromWeb(m);
        String resultPath = String.format("%s_result", messageId);
        File resultFile = new File(resultPath);
        if(!resultFile.createNewFile()){
            String errMsg = String.format("File named %s already exists", resultFile);
            System.out.println(errMsg);
            return errMsg;
        }
        System.out.printf("Processing the message into file: %s\n", resultPath);
        String result = StanfordParser.parse(file_path, resultPath, m.job);
        String ObjectKey = String.format("%s/%s", m.container, messageId);
        if(result.equals("1"))
            return S3Helper.putS3Object(Worker.bucketName, ObjectKey, resultPath);
        else
            return result;
    }


    private static String saveFileFromWeb(Msg m) throws IOException {
        // Gets the text from the url and return all the sentences divided by "."

        URL url = new URL(m.inputLink);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(url.openStream()));

        StringBuilder stringBuilder = new StringBuilder();

        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(inputLine);
            stringBuilder.append(System.lineSeparator());
        }

        bufferedReader.close();
        String savedPath = String.format("%s.txt", messageId);
        try (PrintWriter out = new PrintWriter(savedPath)) {
            out.println(stringBuilder.toString());
        }
        return savedPath;
    }

    private static int saveFiles(String[] lines, String id){
        // Saves the input text as small text files of length 200~ by the name of id_counter. E.G: 172_2
        // This is because Stanford parser can't parse big chunks of text

        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;

        for (String line : lines) {
            stringBuilder.append(line + ".\n");
            if (stringBuilder.length() > 300) {
                saveFile(stringBuilder.toString(), String.format("%s_%d", id, counter));
                stringBuilder = new StringBuilder();
                counter++;
            }
        }
        if(stringBuilder.length() > 0) {
            saveFile(stringBuilder.toString(), String.format("%s_%d", id, counter));
            counter++;
        }

        return counter;
    }

    private static void saveFinalTextFile(int counter, String id){
        // Save a file with the pathes to all the text files

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < counter; i++){
            stringBuilder.append(String.format("%s_%d\n", id, i));
        }

        saveFile(stringBuilder.toString(), String.format("%s_final", id));
    }

    private static void saveFile(String s, String name){
        try {
            FileWriter myWriter = new FileWriter(name);
            myWriter.write(s);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while trying to save: \n" + name);
            e.printStackTrace();
        }
    }
}
