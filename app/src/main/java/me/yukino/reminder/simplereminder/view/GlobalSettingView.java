package me.yukino.reminder.simplereminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sun.mail.util.MailSSLSocketFactory;

import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import me.yukino.reminder.simplereminder.BaseView;
import me.yukino.reminder.simplereminder.BuildConfig;
import me.yukino.reminder.simplereminder.GlobalSetting;
import me.yukino.reminder.simplereminder.R;

/**
 * @author Yukino Yukinoshita
 */

public class GlobalSettingView extends AppCompatActivity implements BaseView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        initCurrentWeek();
        initVersionInfo();
    }

    @Override
    public void onBackPressed() {
        backToMainView();
    }

    public void onClickSaveCurrentWeek(View view) {
        EditText editText = findViewById(R.id.editTextCurrentWeekSetting);
        String currentWeekString = editText.getText().toString();
        try {
            int currentWeek = Integer.parseInt(currentWeekString);
            GlobalSetting.setCurrentWeek(this, currentWeek);
        } catch (Exception e) {
            e.printStackTrace();
            GlobalSetting.setCurrentWeek(this, 1);
        }
    }

    public void onClickSendFeedback(View view) {
        EditText editTextFeedback = findViewById(R.id.editTextFeedback);
        final String feedback = editTextFeedback.getText().toString();
        ThreadPoolExecutor contentMonitorThreadPool = new ThreadPoolExecutor(1, 1, 3, TimeUnit.SECONDS, new ArrayBlockingQueue(5));
        contentMonitorThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                sendFeedback(feedback);
            }
        });
        editTextFeedback.setText("");
    }

    private void initCurrentWeek() {
        EditText editText = findViewById(R.id.editTextCurrentWeekSetting);
        editText.setText(String.valueOf(GlobalSetting.getWeek()));
    }

    private void initVersionInfo() {
        TextView textView = findViewById(R.id.textViewAboutInfo3);
        textView.setText("App Version: " + BuildConfig.VERSION_NAME);
    }

    public void onClickBack(View view) {
        backToMainView();
    }

    private void backToMainView() {
        Intent intent = new Intent();
        intent.setClass(me.yukino.reminder.simplereminder.view.GlobalSettingView.this, me.yukino.reminder.simplereminder.view.MainView.class);
        startActivity(intent);
        this.finish();
    }

    private void sendFeedback(String feedback) {
        if (feedback == null || feedback.length() == 0) {
            return;
        }
        final String to = "******";
        final String from = "******";
        final String host = "******";
        Properties properties = System.getProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "***");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("******", "******");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("[Feedback] A feedback from Reminder Ver." + BuildConfig.VERSION_NAME);
            message.setText(feedback);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

}
