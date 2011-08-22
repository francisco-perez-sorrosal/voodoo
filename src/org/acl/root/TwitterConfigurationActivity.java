package org.acl.root;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity to capture the Twitter OAuth Consumer Data.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class TwitterConfigurationActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "TwitterConfigurationActivity";
	private static final String DEFAULT_TWITTER_MESSAGE = "Sorry, the person you are trying to communicate with is busy.";
	
	private EditText twitterMessage;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences twitterPreferences;

	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter_configuration);
	    
	    twitterPreferences = getApplicationContext().getSharedPreferences(MainActivity.TWITTER_PREFS, Activity.MODE_PRIVATE);
	    twitterMessage = (EditText) findViewById(R.id.twitterMessage);
	    twitterMessage.setText(twitterPreferences.getString(MainActivity.TWITTER_MESSAGE, ""));
		
		doneB = (Button) findViewById(R.id.twitterConfigurationDoneB);
		doneB.setOnClickListener(this);
		clearB = (Button) findViewById(R.id.twitterConfigurationClearB);
		clearB.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		
		if (view == findViewById(R.id.twitterConfigurationDoneB)) {
			String message = twitterMessage.getText().toString();
			
			if(message == null || message.equals(""))
				message = DEFAULT_TWITTER_MESSAGE;
			
			Log.d(TAG, "Saving Tweet: " + message);
			
			twitterPreferences.edit()
				.putString(MainActivity.TWITTER_MESSAGE, message)
				.commit();
			TwitterHelper.getInstance(getApplicationContext()).setTweet(message);
			finish();
		} else
			
		if (view == findViewById(R.id.twitterConfigurationClearB)) {
			twitterMessage.setText("");
			twitterMessage.requestFocus();
		}
		
	}

}
