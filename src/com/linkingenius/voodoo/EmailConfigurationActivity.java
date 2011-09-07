package com.linkingenius.voodoo;

import android.app.Activity;
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
	
	private static final String EMAIL_PREFS = "email_preferences";
	private static final String EMAIL = "email_user";
	private static final String EMAIL_PASSWORD = "email_password";
	
	private EditText email;
	private EditText password;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences emailPreferences;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.email_user_data);
	    
	    emailPreferences = getSharedPreferences(EMAIL_PREFS, Activity.MODE_PRIVATE);
	    email = (EditText) findViewById(R.id.emailT);
	    email.setText(emailPreferences.getString(EMAIL, ""));
		password = (EditText) findViewById(R.id.emailPasswordT);
		password.setText(emailPreferences.getString(EMAIL_PASSWORD, ""));
		
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
			
			if(!emailText.equals("") && !passwordText.equals("")) {
				saveEmailUserDataInAppPreferences(emailText, passwordText);
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
	
	private void saveEmailUserDataInAppPreferences(String email, String password) {
		emailPreferences.edit()
		    .putString(EMAIL, email)
		    .putString(EMAIL_PASSWORD, password)
		    .commit();
		Toast.makeText(this, getResources().getString(R.string.email_prefs_saved), Toast.LENGTH_LONG).show();
	}

}
