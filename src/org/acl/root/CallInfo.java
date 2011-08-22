package org.acl.root;

import java.util.Date;

public class CallInfo {
	
	private final Date date;
	private final String caller;
	private final String callNumber;
	private final String email;

	public CallInfo(Date date, String caller, String callNumber, String email) {
		this.date = date;
		this.caller = caller;
		this.callNumber = callNumber;
		this.email = email;
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

	public String getEmail() {
		return email;
	}
	
}
