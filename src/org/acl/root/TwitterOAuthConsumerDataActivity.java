package org.acl.root;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity to capture the Twitter OAuth Consumer Data.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class TwitterOAuthConsumerDataActivity extends Activity implements OnClickListener {
	
	private EditText consumerKey;
	private EditText consumerSecret;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences twitterPreferences;

	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter_oauth_consumer_data);
	    
	    twitterPreferences = getApplicationContext().getSharedPreferences(MainActivity.TWITTER_PREFS, Activity.MODE_PRIVATE);
	    consumerKey = (EditText) findViewById(R.id.consumerKeyT);
	    consumerKey.setText(twitterPreferences.getString(MainActivity.TWITTER_OAUTH_CONSUMER_KEY, ""));
		consumerSecret = (EditText) findViewById(R.id.consumerSecretT);
		consumerSecret.setText(twitterPreferences.getString(MainActivity.TWITTER_OAUTH_CONSUMER_SECRET, ""));
		
		doneB = (Button) findViewById(R.id.oauthDoneB);
		doneB.setOnClickListener(this);
		clearB = (Button) findViewById(R.id.oauthClearB);
		clearB.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		
		if (view == findViewById(R.id.oauthDoneB)) {
			
			String consumerKeyText = consumerKey.getText().toString();
			String consumerSecretText = consumerSecret.getText().toString();
			
			if(!consumerKeyText.equals("") && !consumerSecretText.equals("")) {
				Intent intent = getIntent();
				intent.putExtra(MainActivity.TWITTER_OAUTH_CONSUMER_KEY, consumerKey.getText().toString());
				intent.putExtra(MainActivity.TWITTER_OAUTH_CONSUMER_SECRET, consumerSecret.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Toast.makeText(this, 
						getResources().getString(R.string.twitter_wrong_consumer_data), 
						Toast.LENGTH_SHORT)
						.show();
			}
			
		} else
			
		if (view == findViewById(R.id.oauthClearB)) {
			consumerKey.setText("");
			consumerSecret.setText("");
			consumerKey.requestFocus();
			//twitterPreferences.edit() // Do not clear preferences if they exists
			//	.putString(MainActivity.TWITTER_OAUTH_CONSUMER_KEY, "")
			//	.putString(MainActivity.TWITTER_OAUTH_CONSUMER_SECRET, "")
			//	.commit();
		}
		
	}

}
