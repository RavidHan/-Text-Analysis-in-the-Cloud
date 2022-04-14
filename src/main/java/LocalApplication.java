import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;


import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;


public class LocalApplication {
    public static void main(String[] args) throws InterruptedException {


        String name = "AnotherName";
        String amiId = "ami-0b36cd6786bcfe120";
        Ec2Client ec2 = GetEc2();
        String instanceId = createEC2Instance(ec2,name, amiId) ;
        System.out.println("The Amazon EC2 Instance ID is "+instanceId);
        TimeUnit.SECONDS.sleep(40);
        describeEC2Instances(ec2);
        stopEC2Instance(ec2, instanceId);
    }

    public static Ec2Client GetEc2(){
        Region region = Region.US_WEST_2;
        return Ec2Client.builder()
                .region(region)
                .build();
    }

    private static String getECSuserData(String s) {
        String userData = "";
        userData = userData + "#!/bin/bash" + "\n";
        userData = userData + "echo " + s + " ";
        userData = userData + ">> /home/ec2-user/helloworld.txt";
        String base64UserData = null;
        try {
            base64UserData = new String( Base64.getEncoder().encode(userData.getBytes("UTF-8")), "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64UserData;
    }

    public static void describeEC2Instances( Ec2Client ec2){

        boolean done = false;
        String nextToken = null;

        try {

            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);

                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        System.out.println("Instance Id is " + instance.instanceId());
                        System.out.println("Image id is "+  instance.imageId());
                        System.out.println("Instance type is "+  instance.instanceType());
                        System.out.println("Instance state name is "+  instance.state().name());
                        System.out.println("monitoring information is "+  instance.monitoring().state());

                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void stopEC2Instance(Ec2Client ec2, String id) {
        StopInstancesRequest runRequest = StopInstancesRequest.builder().instanceIds(id).build();

        ec2.stopInstances(runRequest);
    }
    public static String createEC2Instance(Ec2Client ec2, String name, String amiId ) {
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.T2_MICRO)
                .userData(getECSuserData("hello world"))
                .maxCount(1)
                .minCount(1)
                .securityGroups("launch-wizard-2")
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key("Name")
                .value(name)
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "Successfully started EC2 Instance %s based on AMI %s",
                    instanceId, amiId);

            return instanceId;

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return "";
    }
}