package com.example.v_wngwei.mcandroidintegration;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.ResultCallback;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;
import com.microsoft.azure.mobile.crashes.model.ErrorReport;
import com.microsoft.azure.mobile.push.Push;
import com.microsoft.azure.mobile.push.PushListener;
import com.microsoft.azure.mobile.push.PushNotification;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Push.setListener(new MyPushListener());
        MobileCenter.start(getApplication(), "db5d2eb7-40c4-4f68-b860-cbba9107bce6", Analytics.class, Crashes.class, Push.class);

        Button buttoncrash = (Button) findViewById(R.id.bt_crash);
        buttoncrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int a=0;
//                int b=2/a;
                throw new NullPointerException("NullPointerException");
            }
        });

        Button buttonevents = (Button) findViewById(R.id.bt_customEvents);
        buttonevents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> properties = new HashMap<>();
                properties.put("Category", "Music");
                properties.put("FileName", "favorite.avi");

                Analytics.trackEvent("Video clicked", properties);
            }
        });

    }

    private class MyPushListener implements PushListener {
        public void onPushNotificationReceived(Activity activity, PushNotification pushNotification) {

        /* The following notification properties are available. */
            String title = pushNotification.getTitle();
            String message = pushNotification.getMessage();
            Map<String, String> customData = pushNotification.getCustomData();

        /*
         * Message and title cannot be read from a background notification object.
         * Message being a mandatory field, you can use that to check foreground vs background.
         */
            if (message != null) {

            /* Display an alert for foreground push. */
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                if (title != null) {
                    dialog.setTitle(title);
                }
                dialog.setMessage(message);
                if (!customData.isEmpty()) {
                    dialog.setMessage(message + "\n" + customData);
                }
                dialog.setPositiveButton(android.R.string.ok, null);
                dialog.show();
            } else {

            /* Display a toast when a background push is clicked. */
                //Toast.makeText(activity, String.format(activity.getString(R.string.push_toast), customData), Toast.LENGTH_LONG).show(); // For example R.string.push_toast would be "Push clicked with data=%1s"
            }
        }
    }
}
