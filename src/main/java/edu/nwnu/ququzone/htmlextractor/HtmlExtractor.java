package edu.nwnu.ququzone.htmlextractor;

/**
 * html main body extractor.
 * 
 * @author Yang XuePing
 */
public interface HtmlExtractor {
    /**
     * extract main body.
     * 
     * @param url
     * @return
     */
    HtmlResult extractContent(String url);
}
