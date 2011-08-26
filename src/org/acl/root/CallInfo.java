package org.acl.root;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

/**
 * Used to pass information related to incoming calls to observers
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class CallInfo implements Serializable {
	
	private static final long serialVersionUID = 83472973430823799L;
	
	// The context doesn't need to be serialized
	private final transient Context context; 
	private final Date date;
	private final String caller;
	private final String callNumber;
	private final List<String> emailAddresses;

	public static class Builder {
		// Required
		private Context context;
		private String callNumber;
		
		// Automatically Generated
		private Date date = new Date();
		
		// Optional
		private String caller = "Unknown";
		private List<String> emailAddresses = new ArrayList<String>();
		
		public Builder(Context context, String callNumber) {
			this.context = context;
			this.callNumber = callNumber;
		}
		
		public Builder caller(String caller) {
			this.caller = caller; return this;
		}
		
		public Builder emailAddresses(List<String> emailAddresses) {
			this.emailAddresses = emailAddresses; return this;
		}

		public CallInfo build() {
			return new CallInfo(this);
		}

	}
	
	private CallInfo(Builder builder) {
		context = builder.context;
		callNumber = builder.callNumber;
		date= builder.date;
		caller = builder.caller;
		emailAddresses = builder.emailAddresses;
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getDate() {
		Format formatter = new SimpleDateFormat("dd-MMM-yy");;
		return formatter.format(date);
	}
	
	public String getTime() {
		Format formatter = new SimpleDateFormat("HH:mm:ss");;
		return formatter.format(date);
	}

	public String getCaller() {
		return caller;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public List<String> getEmailAddresses() {
		return emailAddresses;
	}
		
}
