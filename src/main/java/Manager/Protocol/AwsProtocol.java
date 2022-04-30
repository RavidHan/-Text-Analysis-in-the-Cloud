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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
            String appMessageId = req.getData()[0];
            if (appMessagesAmountMap.containsKey(appMessageId)){
                if (this.dataStorage.insertResult(appMessageId, req.getData()[1], req.getData()[2], req.getData()[3])) {
                    ManagerToAppRequest managerToAppRequest = new ManagerToAppRequest();
                    managerToAppRequest.setData(dataStorage.getLibUrl(appMessageId));
                    try {
                        this.appConnection.sendMessage(managerToAppRequest);
                    } catch (RequestUnknownException e) {
                        e.printStackTrace();
                    }
                    appMessagesAmountMap.remove(appMessageId);
                }
            }
        };
    }

    private Runnable processApplicationRequest(AppToManagerRequest req) {
        return () -> {
            try {
                List<ManagerToWorkerRequest> managerToWorkerRequests = new LinkedList<>();
                JsonArrayBuilder dataArray = Json.createArrayBuilder();

                // read text returned by server
                InputStream inputStream = dataStorage.getFile(req.getData());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = line.split("\t");
                    if (strings.length == 2) {
                        ManagerToWorkerRequest managerToWorkerRequest = new ManagerToWorkerRequest();
                        AnalysisType.AnalysisTypeEnum analysisTypeEnum = AnalysisType.getAnalysisType(strings[0]);
                        System.out.println(analysisTypeEnum);
                        if (analysisTypeEnum == null){
                            // TODO ERROR MESSAGE
                            return;
                        }
                        managerToWorkerRequest.setData(new AbstractMap.SimpleEntry<>(analysisTypeEnum, strings[1]));
                        managerToWorkerRequest.setAppMessageId(req.getId());
                        managerToWorkerRequests.add(managerToWorkerRequest);
                        System.out.println("added request");
                        String messageId = this.workersConnection.sendMessage(managerToWorkerRequest);
                        System.out.println("send message to worker");
                        dataArray.add(Json.createObjectBuilder()
                                        .add("output", "")
                                        .add("analysisType", managerToWorkerRequest.getData().getKey().toString())
                                        .add("inputLink", managerToWorkerRequest.getData().getValue())
                                        .build());
                        System.out.println("added message to json");
                    }
                }
                System.out.println("finished breakimg txt file");
                JsonObjectBuilder s3LibData = Json.createObjectBuilder().add("files", dataArray);
                this.dataStorage.createLibInfoFile(req.getId(), s3LibData.build());
                appMessagesAmountMap.put(req.getId(), managerToWorkerRequests.size());
                reader.close();
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
