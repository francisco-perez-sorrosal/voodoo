package com.linkingenius.voodoo.observers;

import static com.linkingenius.voodoo.TwitterOAuthConstants.CONSUMER_KEY;
import static com.linkingenius.voodoo.TwitterOAuthConstants.CONSUMER_SECRET;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.linkingenius.voodoo.R;
import com.linkingenius.voodoo.utils.CallInfo;

public enum Twitterer implements CallObserver {
	
	INSTANCE;
	
	private static final String TAG = "Twitterer";
	
	private static final String TWITTER_PREFS = "twitter_preferences";
	// Max length is 120 (and not 140) because the date/time is also inserted
	private static final int MAX_TWEET_LENGTH = 120;
	
	private static final String ACCESS_TOKEN = "twitter_access_token";
	private static final String ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	
	private String defaultTweet = "Sorry, the person you are trying to communicate with is busy";
	
	private Twitter twitter = null;
			
	public boolean setDefaultTweet(String text) {
		
		if (text.length() > MAX_TWEET_LENGTH) {
			defaultTweet = text.substring(0, MAX_TWEET_LENGTH);
			return false;
		} else {
			defaultTweet = text;
			return true;
		}
	}
	
	@Override
	public void callNotification(CallInfo callInfo) {
		if(twitter == null) {
			SharedPreferences twitterPreferences = 
					callInfo.getContext().getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
			
			String accessToken =  twitterPreferences.getString(ACCESS_TOKEN, "" );
			String accessTokenSecret = twitterPreferences.getString(ACCESS_TOKEN_SECRET, "" );
			
			if(!accessToken.equals("") && !accessTokenSecret.equals("")) {
				Configuration conf = new ConfigurationBuilder()
				.setOAuthConsumerKey(CONSUMER_KEY)
				.setOAuthConsumerSecret(CONSUMER_SECRET)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret)
				.build();
				twitter =  new TwitterFactory(conf).getInstance();
			}
		}
		
		try {
			Log.d(TAG, "Sending defaultTweet: " + defaultTweet);
			twitter.updateStatus(callInfo.getDate() + " " + callInfo.getTime() + " " + defaultTweet);
			Log.d(TAG, "Tweet sent");
		} catch (Exception e) { // TwitterException || NullPointerException
			Log.e(TAG, "Can't send Tweet", e);
			Toast.makeText(callInfo.getContext(), R.string.tweet_not_sent, Toast.LENGTH_SHORT).show();
		}	
	}

}
