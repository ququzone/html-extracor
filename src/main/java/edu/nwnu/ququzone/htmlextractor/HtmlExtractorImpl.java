package edu.nwnu.ququzone.htmlextractor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * html extractor.
 * 
 * @author Yang XuePing
 */
@Component
public class HtmlExtractorImpl implements HtmlExtractor {
    private static final Logger logger = LoggerFactory
            .getLogger(HtmlExtractorImpl.class);

    private static final Pattern NODES = Pattern.compile("p|div");

    private int maxBytes = 1000000 / 2;

    @Override
    public HtmlResult extractContent(String url) {
        String html = "";
        try {
            html = fetchHtmlString(url, 30000);
        } catch (Exception e) {
            logger.error("获取网页异常", e);
            return new HtmlResult("fail", "获取网页异常", url);
        }
        if (html.length() == 0) {
            return new HtmlResult("fail", "无法获取源网页内容", url);
        }
        Document doc = Jsoup.parse(html);
        if (doc == null)
            return new HtmlResult("fail", "无法获取文档", url);

        HtmlResult result = new HtmlResult();
        result.setUrl(url);
        result.setTitle(doc.title());
        removeScriptsAndStyles(doc);

        List<String> blocks = extractBlocks(doc);
        String text = extractMainText(blocks);

        if (text.length() == 0) {
            result.setState("fail");
            result.setMsg("无法抽取正文");
        } else {
            result.setState("ok");
            result.setText(text);
        }
        return result;
    }

    private String extractMainText(List<String> blocks) {
        int lastend = 0;
        List<Block> candidates = new ArrayList<Block>();
        Block current = null;
        StringBuffer currentText = null;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).length() > 50) {
                lastend = i;
                if (current == null) {
                    current = new Block();
                    currentText = new StringBuffer("");
                    current.setStart(i);
                } else {
                    current.setEnd(i);
                }
                currentText.append(blocks.get(i));
            }
            if (i - lastend > 10 && current != null) {
                current.setContent(currentText.toString());
                candidates.add(current);
                current = null;
                currentText = null;
            }
        }
        if (candidates.size() == 0) {
            return "";
        }
        Block mainBody = candidates.get(0);
        for (Block b : candidates) {
            if (b.getContent().length() > mainBody.getContent().length()) {
                mainBody = b;
            }
        }
        return mainBody.getContent();
    }

    private String fetchHtmlString(String url, int timeout)
            throws MalformedURLException, IOException {
        HttpURLConnection connection = createHttpConnection(url, timeout);
        connection.setInstanceFollowRedirects(true);
        String encoding = connection.getContentEncoding();

        InputStream is;
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(connection.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            is = new InflaterInputStream(connection.getInputStream(),
                    new Inflater(true));
        } else {
            is = connection.getInputStream();
        }
        byte[] data = streamToData(is);
        String streamEncoding = detectEncoding(data);
        if (data == null || streamEncoding == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("获取数据流或者编码错误");
            }
            return "";
        }
        return new String(data, streamEncoding);
    }

    private HttpURLConnection createHttpConnection(String url, int timeout)
            throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url)
                .openConnection(Proxy.NO_PROXY);
        connection
                .setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        connection
                .setRequestProperty("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("content-charset", "UTF-8");
        connection.setRequestProperty("Cache-Control", "max-age=0");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    private String detectEncoding(byte[] data) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(data, 0, data.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        return encoding;
    }

    private byte[] streamToData(InputStream is) {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(is, 2048);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            int bytesRead = output.size();
            byte[] arr = new byte[2048];
            while (true) {
                if (bytesRead >= maxBytes) {
                    break;
                }
                int n = in.read(arr);
                if (n < 0)
                    break;
                bytesRead += n;
                output.write(arr, 0, n);
            }
            return output.toByteArray();
        } catch (SocketTimeoutException e) {
            logger.error("获取内容错误", e);
            return null;
        } catch (IOException e) {
            logger.error("获取内容错误", e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private Document removeScriptsAndStyles(Document doc) {
        Elements scripts = doc.getElementsByTag("script");
        for (Element item : scripts) {
            item.remove();
        }
        Elements noscripts = doc.getElementsByTag("noscript");
        for (Element item : noscripts) {
            item.remove();
        }
        Elements styles = doc.getElementsByTag("style");
        for (Element style : styles) {
            style.remove();
        }
        return doc;
    }

    private List<String> extractBlocks(Document doc) {
        List<String> blocks = new ArrayList<String>();
        for (Element el : doc.select("body").get(0).children()) {
            extractElementBlocks(blocks, el);
        }
        return blocks;
    }

    private void extractElementBlocks(List<String> blocks, Element element) {
        if (NODES.matcher(element.tagName()).matches()) {
            blocks.add(element.ownText().trim());
            for (Element ce : element.children()) {
                extractElementBlocks(blocks, ce);
            }
        }
    }
}
