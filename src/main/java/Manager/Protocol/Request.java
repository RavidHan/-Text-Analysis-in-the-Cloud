package Manager.Protocol;


import software.amazon.awssdk.services.sqs.model.Message;

public abstract class Request<T> {
    protected String id;
    protected T data;

    abstract public void setData(Message message);

    public T getData() {
        return data;
    }
}
