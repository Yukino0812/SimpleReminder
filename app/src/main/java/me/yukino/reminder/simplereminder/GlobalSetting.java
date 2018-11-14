package me.yukino.reminder.simplereminder;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Yukino Yukinoshita
 */

public class GlobalSetting implements Serializable {

    private static Date calendarStartDate;

    public static void init(Context context) {
        read(context);
    }

    public static void setCurrentWeek(Context context, int currentWeek) {
        final int allowMaxWeek = 20;
        if (currentWeek < 1 || currentWeek > allowMaxWeek) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -7 * (currentWeek - 1));

        setCalendarStartDate(context, calendar.getTime());
    }

    private static void setCalendarStartDate(Context context, Date date) {
        if (date == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
        int dayDiff = dayOfWeek - 1;
        calendar.add(Calendar.DATE, -dayDiff);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.SECOND, -30);
        calendarStartDate = calendar.getTime();

        save(context);
    }

    public static int getWeek() {
        return getWeek(new Date());
    }

    public static int getWeek(Date date) {
        long startTime = calendarStartDate.getTime();
        long nowTime = date.getTime();

        int dayDiff = (int) ((nowTime - startTime) / (1000 * 60 * 60 * 24));
        int weekDiff = dayDiff / 7;
        int weekNow = weekDiff + 1;
        return weekNow > 0 ? weekNow : 1;
    }

    private static void save(Context context) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput("setting.txt", Context.MODE_PRIVATE));
            objectOutputStream.writeObject(calendarStartDate);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void read(Context context) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(context.openFileInput("setting.txt"));
            calendarStartDate = (Date) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            setCalendarStartDate(context, new Date());
        } catch (IOException e) {
            e.printStackTrace();
            setCalendarStartDate(context, new Date());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            setCalendarStartDate(context, new Date());
        }
    }

}
