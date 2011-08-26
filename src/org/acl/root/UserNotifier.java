package org.acl.root;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public enum UserNotifier implements CallObserver {

	INSTANCE;
	
	private static final String TAG = "UserHelper";

	@Override
	public void callNotification(CallInfo callInfo) {
		CharSequence text = 
				callInfo.getTime() + " "
				+ callInfo.getContext().getResources().getString(R.string.call_message_1)
				+ " " + callInfo.getCaller() + " " 
				+ callInfo.getContext().getResources().getString(R.string.call_message_2);
		showToastWithImage(callInfo.getContext(), text, R.drawable.app_icon);
		Log.d(TAG, "User notified.");
	}

	private void showToastWithImage(Context context, CharSequence textToShow, int imageResourceId) {
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
}
