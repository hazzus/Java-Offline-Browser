package offline;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Page {
    private URL url;
    String sourceCode;
    private String path;
    private List<String> toGo;

    Page(String url, String globalPath) throws IOException {
        this.url = new URL(url);
        if (this.url.getPath().length() <= 1)
            url += "index.html";
        this.url = new URL(url);
        path = globalPath + this.url.getHost() + this.url.getPath();
        downloadCode();
    }

    private void downloadCode() throws IOException, ClassCastException {
        System.out.println("Saving page: " + url.toString());
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String s = in.readLine();
            StringBuilder code = new StringBuilder();
            while (s != null) {
                code.append(s);
                s = in.readLine();
            }
            this.sourceCode = code.toString();
        } catch (IOException e) {
            System.err.println("Exception caught while downloading source of page: " + e.toString());
            throw new IOException();
        }
    }

    void savePage() throws IOException {
        File toWrite = new File(path);
        String pathToFile = "";
        if (path.length() > 0) {
            pathToFile = path.substring(0, path.lastIndexOf('/'));
        }
        File directory  = new File(pathToFile);

        directory.mkdirs();
        PrintWriter out = new PrintWriter(toWrite);
        out.write(sourceCode);
        out.close();

    }

    public URL getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public List<String> getToGo() {
        return toGo;
    }
}
