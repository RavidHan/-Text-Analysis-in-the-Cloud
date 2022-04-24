package Manager.Protocol;

import Manager.Connection.ConnectionHandler;
import Manager.Job.DataStorageInterface;
import Manager.Job.JobExecutor;
import Manager.Job.S3Storage;
import Manager.Requests.*;
import javafx.util.Pair;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AwsProtocol extends Protocol<Request>{

    private static Map<String, Integer> appMessagesAmountMap = new HashMap<>();
    private boolean shouldTerminate = false;
    private ConnectionHandler workersConnection;
    private ConnectionHandler appConnection;
    private JobExecutor jobExecutor;
    private DataStorageInterface dataStorage;

    public AwsProtocol(ConnectionHandler appConnection, ConnectionHandler workersConnection, JobExecutor jobExecutor, DataStorageInterface dataStorage){
        this.appConnection = appConnection;
        this.workersConnection = workersConnection;
        this.jobExecutor = jobExecutor;
        this.dataStorage = dataStorage;
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
        return () -> {
            String appMessageId = dataStorage.getLibOfFileFromUrl(req.getData());
            if (this.dataStorage.getFilesAmountInLib(appMessageId) == appMessagesAmountMap.get(appMessageId)){
                ManagerToAppRequest managerToAppRequest = new ManagerToAppRequest();
                managerToAppRequest.setData(dataStorage.getLibUrl(appMessageId));
                try {
                    this.appConnection.sendMessage(managerToAppRequest);
                } catch (RequestUnknownException e) {
                    e.printStackTrace();
                }
                appMessagesAmountMap.remove(appMessageId);
            }
        };
    }

    private Runnable processApplicationRequest(AppToManagerRequest req) {
        return () -> {
            try {
                List<ManagerToWorkerRequest> managerToWorkerRequests = new LinkedList<>();
                JsonArrayBuilder dataArray = Json.createArrayBuilder();
                JsonObjectBuilder s3LibData = Json.createObjectBuilder().add("files", dataArray);
                URL url = req.getData();
                // read text returned by server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] strings = line.split("\t");
                    if (strings.length == 2) {
                        ManagerToWorkerRequest managerToWorkerRequest = new ManagerToWorkerRequest();
                        managerToWorkerRequest.setData(new Pair<>(AnalysisType.valueOf(strings[0]), strings[1]));
                        managerToWorkerRequest.setAppMessageId(req.getId());
                        managerToWorkerRequests.add(managerToWorkerRequest);
                        String messageId = this.workersConnection.sendMessage(managerToWorkerRequest);
                        dataArray.add(Json.createObjectBuilder()
                                        .add("workerMessageId", messageId)
                                        .add("analysisType", managerToWorkerRequest.getData().getKey().toString())
                                        .add("inputLink", managerToWorkerRequest.getData().getValue()));
                    }
                }
                this.dataStorage.createLibInfoFile(req.getId(), s3LibData.build());
                appMessagesAmountMap.put(req.getId(), managerToWorkerRequests.size());
                in.close();
                jobExecutor.createWorkers();
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL: " + e.getMessage());
            } catch (IOException | RequestUnknownException e) {
                e.printStackTrace();
            }
        };
    }

    public boolean shouldTerminate(){
        return (this.shouldTerminate && appMessagesAmountMap.isEmpty());
    }
}
