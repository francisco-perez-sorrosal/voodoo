package com.linkingenius.voodoo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.linkingenius.voodoo.observers.Mailer;

/**
 * Activity to capture the Email User Data.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class EmailConfigurationActivity extends Activity implements OnClickListener {
	
	private static final String EMAIL_PREFS = "email_preferences";
	private static final String EMAIL = "email_user";
	private static final String EMAIL_PASSWORD = "email_password";
	
	private static final String EMAIL_MESSAGE = "email_message";
	private static final String DEFAULT_EMAIL_MESSAGE = "The person you are trying to communicate is busy at this time. Please call him late.\n\nSent with #VooDooCallKiller. Find me in Android Market: http://tiny.cc/voodoocallkiller";

	
	private EditText email;
	private EditText password;
	private EditText emailMessage;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences emailPreferences;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.email_configuration);
	    
		AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest request = new AdRequest();
	    request.addTestDevice(AdRequest.TEST_EMULATOR);
	    request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");
	    adView.loadAd(request);
	    
	    emailPreferences = getSharedPreferences(EMAIL_PREFS, Activity.MODE_PRIVATE);
	    email = (EditText) findViewById(R.id.emailT);
	    email.setText(emailPreferences.getString(EMAIL, ""));
		password = (EditText) findViewById(R.id.emailPasswordT);
		password.setText(emailPreferences.getString(EMAIL_PASSWORD, ""));
		emailMessage = (EditText) findViewById(R.id.emailMessageT);
	    emailMessage.setText(emailPreferences.getString(EMAIL_MESSAGE, DEFAULT_EMAIL_MESSAGE));
		
		doneB = (Button) findViewById(R.id.emailDoneB);
		doneB.setOnClickListener(this);
		clearB = (Button) findViewById(R.id.emailClearB);
		clearB.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		
		if (view == findViewById(R.id.emailDoneB)) {
			
			String emailText = email.getText().toString();
			String passwordText = password.getText().toString();
			String emailMessageText = emailMessage.getText().toString();
			
			if(!emailText.equals("") && !passwordText.equals("")) {
				saveEmailUserDataInAppPreferences(emailText, passwordText, emailMessageText);
				Mailer.INSTANCE.setBody(emailMessageText);
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
		}
		
	}
	
	private void saveEmailUserDataInAppPreferences(String email, String password, String emailMessage) {
		emailPreferences.edit()
		    .putString(EMAIL, email)
		    .putString(EMAIL_PASSWORD, password)
   		    .putString(EMAIL_MESSAGE, emailMessage)
		    .commit();
		Toast.makeText(this, getResources().getString(R.string.email_prefs_saved), Toast.LENGTH_LONG).show();
	}

}
