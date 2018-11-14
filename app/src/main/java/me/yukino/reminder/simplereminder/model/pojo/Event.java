package me.yukino.reminder.simplereminder.model.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yukino Yukinoshita
 */

public class Event implements Serializable {

    public final static int ONCE = 0;
    public final static int DAILY = 1;
    public final static int WEEKLY = 2;

    private int eventID;
    private String content;
    private Date eventDateTime;
    private long alertBefore;
    private int alertFrequency;
    private boolean isFinish = false;

    public Event() {

    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public long getAlertBefore() {
        return alertBefore;
    }

    public void setAlertBefore(long alertBefore) {
        this.alertBefore = alertBefore;
    }

    public int getAlertFrequency() {
        return alertFrequency;
    }

    public void setAlertFrequency(int alertFrequency) {
        this.alertFrequency = alertFrequency;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }
}
