package Manager.Protocol;

import sun.plugin2.message.Message;

public class ManagerToAppRequest extends Request {
    String appMessageId;
    String htmlFile;

    @Override
    public void setData(Message message) {

    }
}
