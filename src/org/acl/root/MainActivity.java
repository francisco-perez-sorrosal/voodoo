package org.acl.root;

import static org.acl.root.TwitterOAuthConstants.CONSUMER_KEY;
import static org.acl.root.TwitterOAuthConstants.CONSUMER_SECRET;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

/**
 * Main activity for my blacklist application. Contains the start/stop button 
 * for activating the scanner of incoming calls, and the menu to select the 
 * contacts to filter.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	private static final String TAG = "MainActivity";
	
	private static final int PICK_CONTACT = 0;
	private static final int GET_EMAIL_USER_DATA = 1;
	private static final int GET_TWITTER_ACCESS_TOKEN = 2;

	private ToggleButton startStopTB;
	private CheckBox filterAllCB;
	private ToggleButton emailTB;
	private ToggleButton twitterTB;
	private ListView filteredContactsLV;
	private ArrayAdapter<Contact> filteredContactsAdapter;

	private IncomingCallScanner incomingCallScanner;
	private boolean incomingCallScannerIsBound;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			incomingCallScanner = ((IncomingCallScanner.LocalBinder)service).getService();
			Log.d(TAG, "Observers " + incomingCallScanner.nofObservers());
			if(incomingCallScanner != null) {
				Log.d(TAG, "onServiceConnected: " + incomingCallScanner.isServiceRunning());
				startStopTB.setChecked(true);
				filteredContactsAdapter = new ArrayAdapter<Contact>(getApplicationContext(),
						R.layout.contact_list_item, BlackList.INSTANCE.getBlackListAsArrayList());
				filteredContactsLV.setAdapter(filteredContactsAdapter);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
				incomingCallScannerIsBound = true;
				if(incomingCallScanner.isAllCallsFilterEnabled())
					filterAllCB.setChecked(true);
				if(incomingCallScanner.containsObserver(Mailer.getInstance()))
					emailTB.setChecked(true);
				if(incomingCallScanner.containsObserver(Twitterer.INSTANCE))
					twitterTB.setChecked(true);
			} else {
				Log.d(TAG, "onServiceConnected: IncomingCallScanner is null");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			incomingCallScanner = null;
			incomingCallScannerIsBound = false;
			Log.d(TAG, "onServiceDisconnected: Incoming Call Scanner Disconnected");	    
		}
	};

	// ------------------------- Lifecycle -------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		AdView adView = (AdView)this.findViewById(R.id.adView);
	    adView.loadAd(new AdRequest());
		
		startStopTB = (ToggleButton) findViewById(R.id.startStopTB);
		startStopTB.setOnClickListener(this);
		
		filterAllCB = (CheckBox) findViewById(R.id.filterAllCB);
		filterAllCB.setOnClickListener(this);

		emailTB = (ToggleButton) findViewById(R.id.emailTB);
		emailTB.setOnClickListener(this);
		
		twitterTB = (ToggleButton) findViewById(R.id.twitterTB);
		twitterTB.setOnClickListener(this);

		filteredContactsLV = (ListView) findViewById(R.id.filteredContactsLV);
		filteredContactsLV.setOnItemClickListener(this);

		bindService(new Intent(this, 
				IncomingCallScanner.class), mConnection, Context.BIND_NOT_FOREGROUND);
		
		emailPreferences = getSharedPreferences(EMAIL_PREFS, Activity.MODE_PRIVATE);
		twitterPreferences = getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
		
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Do not call clearTwitterConnection() on destroying this activity
		// cause if twitter is enabled messages must be sent!!!
		// Clean incoming call scanner service connection
		unbindService(mConnection);
		incomingCallScannerIsBound = false;
		Log.d(TAG, "onDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	// ----------------------- END Lifecycle ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
			
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contacts:			
			if(incomingCallScannerIsBound) {
				Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
				startActivityForResult(intentContact, PICK_CONTACT);
			} else {
				Toast.makeText(this, "IncomingCallScanner not bound. Connect first!", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.twitter:
			launchTwitterConfigurationActivity();
			break;
		case R.id.logs:
			if(incomingCallScannerIsBound) {
				UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
						UserNotifier.CallScannerNotification.SHOW_LOG);
			}
			Intent showLogIntent = new Intent(this, ShowLogActivity.class);
			startActivity(showLogIntent);
			break;
		case R.id.email:
			launchEmailUserDataActivity();
			break;
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		
		case PICK_CONTACT:
			if(intent != null) { // This is required because the user cannot select any contact
				Contact  contact = getContactInfo(intent);
				// Your class variables now have the data, so do something with it
				String name = contact.getName();
				if(name !=null & !contact.getPhoneNumbers().isEmpty()) {
					Contact previousContact = BlackList.INSTANCE.addContactToBlackList(contact);
					if(previousContact == null) { // To avoid duplicates
						filteredContactsAdapter.add(contact);
						filteredContactsAdapter.notifyDataSetChanged();
						filteredContactsLV.refreshDrawableState();
						Toast.makeText(this, name + " has been filtered", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "Contact already filtered", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, "Some contact data (Name or Phone Numer) is missing", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "No contact was selected", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case GET_EMAIL_USER_DATA:
			if (resultCode == Activity.RESULT_OK) {
				saveEmailUserDataInAppPreferences(
						(String) intent.getExtras().get(EMAIL),
						(String) intent.getExtras().get(EMAIL_PASSWORD));
			}
			break;
			
		case GET_TWITTER_ACCESS_TOKEN:
			if (resultCode == Activity.RESULT_OK) {
				
			    String oauthVerifier = (String) intent.getExtras().get(TWITTER_OAUTH_VERIFIER);
			 
			    try {
			        // Pair up our request with the response
			    		AccessToken receivedTwitterOAuthAccessToken = 
			    				twitter.getOAuthAccessToken(twitterOAuthRequestToken, oauthVerifier);
			    		// Here we can safely save Access Token and inform the user
			        saveTwitterOAuthAccessTokenInAppPreferences(receivedTwitterOAuthAccessToken);
					Toast.makeText(this, "Now you can connect to Twitter", Toast.LENGTH_SHORT).show();
			    } catch (TwitterException e) {
					Toast.makeText(this, "Error getting the Twitter Access Token", Toast.LENGTH_SHORT).show();
			    }
			    
			} else {
				Toast.makeText(this, "Twitter Authorization Failed", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.startStopTB:
			if(startStopTB.isChecked()) {
				Log.d(TAG, "onClick: starting service");
				Intent intent = new Intent(this, IncomingCallScanner.class);
				startService(intent);
				bindService(new Intent(this, 
						IncomingCallScanner.class), mConnection, Context.BIND_NOT_FOREGROUND);
				Log.d(TAG, "onClick: service started");
			} else {
				Log.d(TAG, "onClick: stopping service");
				clearFilterForAllCallsFromService();
				clearEmailConnectionFromService();
				clearTwitterConnectionFromService();
				stopService(new Intent(this, IncomingCallScanner.class));
				filteredContactsAdapter.clear();
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
				Log.d(TAG, "onClick: service stopped");
			}
			break;
		case R.id.filterAllCB:
			if(filterAllCB.isChecked()) {
				if(incomingCallScannerIsBound) {
					incomingCallScanner.filterAllCalls(true);
				} else {
					filterAllCB.setChecked(false);
				}
			} else {				
				if(incomingCallScannerIsBound) {
					clearFilterForAllCallsFromService();
				} 
			}
			break;
		case R.id.twitterTB:
			if(twitterTB.isChecked()) {
				if(incomingCallScannerIsBound) {
					
					if(!isNetworkAvailable()) {
						Toast.makeText(this, "Network connection not available", Toast.LENGTH_SHORT).show();
						clearTwitterConnectionFromService();
						return;
					}
					
					twitterOAuthConsumerData = new OAuthAccessToken(CONSUMER_KEY, CONSUMER_SECRET); 
					twitterOAuthAccessToken = loadTwitterOAuthAccessTokenFromAppPreferences();
					
					if(isTwitterOAuthConsumerDataValid(twitterOAuthConsumerData)
							&& isTwitterOAuthAccessTokenValid(twitterOAuthAccessToken)) {
						Log.d(TAG, "THIS MUST NOT BE HERE");
						// Both values were stored properly, so...
						incomingCallScanner.addCallObserver(Twitterer.INSTANCE);
						
					} else {
						
						prepareTwitterConnection();
						
					}
				} else {
					twitterTB.setChecked(false);
				}
			} else {
				clearTwitterConnectionFromService();
			}
			break;
		case R.id.emailTB:
			if(emailTB.isChecked()) {
				if(incomingCallScannerIsBound) {
					
					if(!isNetworkAvailable()) {
						Toast.makeText(this, "Network connection not available", Toast.LENGTH_SHORT).show();
						clearEmailConnectionFromService();
						return;
					}
					incomingCallScanner.addCallObserver(Mailer.getInstance());
				} else {
					emailTB.setChecked(false);
				}
			} else {
				clearEmailConnectionFromService();
			}
			break;
		}
	}

	private void clearFilterForAllCallsFromService() {
		incomingCallScanner.filterAllCalls(false);
		filterAllCB.setChecked(false);
	}

	// Get the required elements to get a Twitter connection
	private void prepareTwitterConnection() {
		try {
			// Step 1: Get the request token. If the consumer data is wrong a an exception will be thrown
			twitterOAuthRequestToken = getTwitterOAuthRequestToken();
			// Step 2: Use request token to get access token by means of an intermediate activity
			Toast.makeText(this, "Please, put your Twitter user and password", Toast.LENGTH_SHORT).show();
			launchTwitterWebviewActivity(twitterOAuthRequestToken);
		} catch (TwitterException e) {
			Toast.makeText(this, "Please, set properly your Twitter Consumer Key and Secret on the Main Menu", Toast.LENGTH_SHORT).show();
		} finally {
			twitterTB.setChecked(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Log.d(TAG, "onItemClick");
		final Contact contact = (Contact) filteredContactsLV.getItemAtPosition(position);
		AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
		adb.setTitle("Delete?");
		adb.setMessage("Are you sure you want to delete " + contact.getName());
		adb.setNegativeButton("Cancel", null);
		adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				BlackList.INSTANCE.removeContactFromBlackList(contact);
				filteredContactsAdapter.remove(contact);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
			}});
		adb.show();
	}

	protected Contact getContactInfo(Intent intent) {

		Contact contact = null;
		
		Cursor cursor =  managedQuery(intent.getData(), null, null, null, null);     
		startManagingCursor(cursor);
		Log.d(TAG, "getContactInfo. Elements: " + cursor.getCount());

		while (cursor.moveToNext()) {           
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); 

			Contact.Builder contactBuilder = new Contact.Builder(contactId, name);

			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false" ;

			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
				while (phones.moveToNext()) {
					String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					// When receiving a call the number includes spaces, brackets and hyphens.
					phoneNumber = phoneNumber.replaceAll("[\\s\\-()]", "");
					contactBuilder.addPhoneNumber(phoneNumber);
					Log.d(TAG, "getContactInfo. Phone Number: " + phoneNumber);
				}
				phones.close();
			}

			// Find Email Addresses
			Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
			while (emails.moveToNext()) {
				String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				contactBuilder.addEmailAddress(emailAddress);
			}
			emails.close();

			Cursor address = getContentResolver().query(
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + contactId,
					null, null);
			while (address.moveToNext()) { 
				// These are all private class variables, don't forget to create them.
				contactBuilder.poBox(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)));
				contactBuilder.street(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				contactBuilder.city(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				contactBuilder.state(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
				contactBuilder.postalCode(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				contactBuilder.country(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
				contactBuilder.type(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)));
			}  //address.moveToNext()   

			contact = contactBuilder.build();
		}  //while (cursor.moveToNext())        
		cursor.close();
		stopManagingCursor(cursor);
		return contact;
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	// ------------------------- Twitter Related Stuff ------------------------
	
	protected static final String TWITTER_PREFS = "twitter_preferences";
	
	protected static final String TWITTER_OAUTH_CONSUMER_KEY = "twitter_oauth_consumer_key";
	protected static final String TWITTER_OAUTH_CONSUMER_SECRET = "twitter_oauth_consumer_secret";
	
	private static final String TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	
	private static final String TWITTER_OAUTH_ACCESS_TOKEN = "twitter_access_token";
	private static final String TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	
	protected static final String TWITTER_MESSAGE = "twitter_message";
	
	private  SharedPreferences twitterPreferences;
	
	private Twitter twitter;
	private RequestToken twitterOAuthRequestToken;
	private OAuthAccessToken twitterOAuthConsumerData;
	private OAuthAccessToken twitterOAuthAccessToken;
	
	private RequestToken getTwitterOAuthRequestToken() throws TwitterException {
		
		twitter = new TwitterFactory().getInstance();
		Log.d(TAG, "Token " + twitterOAuthConsumerData.getToken() + " Token Secret " + twitterOAuthConsumerData.getTokenSecret());
		twitter.setOAuthConsumer(twitterOAuthConsumerData.getToken(), twitterOAuthConsumerData.getTokenSecret()); 
		
		String callbackURL = getResources().getString(R.string.twitter_callback); 
		// This can throw TwitterException if the oauthConsumerData is not correct
        return twitter.getOAuthRequestToken(callbackURL);
	}
		
	private void clearTwitterConnectionFromService() {
			if(incomingCallScannerIsBound)
				incomingCallScanner.removeCallObserver(Twitterer.INSTANCE);
			
			twitterOAuthConsumerData = null;
			twitterOAuthRequestToken = null;
			twitterOAuthAccessToken =  null;
			twitter = null;
			twitterTB.setChecked(false);
	}
	
	private boolean isTwitterOAuthConsumerDataValid(OAuthAccessToken oauthConsumerData) {
		return (!oauthConsumerData.getToken().equals("") && !oauthConsumerData.getTokenSecret().equals(""));
	}
	
	private boolean isTwitterOAuthAccessTokenValid(OAuthAccessToken oauthAccessToken) {
		return (!oauthAccessToken.getToken().equals("") && !oauthAccessToken.getTokenSecret().equals(""));
	}
	
	private OAuthAccessToken loadTwitterOAuthAccessTokenFromAppPreferences() {
		Log.d(TAG, "onClick: inTwitterAT");
		return new OAuthAccessToken(
				twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN, "" )
				,twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, "" ));
	}

	private void saveTwitterOAuthAccessTokenInAppPreferences(AccessToken accessToken) {
		twitterPreferences.edit()
		    .putString(TWITTER_OAUTH_ACCESS_TOKEN, accessToken.getToken())
		    .putString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, accessToken.getTokenSecret())
		    .commit();
		Toast.makeText(this, "Twitter OAuth Access Token saved in Preferences", Toast.LENGTH_SHORT).show();
	}
	
	private  void launchTwitterWebviewActivity(RequestToken twitterOAuthRequestToken) {
		Intent i = new Intent(this, TwitterWebviewActivity.class);
		i.putExtra("URL", twitterOAuthRequestToken.getAuthenticationURL());
		startActivityForResult(i, GET_TWITTER_ACCESS_TOKEN);
	}

	private  void launchTwitterConfigurationActivity() {
		Intent i = new Intent(this, TwitterConfigurationActivity.class);
		startActivity(i);
	}

	// ----------------------- To Store Twitter Tokens ------------------------
	
	private class OAuthAccessToken {
		
		private String token;
		private String tokenSecret;
		
		public OAuthAccessToken(String token, String tokenSecret) {
			super();
			this.token = token;
			this.tokenSecret = tokenSecret;
		}
		
		public String getToken() {
			return token;
		}
		
		public String getTokenSecret() {
			return tokenSecret;
		}
	}
	
	// ---------------------- Mail related stuff ----------------------------
	
	protected static final String EMAIL_PREFS = "email_preferences";
	
	protected static final String EMAIL = "email_user";
	protected static final String EMAIL_PASSWORD = "email_password";

	private  SharedPreferences emailPreferences;
	
	private void launchEmailUserDataActivity() {
		Intent i = new Intent(this, EmailUserDataActivity.class);
		startActivityForResult(i, GET_EMAIL_USER_DATA);
	}

	private void saveEmailUserDataInAppPreferences(String email, String password) {
		emailPreferences.edit()
		    .putString(EMAIL, email)
		    .putString(EMAIL_PASSWORD, password)
		    .commit();
		Toast.makeText(this, "Email user data saved in Preferences", Toast.LENGTH_LONG).show();
	}
	
	private void clearEmailConnectionFromService() {
		if(incomingCallScannerIsBound) {
			incomingCallScanner.removeCallObserver(Mailer.getInstance());
		}
		emailTB.setChecked(false);
	}
	
	// ---------------------- End Mail related stuff -------------------------
	
}
