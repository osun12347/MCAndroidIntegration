package com.example.v_wngwei.mcandroidintegration;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.ResultCallback;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.analytics.channel.AnalyticsListener;
import com.microsoft.azure.mobile.analytics.ingestion.models.PageLog;
import com.microsoft.azure.mobile.crashes.AbstractCrashesListener;
import com.microsoft.azure.mobile.crashes.Crashes;
import com.microsoft.azure.mobile.crashes.CrashesListener;
import com.microsoft.azure.mobile.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.azure.mobile.crashes.model.ErrorReport;
import com.microsoft.azure.mobile.distribute.Distribute;
import com.microsoft.azure.mobile.ingestion.models.LogWithProperties;
import com.microsoft.azure.mobile.push.Push;
import com.microsoft.azure.mobile.push.PushListener;
import com.microsoft.azure.mobile.push.PushNotification;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Crashes.setListener(getCrashesListener());
        Push.setListener(new MyPushListener());
        //start the sdk
        MobileCenter.start(getApplication(), "db5d2eb7-40c4-4f68-b860-cbba9107bce6", Analytics.class, Crashes.class, Push.class, Distribute.class);

        Button buttoncrash = (Button) findViewById(R.id.bt_crash);
        buttoncrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 //               int a=0;
 //               int b=2/a;
                throw new ArrayIndexOutOfBoundsException();
            }
        });

        //Custom events
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


    @NonNull
    private AbstractCrashesListener getCrashesListener() {
        return new AbstractCrashesListener() {

            @Override
            public boolean shouldAwaitUserConfirmation() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setTitle(R.string.crash_confirmation_dialog_title)
                        .setMessage(R.string.crash_confirmation_dialog_message)
                        .setPositiveButton(R.string.crash_confirmation_dialog_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.SEND);
                            }
                        })
                        .setNegativeButton(R.string.crash_confirmation_dialog_not_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.DONT_SEND);
                            }
                        })
                        .setNeutralButton(R.string.crash_confirmation_dialog_always_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.ALWAYS_SEND);
                            }
                        });
                builder.create().show();
                return true;
            }

            @Override
            public Iterable<ErrorAttachmentLog> getErrorAttachments(ErrorReport report) {

                /* Attach some text. */
                ErrorAttachmentLog textLog = ErrorAttachmentLog.attachmentWithText("This is a text attachment.", "text.txt");

                /* Attach app icon to test binary. */
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitMapData = stream.toByteArray();
                ErrorAttachmentLog binaryLog = ErrorAttachmentLog.attachmentWithBinary(bitMapData, "icon.jpeg", "image/jpeg");

                /* Return attachments as list. */
                return Arrays.asList(textLog, binaryLog);
            }

            @Override
            public void onBeforeSending(ErrorReport report) {
                Toast.makeText(MainActivity.this, R.string.crash_before_sending, Toast.LENGTH_SHORT).show();
//                crashesIdlingResource.increment();
            }

            @Override
            public void onSendingFailed(ErrorReport report, Exception e) {
                Toast.makeText(MainActivity.this, R.string.crash_sent_failed, Toast.LENGTH_SHORT).show();
//                crashesIdlingResource.decrement();
            }

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            public void onSendingSucceeded(ErrorReport report) {
                String message = String.format("%s\nCrash ID: %s", getString(R.string.crash_sent_succeeded), report.getId());
                if (report.getThrowable() != null) {
                    message += String.format("\nThrowable: %s", report.getThrowable().toString());
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
//                crashesIdlingResource.decrement();
            }
        };
    }
}
