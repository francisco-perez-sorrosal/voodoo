package org.acl.root;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.acl.root.utils.InstrumentedConcurrentMap;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This is the service responsible of capturing incoming calls and 
 * perform the required actions.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class IncomingCallScanner extends Service {

	private static final String TAG = "IncomingCallScanner";

	private static final String FILE = "blacklist.txt";

	private static final String JAVI_PHONE = "630445705";

	private ConcurrentMap<String, String> blackList = new InstrumentedConcurrentMap<String, String>(new ConcurrentHashMap<String, String>());

	private boolean filterAllCalls = false;
	
	private AudioManager am;
	private int currentAudioMode;
	
	private boolean serviceRunning = false;

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	private Twitter twitter = null;
	
	private MailHelper mail = null;
	
	/****************************************
	 * Binder class
	 *
	 ****************************************/
	public class LocalBinder extends Binder {
		IncomingCallScanner getService() {
			return IncomingCallScanner.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private BroadcastReceiver phoneStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
				Log.d(TAG, "Call State=" + state);

				if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
					Log.d(TAG, "Idle");
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				} else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					// Incoming call
					String incomingNumber = intent
							.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
					Log.d(TAG, "Incoming call " + incomingNumber);
					if(filterAllCalls || blackList.containsKey(incomingNumber)) {
						// Telephony actions
						if (!killCall(context)) { // Using the method defined earlier
							Log.e(TAG, "Unable to kill incoming call");
						}
						Calendar calendar = Calendar.getInstance();
						// Get relevant call info
						String name = blackList.get(incomingNumber);
						CharSequence text = 
								(calendar.getTime().toString() // To avoid tweet discard 
								+ " " 
								+ getResources().getString(R.string.twitter_message_1)
								+ " " + name + " " 
								+ getResources().getString(R.string.twitter_message_2));
						// Log actions					
						saveLogToFile(calendar.getTime().toString() + " " + name + " " + incomingNumber+"\n");
						// Twitter actions
						if(isTwitterEnabled()) {
							try {
								Log.d(TAG, "Sending tweet...");
								if(incomingNumber.equals(JAVI_PHONE)) {
									String message = context.getResources().getString(R.string.javi_twitter_message);
									twitter.updateStatus(message);
								} else {
									twitter.updateStatus(text.toString());
								}
								Log.d(TAG, "Tweet sent");
							} catch (TwitterException e) {
								Log.e(TAG, "Can't send Tweet");
								e.printStackTrace();
							}
						}
						if (isEmailEnabled()) {
							Log.d(TAG, "Sending email...");
							String[] toArr = { "test@gmail.com" };
							mail.setTo(toArr);
							mail.setFrom("no-reply@linkingenius.com");
							mail.setSubject("Autoresponse from Blacklist Android App. SUBJECT: Call to Francisco");
							mail.setBody("Francisco is busy at this time. Please call him late.");
							try {
								if (mail.send()) {
									Toast.makeText(getApplicationContext(),
											"Email was sent successfully.",
											Toast.LENGTH_LONG).show();
									Log.d(TAG, "Email sent");
								} else {
									Toast.makeText(getApplicationContext(),
											"Email was not sent. Check your user and password",
											Toast.LENGTH_LONG).show();
									Log.d(TAG, "Email was not sent");
								}
							} catch (Exception e) {
								Log.e("MailApp", "Could not send email", e);
							}
						}
						// Inform user
						UserNotification.showToastWithImage(context, text, R.drawable.app_icon);
					} else {
						am.setRingerMode(currentAudioMode);
					}

				} else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
					Log.d(TAG, "Offhook");
				}
			} else if (intent.getAction().equals(
					"android.intent.action.NEW_OUTGOING_CALL")) {
				// Outgoing call: DO NOTHING at this time
				// String outgoingNumber = intent
				// .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				// Log.d(TAG, "Outgoing call " + outgoingNumber);
				// setResultData(null); // Kills the outgoing call
			} else {
				Log.e(TAG, "Unexpected intent.action=" + intent.getAction());
			}
		}
	};

	// ------------------------- Lifecycle -------------------------

	@Override
	public void onCreate() {

		if(!serviceRunning) {
			Log.d(TAG, "Loading map with blacklist...");
			loadMapFromFile();
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			currentAudioMode = am.getRingerMode();
			am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.PHONE_STATE");
			filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			registerReceiver(phoneStateReceiver, filter);
			serviceRunning = true;			
			Log.d(TAG, "Created");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received start id " + startId + ": " + intent);
		UserNotification.showNotification(getApplicationContext());
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		if(serviceRunning) {
			saveMapToFile();
			blackList.clear();
			// Stop filtering calls, otherwise they'll continue to be filtered
			am.setRingerMode(currentAudioMode);
			am = null;
			unregisterReceiver(phoneStateReceiver);
			UserNotification.cancelNotification(getApplicationContext(), UserNotification.SVC_STARTED_NOTIFICATION);
			serviceRunning = false;
			Log.d(TAG, "Stopped");
			// Tell the user we stopped.
			Toast.makeText(getApplicationContext(), R.string.ics_service_stopped, Toast.LENGTH_SHORT).show();
		}
	}

	// ------------------------- Lifecycle -------------------------

	// ------------------- Black List Management -------------------

	public ArrayList<CharSequence> getBlackList() {
		return  new ArrayList<CharSequence>(blackList.values());
	}

	public String addContactToBlackList(Contact contact) {
		String name = contact.getName();
		String phone = contact.getPhoneNumbers().get(0);
		String previousValue = blackList.putIfAbsent(phone, name + " (" + phone + ")");
		Log.d(TAG, name + " " + phone + " added to Black List in service method");
		return previousValue;
	}

	public String removeContactFromBlackList(String key) {
		String previousValue = blackList.remove(key);
		Log.d(TAG, previousValue + " removed from Black List in service method");
		return previousValue;
	}

	// ------------------END Black List Management -----------------


	// ------------------- Twitter Management -------------------

	public void setTwitterConnection(Twitter newTwitter) {
		Log.d(TAG, "New Twitter reference set");
		twitter = newTwitter;
	}

	public boolean isTwitterEnabled() {
		return (twitter != null);
	}

	public void discardTwitterConnection() {
		twitter = null;
	}

	// ------------------END Twitter Management -----------------
	
	
	// ------------------- Email Management -------------------

	public void setEmailConnection(MailHelper newMail) {
		Log.d(TAG, "New MailHelper reference set");
		mail = newMail;
	}

	public boolean isEmailEnabled() {
		return (mail != null);
	}

	public void discardEmailConnection() {
		mail = null;
	}

	// ------------------END Twitter Management -----------------

	private void loadMapFromFile(){
		FileInputStream fis = null;
		InputStreamReader inputreader = null;
		BufferedReader buffreader = null;
		
		try {
			fis = openFileInput(FILE);
			inputreader = new InputStreamReader(fis);
			buffreader = new BufferedReader(inputreader);

			String line;

			// read every line of the file into the line-variable, on line at the time
			while ((line = buffreader.readLine()) != null) {
				// do something with the settings from the file
				Log.d(TAG, "Reading " + line);
				String [] data = line.split("-");	
				blackList.putIfAbsent(data[0], data[1]);
			}
		} catch (FileNotFoundException e) { 
			Toast.makeText(this, "File Still not created", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "Exception" + e.toString(), Toast.LENGTH_SHORT).show();
		} finally {
			try {
				if(buffreader != null) buffreader.close();
				if(inputreader != null) inputreader.close();
				if(fis != null) fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void saveMapToFile() {
		FileOutputStream fos = null;
		OutputStreamWriter outputwriter = null;
		BufferedWriter buffwriter = null;

		try {
			fos = openFileOutput(FILE, Context.MODE_PRIVATE);

			outputwriter = new OutputStreamWriter(fos);
			buffwriter = new BufferedWriter(outputwriter);

			for (Map.Entry<String, String> entry : blackList.entrySet()) {
				String data = entry.getKey() + "-" + entry.getValue() + "\n";
				Log.d(TAG, "Writting " + data);
				buffwriter.write(data);
			}
			Toast.makeText(this, "Black List Saved", Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "FileNotFoundException" + e.toString(),
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "IOException" + e.toString(),
					Toast.LENGTH_SHORT).show();
		} finally {
			try {
				if (buffwriter != null) buffwriter.close();
				if (outputwriter != null) outputwriter.close();
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void saveLogToFile(String data) {
		FileOutputStream fos = null;
		OutputStreamWriter outputwriter = null;
		BufferedWriter buffwriter = null;

		try {
			fos = openFileOutput(ShowLogActivity.LOGFILE, 
					Context.MODE_PRIVATE | Context.MODE_APPEND);

			outputwriter = new OutputStreamWriter(fos);
			buffwriter = new BufferedWriter(outputwriter);

			buffwriter.append(data);
			Toast.makeText(this, "Call saved in log", Toast.LENGTH_SHORT)
					.show();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "FileNotFoundException" + e.toString(),
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "IOException" + e.toString(),
					Toast.LENGTH_SHORT).show();
		} finally {
			try {
				if (buffwriter != null) buffwriter.close();
				if (outputwriter != null) outputwriter.close();
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean killCall(Context context) {
		try {
			// Get the boring old TelephonyManager
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// Get the getITelephony() method
			Class classTelephony = Class.forName(telephonyManager.getClass()
					.getName());
			Method methodGetITelephony = classTelephony
					.getDeclaredMethod("getITelephony");

			// Ignore that the method is supposed to be private
			methodGetITelephony.setAccessible(true);

			// Invoke getITelephony() to get the ITelephony interface
			Object telephonyInterface = methodGetITelephony
					.invoke(telephonyManager);

			// Get the endCall method from ITelephony
			Class telephonyInterfaceClass = Class.forName(telephonyInterface
					.getClass().getName());
			Method methodEndCall = telephonyInterfaceClass
					.getDeclaredMethod("endCall");

			// Invoke endCall()
			methodEndCall.invoke(telephonyInterface);

		} catch (Exception e) { // Many things can go wrong with reflection
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	public void filterAllCalls(boolean decission) {
		filterAllCalls = decission;
	}
	
	public boolean isAllCallsFilterEnabled() {
		return (filterAllCalls == true);
	}
	
}

