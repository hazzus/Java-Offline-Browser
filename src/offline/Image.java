package offline;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Image {
    private URL url;
    private String path;

    Image(String url, String globalPath) throws MalformedURLException {
        this.url = new URL(url);
        path = globalPath + this.url.getHost() + this.url.getPath();
    }

    void downloadImage() throws IOException, ClassCastException {
        System.out.println("Saving image: " + url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        String pathToPic = "";
        if (path.length() > 0) pathToPic = path.substring(0, path.lastIndexOf('/'));
        File directory = new File(pathToPic);
        File pic = new File(path);
        if (!pic.exists()) {
            directory.mkdirs();
            Files.copy(con.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String getPath() {
        return path;
    }

    public URL getUrl() {
        return url;
    }
}
