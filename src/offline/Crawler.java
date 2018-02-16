package offline;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Crawler {
    private String globalPath;
    private Map<String, Page> savedPages;
    private Map<String, Image> savedImages;

    Crawler(String path) {
        this.globalPath = path;
        this.savedImages = new HashMap<>();
        this.savedPages = new HashMap<>();
    }

    private String resolveLink(URL url, String nextLink) {
        String res = nextLink.replaceAll("\\p{javaWhitespace}+", "")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&nbsp;", "\u00A0")
                .replaceAll("&mdash;", "—");
        try {
            if (!res.contains("://"))
                res = new URL(url, nextLink).toString(); //url.toURI().resolve(nextLink).toString();
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Exception while resolving: " + e.toString());
        }
        return res.replaceAll("#.*", "");
    }

    private void checkAndPutImage(Page page, String link, String change) throws IOException, ClassCastException {
        if (!savedImages.containsKey(link)) {
            Image image = new Image(link, globalPath);
            image.downloadImage();
            page.sourceCode = page.sourceCode.replaceFirst("\"" + change + "\"", image.getPath());
            savedImages.put(link, image);
        } else {
            page.sourceCode = page.sourceCode.replaceFirst("\"" + change + "\"", savedImages.get(link).getPath());
        }
    }

    private void checkAndPutPage(Page page, String link, String change) throws IOException, ClassCastException {
        if (!savedPages.containsKey(link)) {
            Page stylesheet = new Page(link, globalPath);
            stylesheet.savePage();
            parseStylesheet(stylesheet);
            page.sourceCode = page.sourceCode.replaceFirst("\"" + change + "\"", stylesheet.getPath());
            savedPages.put(link, stylesheet);
        } else {
            page.sourceCode = page.sourceCode.replaceFirst("\"" + change + "\"", savedPages.get(link).getPath());
        }
    }

    private void parseStylesheet(Page page) {
        Pattern pt = Pattern.compile("url\\((.*?)\\)");
        Matcher mt = pt.matcher(page.sourceCode.replaceAll("/\\*.*?\\*/", ""));
        while (mt.find()) {
            try {
                String link = resolveLink(page.getUrl(), mt.group(1));
                checkAndPutImage(page, link, mt.group(1));
            } catch (IOException | ClassCastException e) {
                System.err.println("Exception caught inside css: " + e.toString());
            }
        }
    }

    private void saveStyles(Page page) {
        Pattern pt = Pattern.compile("<link.+?href[\\p{javaWhitespace}]*?=[\\p{javaWhitespace}]*?\"(.+?)\".*?>");
        Matcher mt = pt.matcher(page.sourceCode.replaceAll("<!--.*?-->", ""));
        while (mt.find()) {
            try {
                String link = resolveLink(page.getUrl(), mt.group(1));
                if (link.contains(".css")) {
                    checkAndPutPage(page, link, mt.group(1));
                } else {
                    checkAndPutImage(page, link, mt.group(1));
                }

            } catch (IOException | ClassCastException e) {
                System.err.println("Exception in stylesheets/icons: " + e.toString());
            }
        }
    }

    private void saveScripts(Page page) {
        Pattern pt = Pattern.compile("<script.+?src[\\p{javaWhitespace}]*?=[\\p{javaWhitespace}]*?\"(.+?)\".*?>");
        Matcher mt = pt.matcher(page.sourceCode.replaceAll("<!--.*?-->", ""));
        while (mt.find()) {
            try {
                String link = resolveLink(page.getUrl(), mt.group(1));
                checkAndPutPage(page, link, mt.group(1));
            } catch (IOException | ClassCastException e) {
                System.err.println("Exception in script: " + e.toString());
            }
        }
    }

    private void savePics(Page page) {
        Pattern pt = Pattern.compile("<img.+?src[\\p{javaWhitespace}]*?=[\\p{javaWhitespace}]*?\"(.+?)\".*?>");
        Matcher mt = pt.matcher(page.sourceCode.replaceAll("<!--.*?-->", ""));
        while (mt.find()) {
            try {
                String link = resolveLink(page.getUrl(), mt.group(1));
                checkAndPutImage(page, link, mt.group(1));
            } catch (IOException | ClassCastException e) {
                System.err.println("Exception in picture: " + e.toString());
            }
        }
    }

    private void findLinks(Page page, int depth) {
        Pattern pt = Pattern.compile("<a.+?href[\\p{javaWhitespace}]*?=[\\p{javaWhitespace}]*?\"(.+?)\".*?>");
        Matcher mt = pt.matcher(page.sourceCode.replaceAll("<!--.*?-->", ""));
        while (mt.find()) {
            String link = resolveLink(page.getUrl(), mt.group(1));
            Page next = crawl(link, depth - 1);
            if (next != null)
                page.sourceCode = page.sourceCode.replaceFirst("\"" + mt.group(1) + "\"", next.getPath());
            else
                page.sourceCode = page.sourceCode.replaceFirst("\"" + mt.group(1) + "\"", link);
        }
    }

    Page crawl(String url, int depth) {
        Page actual = null;
        try {
            if (depth > 0) {
                actual = new Page(url, globalPath);
                saveStyles(actual);
                saveScripts(actual);
                savePics(actual);
                findLinks(actual, depth);
                actual.savePage();
            }
        } catch (IOException | ClassCastException e) {
            System.err.println("Exception caught in pages: " + e.toString());
        }
        return actual;
    }
}
