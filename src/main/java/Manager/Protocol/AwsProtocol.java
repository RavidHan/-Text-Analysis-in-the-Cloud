package Manager.Protocol;

import Manager.Connection.ConnectionHandler;

public class AwsProtocol extends Protocol<Request>{

    private boolean shouldTerminate = false;
    private ConnectionHandler workersConnection;
    private ConnectionHandler appConnection;

    public AwsProtocol(ConnectionHandler appConnection, ConnectionHandler workersConnection){
        this.appConnection = appConnection;
        this.workersConnection = workersConnection;
    }

    @Override
    public Runnable process(Request req) throws RequestUnknownException {
        if (req instanceof AppToManagerRequest){
            if (((AppToManagerRequest) req).isTermination()){
                shouldTerminate = true;
                return null;
            }
            return this.processApplicationRequest((AppToManagerRequest) req);
        }
        if (req instanceof ManagerToAppRequest){
            // TODO
            return null;
        }
        if (req instanceof WorkerToManagerRequest){
            return this.processWorkerRequest((WorkerToManagerRequest) req);
        }

        throw new RequestUnknownException();
    }

    private Runnable processWorkerRequest(WorkerToManagerRequest req) {
        // TODO
        return null;
    }

    private Runnable processApplicationRequest(AppToManagerRequest req) {
        // TODO


        return null;
    }

    public boolean shouldTerminate(){
        return this.shouldTerminate;
    }
}
