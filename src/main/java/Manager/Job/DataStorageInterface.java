package Manager.Job;

import javax.json.Json;
import java.net.URL;

public interface DataStorageInterface {
    void createLibInfoFile(String libName, Object libInfo);
    int getFilesAmountInLib(String libName);
    String getLibUrl(String libName);
    String getLibOfFileFromUrl(String toString);
}
