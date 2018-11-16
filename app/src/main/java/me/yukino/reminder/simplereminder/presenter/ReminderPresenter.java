package me.yukino.reminder.simplereminder.presenter;

import android.os.Looper;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.yukino.reminder.simplereminder.BasePresenter;
import me.yukino.reminder.simplereminder.GlobalSetting;
import me.yukino.reminder.simplereminder.model.ReminderModel;
import me.yukino.reminder.simplereminder.model.pojo.Event;
import me.yukino.reminder.simplereminder.util.DateParser;
import me.yukino.reminder.simplereminder.util.DateTimeParser;
import me.yukino.reminder.simplereminder.util.FrequencyParser;
import me.yukino.reminder.simplereminder.view.MainView;

import static java.util.Date.from;

/**
 * @author Yukino Yukinoshita
 */

public class ReminderPresenter implements BasePresenter, Serializable {

    private transient ThreadPoolExecutor singleThreadPool;
    private MainView mainView;
    private ReminderModel reminderModel;

    public ReminderPresenter() {

    }

    public ReminderPresenter(MainView mainView) {
        this.mainView = mainView;
        this.reminderModel = new ReminderModel(this);
        initThread();
    }

    public void refreshEventList() {
        reminderModel.save();
        this.listEvent();
    }

    public void saveEventList() {
        reminderModel.save();
    }

    public void listEvent() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (singleThreadPool == null) {
                initThread();
            }
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Event> eventArrayList = reminderModel.listEvent();
                    ArrayList<Event> onGoingEventList = new ArrayList<>();
                    ArrayList<Event> finishOrOutOfTimeEventList = new ArrayList<>();

                    splitEvent(eventArrayList, onGoingEventList, finishOrOutOfTimeEventList);

                    ArrayList<Event> resultArrayList = new ArrayList<>(onGoingEventList);
                    resultArrayList.addAll(finishOrOutOfTimeEventList);
                    final ArrayList<Event> finalResultArrayList = new ArrayList<>(resultArrayList);
                    mainView.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainView.showEvents(finalResultArrayList);
                        }
                    });
                }
            });
        } else {
            ArrayList<Event> eventArrayList = reminderModel.listEvent();
            ArrayList<Event> onGoingEventList = new ArrayList<>();
            ArrayList<Event> finishOrOutOfTimeEventList = new ArrayList<>();

            splitEvent(eventArrayList, onGoingEventList, finishOrOutOfTimeEventList);

            ArrayList<Event> resultArrayList = new ArrayList<>(onGoingEventList);
            resultArrayList.addAll(finishOrOutOfTimeEventList);

            final ArrayList<Event> finalResultArrayList = new ArrayList<>(resultArrayList);

            mainView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainView.showEvents(finalResultArrayList);
                }
            });
        }
    }

    private void splitEvent(ArrayList<Event> allEventList, ArrayList<Event> onGoingEventList, ArrayList<Event> finishOrOutOfTimeEventList) {
        for (Event event : allEventList) {
            Date date = new Date();
            if (event.isFinish()) {
                if (event.getEventDateTime().compareTo(date) < 0) {
                    if (!checkIsOnce(event)) {
                        if (checkIsOutOfTime(event)) {
                            deleteEvent(event.getEventID());
                        } else {
                            onGoingEventList.add(event);
                        }
                    }
                } else {
                    finishOrOutOfTimeEventList.add(event);
                }
            } else if (event.getEventDateTime().compareTo(date) < 0) {
                if (checkIsOutOfTime(event)) {
                    finishOrOutOfTimeEventList.add(event);
                } else {
                    onGoingEventList.add(event);
                }
            } else {
                onGoingEventList.add(event);
            }
        }

        Collections.sort(onGoingEventList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return event1.getEventDateTime().compareTo(event2.getEventDateTime());
            }
        });
    }

    private boolean checkIsOnce(Event event) {
        if (event.getAlertFrequency() == Event.ONCE) {
            deleteEvent(event.getEventID());
            return true;
        }
        return false;
    }

    private boolean checkIsOutOfTime(Event event) {
        switch (event.getAlertFrequency()) {
            case Event.DAILY:
                for (int i = 0; i <= 70; ++i) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(event.getEventDateTime());
                    calendar.add(Calendar.DATE, i);
                    if (calendar.getTime().getTime() > System.currentTimeMillis()) {
                        event.setEventDateTime(calendar.getTime());
                        saveEventList();
                        return false;
                    }
                }
                return true;
            case Event.WEEKLY:
                for (int i = 0; i <= 10; ++i) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(event.getEventDateTime());
                    calendar.add(Calendar.WEEK_OF_YEAR, i);
                    if (calendar.getTime().getTime() > System.currentTimeMillis()) {
                        event.setEventDateTime(calendar.getTime());
                        saveEventList();
                        return false;
                    }
                }
                return true;
            default:
                return true;
        }
    }

    public void addEvent(String content, Date time, int alertBefore, int alertFrequency) {
        Event event = new Event();
        event.setContent(content);
        event.setEventDateTime(time);
        event.setAlertBefore(alertBefore);
        event.setAlertFrequency(alertFrequency);
        reminderModel.saveEvent(event);
        mainView.replyMessage("Create new event successfully");
    }

    public void deleteEvent(int eventID) {
        reminderModel.deleteEvent(eventID);
    }

    public Event getEvent(int eventID) {
        return reminderModel.getEvent(eventID);
    }

    public void getEventAndShow(int eventID) {
        mainView.showConcreteEvent(getEvent(eventID));
    }

    public Date getParsedDate(String content) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime localDateTime = DateTimeParser.getDateTime(content, GlobalSetting.getWeek(), 90);
            Instant instant = null;
            instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            return from(instant);
        } else {
            return DateParser.getDateTime(content, GlobalSetting.getWeek(), 90);
        }

    }

    public int getParsedFrequency(String content) {
        return FrequencyParser.getFrequency(content);
    }

    private void initThread() {
        singleThreadPool = new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, new ArrayBlockingQueue(5));
    }

}
