package me.yukino.reminder.simplereminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.yukino.reminder.simplereminder.BaseView;
import me.yukino.reminder.simplereminder.GlobalSetting;
import me.yukino.reminder.simplereminder.R;
import me.yukino.reminder.simplereminder.presenter.ReminderPresenter;
import me.yukino.reminder.simplereminder.widget.CustomDatePicker;

/**
 * @author Yukino Yukinoshita
 */

public class NewEventView extends AppCompatActivity implements BaseView {

    private ReminderPresenter reminderPresenter;
    private ThreadPoolExecutor contentMonitorThreadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_new_event);

        initThread();
        initView();
    }

    @Override
    public void onBackPressed() {
        backToMainView();
    }

    public void onClickCancelNewEvent(View view) {
        backToMainView();
    }

    public void onClickSetCurrentWeek(View view) {
        toSettingView();
    }

    public void onClickAddNewEvent(View view) {
        TextView textViewContent = findViewById(R.id.editTextEventContent);
        String content = textViewContent.getText().toString();

        TextView textViewTime = findViewById(R.id.currentTime);
        String timeString = textViewTime.getText().toString();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date date = new Date();
        try {
            date = dateFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CheckBox[] checkBoxes = new CheckBox[3];
        checkBoxes[0] = findViewById(R.id.checkBoxOnce);
        checkBoxes[1] = findViewById(R.id.checkBoxDaily);
        checkBoxes[2] = findViewById(R.id.checkBoxWeekly);

        int alertFrequency = 0;

        for (int i = 0; i < 3; ++i) {
            if (checkBoxes[i].isChecked()) {
                alertFrequency = i;
                break;
            }
        }

        reminderPresenter.addEvent(content, date, 0, alertFrequency);

        backToMainView();
    }

    private void backToMainView() {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.NewEventView.this, me.yukino.reminder.simplereminder.view.MainView.class);
        startActivity(intent);
        this.finish();
    }

    private void toSettingView() {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.NewEventView.this, me.yukino.reminder.simplereminder.view.GlobalSettingView.class);
        startActivity(intent);
        this.finish();
    }

    private void initView() {
        reminderPresenter = (ReminderPresenter) getIntent().getSerializableExtra("reminderPresenter");
        initDatePicker();
        initWeek();
        initCheckBoxListener();
        initContentChangeListener();
    }

    private void initDatePicker() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = simpleDateFormat.format(new Date());

        final TextView textViewCurrentTime = findViewById(R.id.currentTime);
        textViewCurrentTime.setText(now);

        Date dateAfterHalfYear = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateAfterHalfYear);
        calendar.add(Calendar.DATE, 180);
        dateAfterHalfYear.setTime(calendar.getTime().getTime());
        String dateAfter90daysStr = simpleDateFormat.format(dateAfterHalfYear);

        final CustomDatePicker customDatePicker = new CustomDatePicker(this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) {
                textViewCurrentTime.setText(time);
                try {
                    setWeekDay(simpleDateFormat.parse(time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, "2018-01-01 00:00", dateAfter90daysStr);

        findViewById(R.id.selectTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDatePicker.show(textViewCurrentTime.getText().toString());
            }
        });

    }

    private void initCheckBoxListener() {
        final CheckBox[] checkBoxes = new CheckBox[3];
        checkBoxes[0] = findViewById(R.id.checkBoxOnce);
        checkBoxes[1] = findViewById(R.id.checkBoxDaily);
        checkBoxes[2] = findViewById(R.id.checkBoxWeekly);

        for (final CheckBox checkBox : checkBoxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (CheckBox box : checkBoxes) {
                        if (box == checkBox) {
                            box.setChecked(true);
                        } else {
                            box.setChecked(false);
                        }
                    }
                }
            });
        }
    }

    private void initContentChangeListener() {
        final EditText editText = findViewById(R.id.editTextEventContent);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                contentMonitorThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Date date = reminderPresenter.getParsedDate(editText.getText().toString());
                        final int frequency = reminderPresenter.getParsedFrequency(editText.getText().toString());
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeDate(date);
                                    changeFrequency(frequency);
                                }
                            });
                        } else {
                            changeDate(date);
                            changeFrequency(frequency);
                        }
                    }
                });
            }
        });
    }

    private void changeDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String dateString = simpleDateFormat.format(date);

        TextView textView = findViewById(R.id.currentTime);
        textView.setText(dateString);

        setWeekDay(date);
    }

    private void changeFrequency(int frequency) {
        CheckBox[] checkBoxes = new CheckBox[3];
        checkBoxes[0] = findViewById(R.id.checkBoxOnce);
        checkBoxes[1] = findViewById(R.id.checkBoxDaily);
        checkBoxes[2] = findViewById(R.id.checkBoxWeekly);

        for (int i = 0; i < 3; ++i) {
            if (i == frequency) {
                checkBoxes[i].setChecked(true);
            } else {
                checkBoxes[i].setChecked(false);
            }
        }
    }

    private void initWeek() {
        Date date = new Date();
        setWeekDay(date);
    }

    private void setWeekDay(Date date) {
        final String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

        int week = GlobalSetting.getWeek(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String weekTextStr = "第" + String.valueOf(week) + "周" + "    " + weekDays[dayOfWeek];

        TextView textView = findViewById(R.id.textViewWeekDay);
        textView.setText(weekTextStr);
    }

    private void initThread() {
        contentMonitorThreadPool = new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, new ArrayBlockingQueue(5));
    }

}
