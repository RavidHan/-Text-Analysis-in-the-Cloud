package Manager.Job;

import javax.json.Json;
import java.net.URL;

public interface DataStorageInterface {
    void createLib(String libName);
    void createLibInfoFile(String libName, Object libInfo);
    int getFilesAmountInLib(String libName);
    URL getLibUrl(String libName);
}
