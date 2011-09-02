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
 * Activity to capture the Email User Data.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class EmailConfigurationActivity extends Activity implements OnClickListener {
	
	private EditText email;
	private EditText password;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences emailPreferences;

	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.email_user_data);
	    
	    emailPreferences = getApplicationContext().getSharedPreferences(MainActivity.EMAIL_PREFS, Activity.MODE_PRIVATE);
	    email = (EditText) findViewById(R.id.emailT);
	    email.setText(emailPreferences.getString(MainActivity.EMAIL, ""));
		password = (EditText) findViewById(R.id.emailPasswordT);
		password.setText(emailPreferences.getString(MainActivity.EMAIL_PASSWORD, ""));
		
		doneB = (Button) findViewById(R.id.emailDoneB);
		doneB.setOnClickListener(this);
		clearB = (Button) findViewById(R.id.emailClearB);
		clearB.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		
		if (view == findViewById(R.id.emailDoneB)) {
			
			String consumerKeyText = email.getText().toString();
			String consumerSecretText = password.getText().toString();
			
			if(!consumerKeyText.equals("") && !consumerSecretText.equals("")) {
				Intent intent = getIntent();
				intent.putExtra(MainActivity.EMAIL, email.getText().toString());
				intent.putExtra(MainActivity.EMAIL_PASSWORD, password.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Toast.makeText(this, 
						getResources().getString(R.string.email_wrong_user_data), 
						Toast.LENGTH_LONG)
						.show();
			}
			
		} else
			
		if (view == findViewById(R.id.emailClearB)) {
			email.setText("");
			password.setText("");
			email.requestFocus();
			//emailPreferences.edit() // Do not clear preferences if they exists
			//	.putString(MainActivity.TWITTER_OAUTH_CONSUMER_KEY, "")
			//	.putString(MainActivity.TWITTER_OAUTH_CONSUMER_SECRET, "")
			//	.commit();
		}
		
	}

}
