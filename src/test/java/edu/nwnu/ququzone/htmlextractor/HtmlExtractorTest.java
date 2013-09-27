package edu.nwnu.ququzone.htmlextractor;

import org.testng.annotations.Test;

/**
 * Html extractor test.
 * 
 * @author Yang XuePing
 */
public class HtmlExtractorTest {
    @Test
    public void aSlowTest() {
        HtmlExtractorImpl e = new HtmlExtractorImpl();
        HtmlResult r = e
                .extractContent("http://money.163.com/13/0625/11/927B6MH200254IU4.html");
        System.out.println(r.getText());
    }

}
