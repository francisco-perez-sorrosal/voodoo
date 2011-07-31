package org.acl.root;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserNotification {
	
	protected static void showToastWithImage(Context context, CharSequence textToShow, int imageResourceId) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.toast_layout, null);

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(imageResourceId);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(textToShow);

		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	public static final int SVC_STARTED_NOTIFICATION = 0;
	
	public static void showNotification(Context context) {
		NotificationManager mNotificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.skullnbones;
		CharSequence tickerText = "Starting Incoming Call Scanner";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.DEFAULT_VIBRATE | 
				Notification.FLAG_NO_CLEAR;
		long[] vibrate = {0,100,200,300};
		notification.vibrate = vibrate;

		CharSequence contentTitle = "Incoming Call Scanner";
		CharSequence contentText = "The service is filtering inconming calls from jerks!";
		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(SVC_STARTED_NOTIFICATION, notification);
	}

	public static void cancelNotification(Context context, int notification) {
		NotificationManager mNotificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(notification);
	}
}
