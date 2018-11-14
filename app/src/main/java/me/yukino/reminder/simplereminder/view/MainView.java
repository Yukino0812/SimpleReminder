package me.yukino.reminder.simplereminder.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;

import me.yukino.reminder.simplereminder.BaseView;
import me.yukino.reminder.simplereminder.GlobalSetting;
import me.yukino.reminder.simplereminder.R;
import me.yukino.reminder.simplereminder.model.pojo.Event;
import me.yukino.reminder.simplereminder.presenter.ReminderPresenter;
import me.yukino.reminder.simplereminder.widget.EventAdapter;

/**
 * @author Yukino Yukinoshita
 */

public class MainView extends AppCompatActivity implements BaseView, Serializable {

    private ReminderPresenter reminderPresenter;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        context = getApplicationContext();
        initMainView();
        GlobalSetting.init(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("reminderPresenter", reminderPresenter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            toGlobalSettingView();
            return true;
        } else if (id == R.id.action_new_event) {
            toNewEventView(reminderPresenter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        reminderPresenter.refreshEventList();
    }

    public void onClickSetting(View view) {
        toGlobalSettingView();
    }

    public void showEvents(ArrayList<Event> eventArrayList) {
        if (eventArrayList.size() == 0) {
            findViewById(R.id.textViewHintForFirstReminder).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.textViewHintForFirstReminder).setVisibility(View.GONE);
        }
        ListView listView = findViewById(R.id.listViewContent);
        listView.setAdapter(new EventAdapter(this, R.layout.event_item, eventArrayList));
    }

    public void showConcreteEvent(Event event) {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.MainView.this, me.yukino.reminder.simplereminder.view.EditEventView.class);
        intent.putExtra("event", event);
        intent.putExtra("reminderPresenter", reminderPresenter);
        startActivity(intent);
        this.finish();
    }

    public void replyMessage(String message) {

    }

    public static Context getContext() {
        return context;
    }

    private void initMainView() {
        reminderPresenter = new ReminderPresenter(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toNewEventView(reminderPresenter);
            }
        });

        reminderPresenter.listEvent();
    }

    private void toNewEventView(ReminderPresenter reminderPresenter) {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.MainView.this, me.yukino.reminder.simplereminder.view.NewEventView.class);
        intent.putExtra("reminderPresenter", reminderPresenter);
        startActivity(intent);
        this.finish();
    }

    private void toGlobalSettingView() {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.MainView.this, me.yukino.reminder.simplereminder.view.GlobalSettingView.class);
        startActivity(intent);
        this.finish();
    }

}
