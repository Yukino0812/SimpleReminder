package me.yukino.reminder.simplereminder.util;

import java.util.regex.Pattern;

import me.yukino.reminder.simplereminder.model.pojo.Event;

/**
 * @author Yukino Yukinoshita
 */

public class FrequencyParser {

    public static int ONCE = Event.ONCE;
    public static int DAILY = Event.DAILY;
    public static int WEEKLY = Event.WEEKLY;

    public static int getFrequency(String content){
        if(parseOnce(content)){
            return ONCE;
        }else if(parseDaily(content)){
            return DAILY;
        }else if(parseWeekly(content)){
            return WEEKLY;
        }else {
            return ONCE;
        }
    }

    private static boolean parseOnce(String content){
        // 其实没必要写
        return false;
    }

    private static boolean parseDaily(String content){
        if(Pattern.matches(".*每[天日].*",content)){
            return true;
        }else {
            // Waiting for another pattern
        }

        return false;
    }

    private static boolean parseWeekly(String content){
        if(Pattern.matches(".*每(周|星期|礼拜).*",content)){
            return true;
        }else {
            // Waiting for another pattern
        }

        return false;
    }

}
