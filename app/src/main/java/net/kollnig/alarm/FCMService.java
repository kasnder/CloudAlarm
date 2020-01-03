package net.kollnig.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.core.app.NotificationCompat;

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

		if (data.get("hours") == null
				|| data.get("minutes") == null)
			return;

		int hours = Integer.parseInt(data.get("hours"));
		int minutes = Integer.parseInt(data.get("minutes"));
		startActivity(setAlarm(hours, minutes));

		// Show notification
		Intent alarmClockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, alarmClockIntent, 0);

		String channelId = getString(R.string.channel_id);
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this, channelId)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle(getString(R.string.notificationTitle))
						.setContentText(getString(R.string.notificationBody))
						.setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
						.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	/**
	 * Set system alarm clock.
	 *
	 * @param hours   Hours
	 * @param minutes Minutes
	 */
	public Intent setAlarm (int hours, int minutes) {
		Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Cloud Alarm");
		intent.putExtra(AlarmClock.EXTRA_HOUR, hours);
		intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
		intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

		return intent;
	}
}
