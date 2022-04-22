package Manager.Protocol;


import Manager.Connection.S3Location;
import software.amazon.awssdk.services.sqs.model.Message;

public class ManagerToAppRequest extends Request<S3Location> {
    String appMessageId;
    String htmlFile;

    @Override
    public void setData(Message message) {

    }
}
