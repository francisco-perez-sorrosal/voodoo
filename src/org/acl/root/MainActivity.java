package org.acl.root;

import static org.acl.root.TwitterOAuthConstants.CONSUMER_KEY;
import static org.acl.root.TwitterOAuthConstants.CONSUMER_SECRET;

import java.util.ArrayList;

import org.acl.root.core.BlackList;
import org.acl.root.observers.Mailer;
import org.acl.root.observers.Twitterer;
import org.acl.root.observers.UserNotifier;
import org.acl.root.utils.AboutDialogBuilder;
import org.acl.root.utils.Contact;
import org.acl.root.utils.Tools;

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
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			incomingCallScanner = ((IncomingCallScanner.LocalBinder)service).getService();
			Log.d(TAG, "Observers " + incomingCallScanner.nofObservers());
			if(incomingCallScanner != null) {
				startStopTB.setChecked(true);
				filteredContactsAdapter = new DualLineArrayAdapter(getApplicationContext(),
						R.layout.contact_list_item, BlackList.INSTANCE.getBlackListAsArrayList());
				filteredContactsLV.setAdapter(filteredContactsAdapter);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
				if(incomingCallScanner.isAllCallsFilterEnabled())
					filterAllCB.setChecked(true);
				if(incomingCallScanner.containsObserver(Mailer.INSTANCE))
					emailTB.setChecked(true);
				if(incomingCallScanner.containsObserver(Twitterer.INSTANCE))
					twitterTB.setChecked(true);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected: Incoming Call Scanner Disconnected");
			incomingCallScanner = null;
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

		// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND 
		bindService(new Intent(this, 
				IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
		
		emailPreferences = getSharedPreferences(EMAIL_PREFS, Activity.MODE_PRIVATE);
		twitterPreferences = getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
		
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Don't call clearTwitter/MailConnection() on destroying this activity
		// cause, if enabled, messages must be sent!!!
		unbindService(mConnection);
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
			if(incomingCallScanner != null) {
				Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
				startActivityForResult(intentContact, PICK_CONTACT);
			} else {
				Toast.makeText(this, getResources().getString(R.string.ics_service_not_bound), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.twitter:
			launchTwitterConfigurationActivity();
			break;
		case R.id.logs:
			if(incomingCallScanner != null)
				UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
						UserNotifier.CallScannerNotification.SHOW_LOG);
			Intent showLogIntent = new Intent(this, ShowLogActivity.class);
			startActivity(showLogIntent);
			break;
		case R.id.email:
			launchEmailUserDataActivity();
			break;
		case R.id.about:
			AlertDialog builder;
			try {
				builder = AboutDialogBuilder.create(this);
				builder.show();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			break;
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		
		case PICK_CONTACT:
			if(intent != null) { // This is required because the user cannot select any contact
				Contact  contact = Contact.getContactFromAndroidUserContacts(this, intent.getData().getLastPathSegment());
				// Your class variables now have the data, so do something with it
				String name = contact.getName();
				if(name !=null & !contact.getPhoneNumbers().isEmpty()) {
					Contact previousContact = BlackList.INSTANCE.addContactToBlackList(contact);
					if(previousContact == null) { // To avoid duplicates
						filteredContactsAdapter.add(contact);
						filteredContactsAdapter.notifyDataSetChanged();
						filteredContactsLV.refreshDrawableState();
						Log.d(TAG, name + " has been filtered");
					} else {
						Toast.makeText(this, getResources().getString(R.string.contact_already_filtered), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, getResources().getString(R.string.missing_contact_data), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, getResources().getString(R.string.no_contact_selected), Toast.LENGTH_SHORT).show();
			}
			break;
			
		case GET_EMAIL_USER_DATA:
			if (resultCode == Activity.RESULT_OK)
				saveEmailUserDataInAppPreferences(
						(String) intent.getExtras().get(EMAIL),
						(String) intent.getExtras().get(EMAIL_PASSWORD));
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
					Toast.makeText(this, getResources().getString(R.string.no_contact_selected), Toast.LENGTH_SHORT).show();
			    } catch (TwitterException e) {
					Toast.makeText(this, getResources().getString(R.string.twitter_at_error), Toast.LENGTH_SHORT).show();
			    }
			    
			} else {
				Toast.makeText(this, getResources().getString(R.string.twitter_auth_failed), Toast.LENGTH_SHORT).show();
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
				// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND 
				bindService(new Intent(this, 
						IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
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
				if(incomingCallScanner != null) {
					incomingCallScanner.filterAllCalls(true);
				} else {
					filterAllCB.setChecked(false);
				}
			} else {				
				if(incomingCallScanner != null)
					clearFilterForAllCallsFromService();
			}
			break;
		case R.id.twitterTB:
			if(twitterTB.isChecked()) {
				if(incomingCallScanner != null) {
					
					if(!Tools.isNetworkAvailable(getApplicationContext())) {
						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
						clearTwitterConnectionFromService();
						return;
					}
					
					twitterOAuthConsumerData = new OAuthAccessToken(CONSUMER_KEY, CONSUMER_SECRET); 
					twitterOAuthAccessToken = loadTwitterOAuthAccessTokenFromAppPreferences();
					
					if(isTwitterOAuthConsumerDataValid(twitterOAuthConsumerData)
							&& isTwitterOAuthAccessTokenValid(twitterOAuthAccessToken)) {
						// Both values were stored properly, so...
						incomingCallScanner.addCallObserver(Twitterer.INSTANCE);
						
					} else {
						prepareTwitterConnection();
					}
				} else {
					twitterTB.setChecked(false);
				}
			} else {
				if(incomingCallScanner != null)
					clearTwitterConnectionFromService();
			}
			break;
		case R.id.emailTB:
			if(emailTB.isChecked()) {
				if(incomingCallScanner != null) {
					
					if(!Tools.isNetworkAvailable(getApplicationContext())) {
						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
						clearEmailConnectionFromService();
						return;
					}
					//incomingCallScanner.addCallObserver(Mailer.getInstance());
					incomingCallScanner.addCallObserver(Mailer.INSTANCE);
				} else {
					emailTB.setChecked(false);
				}
			} else {
				if(incomingCallScanner != null) 
					clearEmailConnectionFromService();
			}
			break;
		}
	}

	private void clearFilterForAllCallsFromService() {
		incomingCallScanner.filterAllCalls(false);
		filterAllCB.setChecked(false);
	}

	private void prepareTwitterConnection() {
		try {
			// Step 1: Get the request token. If the consumer data is wrong a an exception will be thrown
			twitterOAuthRequestToken = getTwitterOAuthRequestToken();
			// Step 2: Use request token to get access token by means of an intermediate activity
			Toast.makeText(this, getResources().getString(R.string.twitter_enter_consumer_data), Toast.LENGTH_SHORT).show();
			launchTwitterWebviewActivity(twitterOAuthRequestToken);
		} catch (TwitterException e) {
			Toast.makeText(this, getResources().getString(R.string.twitter_set_right_consumer_data), Toast.LENGTH_SHORT).show();
		} finally {
			twitterTB.setChecked(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Log.d(TAG, "onItemClick");
		final Contact contact = (Contact) filteredContactsLV.getItemAtPosition(position);
		AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(getResources().getString(R.string.delete_op));
		adb.setMessage("Are you sure you want to delete " + contact.getName());
		adb.setPositiveButton(getResources().getString(R.string.ok_tag), new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				BlackList.INSTANCE.removeContactFromBlackList(contact);
				filteredContactsAdapter.remove(contact);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
			}});
		adb.setNegativeButton(getResources().getString(R.string.cancel_tag), null);
		adb.show();
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
		return new OAuthAccessToken(
				twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN, "" )
				,twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, "" ));
	}

	private void saveTwitterOAuthAccessTokenInAppPreferences(AccessToken accessToken) {
		twitterPreferences.edit()
		    .putString(TWITTER_OAUTH_ACCESS_TOKEN, accessToken.getToken())
		    .putString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, accessToken.getTokenSecret())
		    .commit();
		Toast.makeText(this, getResources().getString(R.string.twitter_prefs_saved), Toast.LENGTH_SHORT).show();
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
		Intent i = new Intent(this, EmailConfigurationActivity.class);
		startActivityForResult(i, GET_EMAIL_USER_DATA);
	}

	private void saveEmailUserDataInAppPreferences(String email, String password) {
		emailPreferences.edit()
		    .putString(EMAIL, email)
		    .putString(EMAIL_PASSWORD, password)
		    .commit();
		Toast.makeText(this, getResources().getString(R.string.email_prefs_saved), Toast.LENGTH_LONG).show();
	}
	
	private void clearEmailConnectionFromService() {
		//incomingCallScanner.removeCallObserver(Mailer.getInstance());
		incomingCallScanner.removeCallObserver(Mailer.INSTANCE);
		emailTB.setChecked(false);
	}
	
	// -------------------------- Private classes -----------------------------
	
	/**
	 * Allows to present the contents of the Blacklist as well-defined rows
	 * 
	 * Francisco PŽrez-Sorrosal (fperez)
	 *
	 */
	private class DualLineArrayAdapter extends ArrayAdapter<Contact> {

		private ArrayList<Contact> contacts;

	    public DualLineArrayAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts) {
	            super(context, textViewResourceId, contacts);
	            this.contacts = contacts;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View v = convertView;
	            if (v == null) {
	                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate(R.layout.contact_list_item, null);
	            }
	            Contact contact = contacts.get(position);
	            if (contact != null) {
	        			ImageView contactPhoto = (ImageView) v.findViewById(R.id.contactPhoto);
	                TextView contactName = (TextView) v.findViewById(R.id.contactName);
	                TextView contactPhones = (TextView) v.findViewById(R.id.contactPhones);
	                
	                if (contactPhoto != null) {
	                      contactPhoto.setImageBitmap(contact.getPhoto());                            
	                }
	                if (contactName != null) {
	                      contactName.setText(contact.getName());                            
	                }
	                if(contactPhones != null){
	                      contactPhones.setText(contact.getPhoneNumbers().toString());
	                }
	            }
	            return v;
	    }
	}
}
