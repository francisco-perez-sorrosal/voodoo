package org.acl.root;

import static org.acl.root.TwitterOAuthConstants.CONSUMER_KEY;
import static org.acl.root.TwitterOAuthConstants.CONSUMER_SECRET;

import org.acl.root.observers.Twitterer;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity to capture the Twitter OAuth Consumer Data and other related stuff
 * such as default twitter message.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class TwitterConfigurationActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "TwitterConfigurationActivity";
	
	private static final int GET_TWITTER_ACCESS_TOKEN = 0;
	
	private static final String TWITTER_PREFS = "twitter_preferences";
	
	private static final String TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	
	private static final String TWITTER_OAUTH_ACCESS_TOKEN = "twitter_access_token";
	private static final String TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	
	private static final String TWITTER_MESSAGE = "twitter_message";
	private static final String DEFAULT_TWITTER_MESSAGE = "Sorry, the person you are trying to communicate with is busy";
	
	private EditText twitterMessage;
	private Button doneB;
	private Button clearB;
	
	private SharedPreferences twitterPreferences;

	private Twitter twitter;
	private RequestToken twitterOAuthRequestToken;
	private OAuthAccessToken twitterOAuthConsumerData;
	private OAuthAccessToken twitterOAuthAccessToken;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter_configuration);
	    
		doneB = (Button) findViewById(R.id.twitterConfigurationDoneB);
		doneB.setOnClickListener(this);
		clearB = (Button) findViewById(R.id.twitterConfigurationClearB);
		clearB.setOnClickListener(this);
		
	    twitterPreferences = getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
	    twitterMessage = (EditText) findViewById(R.id.twitterMessage);
	    twitterMessage.setText(twitterPreferences.getString(TWITTER_MESSAGE, ""));
	    
	    twitterOAuthConsumerData = new OAuthAccessToken(CONSUMER_KEY, CONSUMER_SECRET); 
		twitterOAuthAccessToken = loadTwitterOAuthAccessTokenFromAppPreferences();
		
		if(!isTwitterOAuthConsumerDataValid(twitterOAuthConsumerData)
				|| !isTwitterOAuthAccessTokenValid(twitterOAuthAccessToken)) {
			prepareTwitterConnection();
		}
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
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
	public void onClick(View view) {
		
		if (view == findViewById(R.id.twitterConfigurationDoneB)) {
			String message = twitterMessage.getText().toString();
			
			if(message == null || message.equals(""))
				message = DEFAULT_TWITTER_MESSAGE;
			
			Log.d(TAG, "Saving Tweet: " + message);
			
			twitterPreferences.edit()
				.putString(TWITTER_MESSAGE, message)
				.commit();
			Twitterer.INSTANCE.setDefaultTweet(message);
			finish();
		} else
			
		if (view == findViewById(R.id.twitterConfigurationClearB)) {
			twitterMessage.setText("");
			twitterMessage.requestFocus();
		}
		
	}
	
	// --------------------------- Private methods ---------------------------
	
	private void prepareTwitterConnection() {
		try {
			// Step 1: Get the request token. If the consumer data is wrong a an exception will be thrown
			twitterOAuthRequestToken = getTwitterOAuthRequestToken();
			// Step 2: Use request token to get access token by means of an intermediate activity
			Toast.makeText(this, getResources().getString(R.string.twitter_enter_consumer_data), Toast.LENGTH_SHORT).show();
			launchTwitterWebviewActivity(twitterOAuthRequestToken);
		} catch (TwitterException e) {
			Toast.makeText(this, getResources().getString(R.string.twitter_set_right_consumer_data), Toast.LENGTH_SHORT).show();
		}
	}
	
	private RequestToken getTwitterOAuthRequestToken() throws TwitterException {
		
		twitter = new TwitterFactory().getInstance();
		Log.d(TAG, "Token " + twitterOAuthConsumerData.getToken() + " Token Secret " + twitterOAuthConsumerData.getTokenSecret());
		twitter.setOAuthConsumer(twitterOAuthConsumerData.getToken(), twitterOAuthConsumerData.getTokenSecret()); 
		
		String callbackURL = getResources().getString(R.string.twitter_callback); 
		// This can throw TwitterException if the oauthConsumerData is not correct
        return twitter.getOAuthRequestToken(callbackURL);
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

}
