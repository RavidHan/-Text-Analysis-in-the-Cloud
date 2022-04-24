package Manager.Connection;

import Manager.Requests.AppToManagerRequest;
import Manager.Requests.ManagerToAppRequest;
import Manager.Requests.Request;
import Manager.Requests.RequestUnknownException;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.MalformedURLException;
import java.net.URL;

public class ApplicationEncoderDecoder extends EncoderDecoder<String, URL> {

    @Override
    public String encode(Request<String> request) throws RequestUnknownException {
        if (!(request instanceof  ManagerToAppRequest)){
            throw new RequestUnknownException();
        }
        return request.getData();
    }

    @Override
    public Request<URL> decode(Message message) {
        AppToManagerRequest appToManagerRequest = new AppToManagerRequest();
        appToManagerRequest.setId(message.messageId());
        try {
            if (message.body().equals("terminate")){
                appToManagerRequest.setTerminationMessage(true);
            } else {
                appToManagerRequest.setData(new URL(message.body()));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return appToManagerRequest;
    }
}
