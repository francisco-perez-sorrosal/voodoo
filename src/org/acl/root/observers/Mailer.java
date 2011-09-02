package org.acl.root.observers;

import javax.mail.PasswordAuthentication;

import org.acl.root.observers.delegates.RealMailer;
import org.acl.root.utils.CallInfo;

import android.content.Context;
import android.util.Log;

/**
 * This class implements a singleton object that allows the application 
 * to send e-mails to the callers if possible.
 * 
 * Each time an incoming call is received, the singleton is notified (because 
 * it plays an observer role in an observer pattern) and delegates (acting as a
 * delegator in a Delegation pattern) the notification processing to the 
 *  @RealMailer class, which implements the real functionality of sending mails 
 * (delegate).
 *  
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public enum Mailer implements CallObserver {

	INSTANCE;
	
	private static final String TAG = "Mailer";
	
	private  RealMailer mailer = new RealMailer();

	public boolean send(Context context) throws Exception {
		Log.d(TAG, "Delegating call to RealMailer...");
		return mailer.send(context);
	}

	public void addAttachment(String filename) throws Exception {
		Log.d(TAG, "Delegating call to RealMailer...");
		mailer.addAttachment(filename);
	}

	public PasswordAuthentication getPasswordAuthentication() {
		Log.d(TAG, "Delegating call to RealMailer...");
		return mailer.getPasswordAuthentication();
	}
	
	public void setBody(String body) {
		Log.d(TAG, "Delegating call to RealMailer...");
		mailer.setBody(body);
	}

	/**
	 * Observer pattern for incoming calls
	 */
	@Override
	public void callNotification(CallInfo callInfo) {
		Log.d(TAG, "Delegating call to RealMailer...");
		mailer.processNotification(callInfo);
	}

}
