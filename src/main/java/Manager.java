import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;

public class Manager {

    public static void main(String[] args) {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .build();

        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl("https://sqs.us-west-2.amazonaws.com/012182824699/queue1649943493265")
                .messageBody("Timeout Testing")
                .delaySeconds(5)
                .build();
        String msgID = sqsClient.sendMessage(messageRequest).messageId();
        System.out.println();



    }
}
