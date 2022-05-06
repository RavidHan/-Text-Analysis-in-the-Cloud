import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class HTMLCreator {
    public static void main(String[] args) throws IOException {
        LocalApplication.ResultEntry entry = new LocalApplication.ResultEntry("POS", "https://www.youtube.com", "output1", false);
        LocalApplication.ResultEntry entry1 = new LocalApplication.ResultEntry("DEP", "https://www.youtube.com", "output2", false);
        LocalApplication.ResultEntry entry2 = new LocalApplication.ResultEntry("JOBTYPE3", "https://www.youtube.com", "output4", false);
        LocalApplication.ResultEntry entry3 = new LocalApplication.ResultEntry("JOBTYPE4", "https://www.youtube.com", "Error: Worker failed due to an issue", true);
        LocalApplication.ResultEntry[] res = new LocalApplication.ResultEntry[]{entry, entry1, entry2, entry3};
        createHTML(res, "ID123712899");
    }

    public static void createHTML(LocalApplication.ResultEntry[] results, String msgID) throws IOException {
        String bodies = getAllEntries(results);
        String finalResult = String.format("<html>\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "<title>Results for %s</title>\n" +
                "</head>\n" +
                "%s\n" +
                "</html>", msgID, bodies);
        File newHtmlFile = new File("resultHTML.html");
        if(!newHtmlFile.createNewFile()) {
            newHtmlFile.delete();
            newHtmlFile.createNewFile();
        }
        try (PrintWriter out = new PrintWriter(newHtmlFile)) {
            out.println(finalResult);
        }


    }

    private static String getAllEntries(LocalApplication.ResultEntry[] results) {
        StringBuilder sb = new StringBuilder();
        for(LocalApplication.ResultEntry result : results){
            if(result.outputLink.startsWith("https://")) {
                sb.append(String.format("<body style=\"font-size:150%%;\"> %s: \n" +
                        "<a href=\"%s\">intput</a>\n" +
                        "<a href=\"%s\">output</a>\n" +
                        "<br></body>", result.job, result.inputLink, result.outputLink));
            }
            else
                sb.append(String.format("<body style=\"font-size:150%%;\"> %s: \n" +
                        "<a href=\"%s\">intput</a>\n" +
                        " %s\n" +
                        "<br></body>", result.job, result.inputLink, result.outputLink));
        }
        return sb.toString();
    }
}
