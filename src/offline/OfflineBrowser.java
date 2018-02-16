package offline;

import java.io.File;

public class OfflineBrowser {
    public static void main(String[] args) {
        File globalDir = new File("result");
        Crawler browser = new Crawler(globalDir.getAbsolutePath() + '/');
        try {
            browser.crawl(args[0], Integer.parseInt(args[1]));
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Not enough arguments!");
        }
    }
}
