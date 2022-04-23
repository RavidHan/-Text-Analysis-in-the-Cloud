package Manager.Protocol;

import Manager.Connection.ConnectionHandler;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AwsProtocol extends Protocol<Request>{

    private static AtomicInteger counter = new AtomicInteger(0);
    private boolean shouldTerminate = false;
    private ConnectionHandler workersConnection;
    private ConnectionHandler appConnection;
    private final int messagesPerWorker;

    public AwsProtocol(ConnectionHandler appConnection, ConnectionHandler workersConnection, int messagesPerWorker){
        this.messagesPerWorker = messagesPerWorker;
        this.appConnection = appConnection;
        this.workersConnection = workersConnection;
    }

    @Override
    public Runnable process(Request req) throws RequestUnknownException, NotifyFinishedException {
        if (req instanceof AppToManagerRequest){
            if (((AppToManagerRequest) req).isTermination()){
                this.shouldTerminate = true;
                this.workersConnection.terminate();
                this.appConnection.terminate();
                throw new NotifyFinishedException();
            }
            return this.processApplicationRequest((AppToManagerRequest) req);
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
        return () -> {
            try {
                List<ManagerToWorkerRequest> managerToWorkerRequests = new LinkedList<>();
                URL url = req.getData();
                // read text returned by server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] strings = line.split(" ");
                    if (strings.length == 2) {
                        ManagerToWorkerRequest managerToWorkerRequest = new ManagerToWorkerRequest();
                        managerToWorkerRequest.setData(new Pair<>(strings[0], strings[1]));
                        managerToWorkerRequest.setAppMessageId(req.getId());
                        managerToWorkerRequests.add(managerToWorkerRequest);
                    }
                }
                for (ManagerToWorkerRequest managerToWorkerRequest : managerToWorkerRequests) {
                    managerToWorkerRequest.setResponsesAmount(managerToWorkerRequests.size());
                    this.workersConnection.sendMessage(managerToWorkerRequest);
                }
                counter.getAndIncrement();
                in.close();
                createWorkers();
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL: " + e.getMessage());
            } catch (IOException | RequestUnknownException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean shouldTerminate(){
        return (this.shouldTerminate && counter.get() == 0);
    }


    private void createWorkers() {

    }
}
