import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
// snippet-end:[s3.java2.s3_object_upload.import]

/**
 * To run this AWS code example, ensure that you have setup your development environment, including your AWS credentials.
 *
 * For information, see this documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */

public class S3Helper {

    public static void main(String[] args) {
        final String USAGE = "\n" +
                "Usage:\n" +
                "  <bucketName> <objectKey> <objectPath> \n\n" +
                "Where:\n" +
                "  bucketName - the Amazon S3 bucket to upload an object into.\n" +
                "  objectKey - the object to upload (for example, book.pdf).\n" +
                "  objectPath - the path where the file is located (for example, C:/AWS/book2.pdf). \n\n" ;


        String bucketName = "diamlior321";
        String objectKey = "ListObjects/ListObjects.java";
        String objectPath = "143_0";
        System.out.println("Putting object " + objectKey +" into bucket "+bucketName);
        System.out.println("  in bucket: " + bucketName);


        String result = putS3Object(bucketName, objectKey, objectPath);
        System.out.println("Tag information: "+result);
    }

    public static int getFilesNum(String bucketName, String prefix){
        int sum = 0;
        S3Client client = S3Client.builder().region(Region.US_WEST_2).build();
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
        ListObjectsV2Iterable response = client.listObjectsV2Paginator(request);
        for (ListObjectsV2Response page : response) {
            sum += page.contents().size();
        }
        return sum;
    }

    // snippet-start:[s3.java2.s3_object_upload.main]
    public static String putS3Object(String bucketName,
                                     String objectKey,
                                     String objectPath) {
        Region region = Region.US_WEST_2;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        try {

            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-amz-meta-myVal", "test");

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)
                    .build();

            s3.putObject(putOb, RequestBody.fromBytes(getObjectFile(objectPath)));
            s3.close();
            GetUrlRequest request = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            URL url = s3.utilities().getUrl(request);
            return url.toString();

        } catch (S3Exception e) {
            s3.close();
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    public static boolean doesObjectExists(String bucketName,
                                           String objectKey){
        Region region = Region.US_WEST_2;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        try {
            HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                    .key(objectKey)
                    .bucket(bucketName)
                    .build();

            HeadObjectResponse objectHead = s3.headObject(objectRequest);
            String type = objectHead.contentType();
            return true;
        } catch (S3Exception e) {
            return false;
        }

    }
    // Return a byte array
    private static byte[] getObjectFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
    // snippet-end:[s3.java2.s3_object_upload.main]
}