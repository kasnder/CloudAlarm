package net.kollnig.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {
	public static final int NOTIFICATION_ID = 0;

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	@Override
	public void onMessageReceived (RemoteMessage remoteMessage) {
		// Set alarm
		Map<String, String> data = remoteMessage.getData();

		if (!data.containsKey("hours") || !data.containsKey("minutes")) {
			return;
		}

		int hours = Integer.parseInt(data.get("hours"));
		int minutes = Integer.parseInt(data.get("minutes"));
		setAlarm(hours, minutes);

		// Show notification
		Intent alarmClockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmClockIntent, 0);

		String channelId = getString(R.string.default_notification_channel_id);
		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(this, channelId)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle(getString(R.string.notificationTitle))
						.setContentText(getString(R.string.notificationBody))
						.setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
						.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Since android Oreo notification channel is needed.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId,
					getString(R.string.default_notification_channel_name),
					NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}

		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	/**
	 * Set system alarm clock.
	 *
	 * @param hours   Hours
	 * @param minutes Minutes
	 */
	public void setAlarm (int hours, int minutes) {
		Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);

		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		i.putExtra(AlarmClock.EXTRA_MESSAGE, "Cloud Alarm");
		i.putExtra(AlarmClock.EXTRA_HOUR, hours);
		i.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
		i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

		startActivity(i);
	}
}
