package edu.nwnu.ququzone.htmlextractor;

import java.io.Serializable;

/**
 * html result.
 * 
 * @author Yang XuePing
 */
public class HtmlResult implements Serializable {
    private static final long serialVersionUID = -2323320480483953602L;

    private String state;

    private String msg;

    private String url;

    private String title;

    private String text;

    public HtmlResult() {
    }

    public HtmlResult(String state, String msg, String url) {
        this.state = state;
        this.msg = msg;
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
