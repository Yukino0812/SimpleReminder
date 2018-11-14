package me.yukino.reminder.simplereminder.model;

import android.content.Context;
import android.util.SparseArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.yukino.reminder.simplereminder.model.pojo.Event;
import me.yukino.reminder.simplereminder.presenter.ReminderPresenter;
import me.yukino.reminder.simplereminder.view.MainView;

/**
 * @author Yukino Yukinoshita
 */

public class ReminderModel implements Serializable {

    private ReminderPresenter reminderPresenter;
    private Event[] eventAry;
    private transient SparseArray<Event> eventSparseArray = new SparseArray<>();

    public ReminderModel() {

    }

    public ReminderModel(ReminderPresenter reminderPresenter) {
        this.reminderPresenter = reminderPresenter;
        initEventSparseArray();
    }

    public ArrayList<Event> listEvent() {
        if (eventSparseArray == null) {
            initEventSparseArray();
        }
        ArrayList<Event> eventArrayList = new ArrayList<>();
        for (int i = 0; i < eventSparseArray.size(); ++i) {
            eventArrayList.add(eventSparseArray.valueAt(i));
        }
        Collections.sort(eventArrayList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return event1.getEventDateTime().compareTo(event2.getEventDateTime());
            }
        });
        return eventArrayList;
    }

    public Event getEvent(int eventID) {
        if (eventSparseArray == null) {
            initEventSparseArray();
        }
        return eventSparseArray.get(eventID);
    }

    public void saveEvent(Event event) {
        if (eventSparseArray == null) {
            initEventSparseArray();
        }
        int eventID = event.getEventID();
        if (eventID == 0) {
            for (int i = 1; ; ++i) {
                if (eventSparseArray.get(i, null) == null) {
                    event.setEventID(i);
                    eventSparseArray.put(i, event);
                    break;
                }
            }
        } else {
            eventSparseArray.put(eventID, event);
        }
        save();
    }

    public void deleteEvent(int eventID) {
        if (eventSparseArray == null) {
            initEventSparseArray();
        }
        eventSparseArray.remove(eventID);
        save();
    }

    public void save() {
        copyToAry();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(MainView.getContext().openFileOutput("event_list.txt", Context.MODE_PRIVATE));
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ReminderModel read() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(MainView.getContext().openFileInput("event_list.txt"));
            ReminderModel reminderModel = (ReminderModel) objectInputStream.readObject();
            objectInputStream.close();
            return reminderModel;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return this;
        } catch (IOException e) {
            e.printStackTrace();
            return this;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return this;
        }
    }

    private void initEventSparseArray() {
        this.eventSparseArray = this.read().getInitEvent();
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private SparseArray<Event> getInitEvent() {
        eventSparseArray = new SparseArray<>();
        if (eventAry == null) {
            return eventSparseArray;
        }
        for (Event event : eventAry) {
            eventSparseArray.put(event.getEventID(), event);
        }
        return eventSparseArray;
    }

    private void copyToAry() {
        if (eventSparseArray == null) {
            initEventSparseArray();
        }
        ArrayList<Event> eventArrayList = new ArrayList<>();
        for (int i = 0; i < eventSparseArray.size(); ++i) {
            eventArrayList.add(eventSparseArray.valueAt(i));
        }
        eventAry = new Event[eventArrayList.size()];
        eventAry = eventArrayList.toArray(eventAry);
    }

}
