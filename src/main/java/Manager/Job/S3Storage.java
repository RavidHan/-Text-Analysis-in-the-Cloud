package Manager.Job;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ListIterator;

public class S3Storage implements DataStorageInterface {
    private String bucketName;
    private S3Client s3;
    private static final String id_file_name= "ID-INFO.json";

    public S3Storage(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public InputStream getFile(String location) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(this.bucketName).key(location).build();
        return this.s3.getObject(getObjectRequest);
    }

    @Override
    public void createLibInfoFile(String libName, Object libInfo) {
        if (libInfo instanceof JsonObject) {
            PutObjectRequest putObjectRequest = PutObjectRequest
                    .builder()
                    .bucket(this.bucketName)
                    .key(libName + "/" + id_file_name)
                    .build();
            s3.putObject(putObjectRequest,
                    RequestBody.fromBytes(libInfo.toString().getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Override
    public int getFilesAmountInLib(String libName) {
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .prefix(libName)
                    .delimiter("/")
                    .build();

            ListObjectsResponse res = this.s3.listObjects(listObjects);
            return res.contents().size()-1;

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return 0;
    }

    @Override
    public String getLibUrl(String libName) {
        return "s3//:" + this.bucketName + "/" + libName;
    }

    @Override
    public String getLibOfFileFromUrl(String url) {
        String[] strings = url.split("/");
        return strings[strings.length-2];
    }
}
