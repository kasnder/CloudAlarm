package net.kollnig.alarm;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class ApplicationEx extends Application {
	private static final String TAG = "CloudAlarm.App";

	@Override
	public void onCreate () {
		super.onCreate();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			createNotificationChannels();
	}

	@TargetApi(Build.VERSION_CODES.O)
	private void createNotificationChannels () {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationChannel channel = new NotificationChannel(
				getString(R.string.channel_id),
				getString(R.string.channel_name),
				NotificationManager.IMPORTANCE_DEFAULT);
		nm.createNotificationChannel(channel);
	}
}
