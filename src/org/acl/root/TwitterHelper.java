package org.acl.root;

import static org.acl.root.TwitterOAuthConstants.CONSUMER_KEY;
import static org.acl.root.TwitterOAuthConstants.CONSUMER_SECRET;

import java.util.Calendar;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TwitterHelper implements CallObserver {
	
	private static final String TAG = "TwitterHelper";
	
	private static final String TWITTER_PREFS = "twitter_preferences";
	private static final int MAX_TWEET_LENGTH = 140;
	
	private static final String TWITTER_OAUTH_ACCESS_TOKEN = "twitter_access_token";
	private static final String TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	
	private static TwitterHelper instance;
	
	private String tweet = "";
	
	private Twitter twitter = null;
	
	private Calendar calendar;

	public static TwitterHelper getInstance(Context context) {
		if(instance == null)
			instance = new TwitterHelper(context);
		return instance;
	}
	
	private TwitterHelper(Context context) {
		
		SharedPreferences twitterPreferences = 
				context.getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
		
		Configuration conf = new ConfigurationBuilder()
	    .setOAuthConsumerKey(CONSUMER_KEY)
	    .setOAuthConsumerSecret(CONSUMER_SECRET)
	    .setOAuthAccessToken(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN, "" ))
	    .setOAuthAccessTokenSecret(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, "" ))
	    .build();
	 
		twitter = new TwitterFactory(conf).getInstance();
		
		calendar = Calendar.getInstance();
	}
		
	public boolean setTweet(String text) {
		
		if (text.length() > MAX_TWEET_LENGTH) {
			tweet = text.substring(0, MAX_TWEET_LENGTH);
			return false;
		} else {
			tweet = text;
			return true;
		}
	}
	
	@Override
	public void callNotification(CallInfo callInfo) {
		try {
			Log.d(TAG, "Sending tweet: " + tweet);
			// Timestamp tweet
			Date dateTime = calendar.getTime();
			twitter.updateStatus(dateTime.toLocaleString() + " " + tweet);
			Log.d(TAG, "Tweet sent");
		} catch (TwitterException e) {
			Log.e(TAG, "Can't send Tweet", e);
		}	
	}

}
