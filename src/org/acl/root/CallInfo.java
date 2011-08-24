package org.acl.root;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;

/**
 * Used to pass information related to incoming calls to observers
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class CallInfo {
	
	private final Context context;
	private final Date date;
	private final String caller;
	private final String callNumber;
	private final ArrayList<String> emailAddresses;

	public CallInfo(Context context, Date date, String caller, String callNumber, ArrayList<String> email) {
		this.context = context;
		this.date = date;
		this.caller = caller;
		this.callNumber = callNumber;
		this.emailAddresses = email;
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getDate() {
		return date.toString();
	}
	
	public String getCaller() {
		return caller;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public ArrayList<String> getEmailAddresses() {
		return emailAddresses;
	}
	
}
