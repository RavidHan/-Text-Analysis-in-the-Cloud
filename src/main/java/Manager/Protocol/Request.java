package Manager.Protocol;

import sun.plugin2.message.Message;

public abstract class Request<T> {
    protected String id;
    protected T data;

    abstract public void setData(Message message);

    public T getData() {
        return data;
    }
}
