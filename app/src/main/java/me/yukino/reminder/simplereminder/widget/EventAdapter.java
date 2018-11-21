package me.yukino.reminder.simplereminder.widget;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.yukino.reminder.simplereminder.GlobalSetting;
import me.yukino.reminder.simplereminder.R;
import me.yukino.reminder.simplereminder.model.pojo.Event;
import me.yukino.reminder.simplereminder.view.MainView;

/**
 * @author Yukino Yukinoshita
 */

public class EventAdapter extends ArrayAdapter<Event> {

    private MainView mainView;
    private int resId;

    public EventAdapter(MainView mainView, int resId, List<Event> eventList) {
        super(mainView, resId, eventList);
        this.mainView = mainView;
        this.resId = resId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(resId, null);

        TextView textViewContent = view.findViewById(R.id.textViewContent);
        Button buttonFinish = view.findViewById(R.id.buttonCheckToFinish);
        TextView textViewAlertFrequency = view.findViewById(R.id.textViewAlertFrequency);
        TextView textViewEventTime = view.findViewById(R.id.textViewEventTime);
        TextView textViewEventWeekDay = view.findViewById(R.id.textViewEventWeekDay);
        TextView textViewRestDays = view.findViewById(R.id.textViewEventRestDays);

        // set event content
        final Event event = getItem(position);
        textViewContent.setText(event.getContent());

        // set event alert frequency
        switch (event.getAlertFrequency()) {
            case Event.ONCE:
                textViewAlertFrequency.setText("一次");
                break;
            case Event.DAILY:
                textViewAlertFrequency.setText("每天");
                break;
            case Event.WEEKLY:
                textViewAlertFrequency.setText("每周");
                break;
            default:
        }

        // set event date
        Date date = event.getEventDateTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String dateString = simpleDateFormat.format(date);
        textViewEventTime.setText(dateString);

        // set event week and day of week
        final String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int week = GlobalSetting.getWeek(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String weekTextStr = "第" + String.valueOf(week) + "周" + "  " + weekDays[dayOfWeek];
        textViewEventWeekDay.setText(weekTextStr);

        // set event rest days
        setRestDays(textViewRestDays, event.getEventDateTime());

        // set button style and onClick listener
        if (event.isFinish()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                buttonFinish.setBackground(mainView.getResources().getDrawable(R.drawable.checkbox_on_background));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                buttonFinish.setBackground(mainView.getResources().getDrawable(R.drawable.checkbox_off_background));
            }
        }
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event.setFinish(!event.isFinish());
                mainView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainView.refresh();
                    }
                });
            }
        });

        // set text color and background color
        int textColor;
        int backgroundColor;
        if (!event.isFinish() && event.getEventDateTime().compareTo(new Date()) >= 0) {
            textColor = getTextColor(date);
            backgroundColor = getBackgroundColor(date);
        } else {
            textColor = Color.parseColor(getColorString(0));
            backgroundColor = Color.parseColor(getColorString(0xAFAFAF));
        }

        textViewContent.setTextColor(textColor);
        textViewAlertFrequency.setTextColor(textColor);
        textViewEventTime.setTextColor(textColor);
        textViewEventWeekDay.setTextColor(textColor);
        textViewRestDays.setTextColor(textColor);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(backgroundColor);
        gradientDrawable.setCornerRadius(20f);
        gradientDrawable.setGradientType(GradientDrawable.RECTANGLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(gradientDrawable);
        } else {
            view.setBackgroundColor(backgroundColor);
        }
        view.getBackground().setAlpha(getBackgroundAlpha(date));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainView.showConcreteEvent(event);
            }
        });

        return view;
    }

    private void setRestDays(TextView textViewRestDay, Date eventDate) {
        long currentTime = System.currentTimeMillis();
        long eventTime = eventDate.getTime();
        long timeDiff = eventTime - currentTime;
        int dayDiff = (int) (timeDiff / (1000 * 60 * 60 * 24));
        int hourDiff = (int) (timeDiff / (1000 * 60 * 60));
        int minuteDiff = (int) (timeDiff / (1000 * 60));
        if (timeDiff < 0) {
            textViewRestDay.setText("已过时");
        } else if (dayDiff > 0) {
            textViewRestDay.setText("剩余 " + String.valueOf(dayDiff) + " 天");
        } else if (hourDiff > 0) {
            textViewRestDay.setText("剩余 " + String.valueOf(hourDiff) + " 小时");
        } else if (minuteDiff > 0) {
            textViewRestDay.setText("剩余 " + String.valueOf(minuteDiff) + " 分钟");
        } else {
            textViewRestDay.setText("即将过时");
        }
    }

    private int getTextColor(Date eventDate) {
        float[] hsv = new float[3];
        hsv[0] = (getBackgroundColorH(eventDate) + 180) % 360;
        hsv[1] = getBackgroundColorS(eventDate);
        hsv[2] = getBackgroundColorV();

        return Color.HSVToColor(hsv);
    }

    private int getBackgroundColor(Date eventDate) {
        float[] hsv = new float[3];
        hsv[0] = getBackgroundColorH(eventDate);
        hsv[1] = getBackgroundColorS(eventDate);
        hsv[2] = getBackgroundColorV();

        return Color.HSVToColor(50, hsv);
    }

    private String getColorString(int color) {
        String str = Integer.toHexString(color);

        if (str.length() == 6) {
            return "#" + str;
        } else {
            StringBuilder stringBuilder = new StringBuilder(str);
            for (int i = 0; i < 6 - str.length(); ++i) {
                stringBuilder.insert(0, '0');
            }
            return "#" + stringBuilder.toString();
        }
    }

    private int getBackgroundAlpha(Date eventDate) {
        long dayDiffAllow = 15 * 24 * 60 * 60 * 1000;
        long dayDiff = eventDate.getTime() - System.currentTimeMillis();
        float dayDiffPercent = (float) dayDiff / (float) dayDiffAllow;
        dayDiffPercent = dayDiffPercent > 1 ? 1 : dayDiffPercent;
        dayDiffPercent = dayDiffPercent < 0 ? 0 : dayDiffPercent;
        int alpha = (int) ((1 - dayDiffPercent) * 255);
        alpha = alpha < 100 ? 100 : alpha;
        return alpha;
    }

    private float getBackgroundColorH(Date eventDate) {
        long dayDiffAllow = 15 * 24 * 60 * 60 * 1000;
        long dayDiff = eventDate.getTime() - System.currentTimeMillis();
        float dayDiffPercent = (float) dayDiff / (float) dayDiffAllow;
        dayDiffPercent = dayDiffPercent > 1 ? 1 : dayDiffPercent;
        dayDiffPercent = dayDiffPercent < 0 ? 0 : dayDiffPercent;
        return (1 - dayDiffPercent) * 360;
    }

    private float getBackgroundColorS(Date eventDate) {
        long dayDiffAllow = 15 * 24 * 60 * 60 * 1000;
        long dayDiff = eventDate.getTime() - System.currentTimeMillis();
        float dayDiffPercent = (float) dayDiff / (float) dayDiffAllow;
        dayDiffPercent = dayDiffPercent > 0.5f ? 0.5f : dayDiffPercent;
        dayDiffPercent = dayDiffPercent < 0 ? 0 : dayDiffPercent;
        return 1 - dayDiffPercent;
    }

    private float getBackgroundColorV() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 17 || hour < 6) {
            return 0.5f;
        } else {
            return 1.0f;
        }
    }

}
