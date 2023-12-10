package com.example.notetakingapplication;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int PERMISSION_REQUEST_CODE = 123;
    MediaPlayer mp;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            // Nếu không có quyền, yêu cầu quyền từ người dùng
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Nếu đã có quyền, hiển thị thông báo
            Intent nextActivity = new Intent(context, activity_notification.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nextActivity, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notify")
                    .setSmallIcon(R.drawable.alarm)
                    .setContentTitle("Reminder")
                    .setContentText("It's time to wake up")
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(123, builder.build());

            // Ví dụ
            Log.d("AlarmReceiver", "onReceive: Báo thức được kích hoạt");
        }
    }
}