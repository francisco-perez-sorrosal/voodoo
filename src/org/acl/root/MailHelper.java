package org.acl.root;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class MailHelper extends Authenticator implements CallObserver {

	private static final String TAG = "MailHelper";
	private static final String DEFAULT_FROM_EMAIL = "no-reply@linkingenius.com";
	private static final String DEFAULT_SUBJECT = "Autoresponse from Blacklist Android App";

	protected static final String EMAIL_PREFS = "email_preferences";
	
	protected static final String EMAIL = "email_user";
	protected static final String EMAIL_PASSWORD = "email_password";
	
	public static  MailHelper instance;
	
	public static MailHelper getInstance(Context context) {
		if(instance == null)
			instance = new MailHelper(context);
		return instance;
	}
	
	private String user;
	private String password;
	private String[] to;
	private String from;
	private String subject;
	private String body;
	private Multipart multipart;

	private String host;
	private String port;
	private String sport;

	private boolean auth;

	private boolean debuggable;

	/**
	 * First version of MailHelper.
	 * 
	 * Check this for more info:
	 * http://www.jondev.net/articles/Sending_Emails_without_User_Intervention_
	 * %28no_Intents%29_in_Android
	 */
	private MailHelper(Context context) {
		
		Log.d(TAG, "Constructor");
		SharedPreferences emailPreferences = 
				context.getSharedPreferences(EMAIL_PREFS, Activity.MODE_PRIVATE);
		
		user = emailPreferences.getString(EMAIL, "" );
		password = emailPreferences.getString(EMAIL_PASSWORD, "" );
		
		host = "smtp.gmail.com"; // default smtp server
		port = "465"; // default smtp port
		sport = "465"; // default socketfactory port

		from = ""; // email sent from
		subject = ""; // email subject
		body = ""; // email body
		multipart = new MimeMultipart();

		debuggable = false; // debug mode on or off - default off
		auth = true; // smtp authentication - default on

		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public boolean send() throws Exception {
		Properties props = setMailProperties();
		
		if (!user.equals("") && !password.equals("") && to.length > 0
				&& !from.equals("") && !subject.equals("") && !body.equals("")) {
			Session session = Session.getInstance(props, this);

			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(from));

			InternetAddress[] addressTo = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				addressTo[i] = new InternetAddress(to[i]);
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

			msg.setSubject(subject);
			msg.setSentDate(new Date());

			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			msg.setContent(multipart);

			// send email
			Transport.send(msg);

			return true;
		} else {
			return false;
		}
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		multipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	private Properties setMailProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", host);

		if (debuggable) {
			props.put("mail.debug", "true");
		}

		if (auth) {
			props.put("mail.smtp.auth", "true");
		}

		props.put("mail.smtp.port", port);
		props.put("mail.smtp.socketFactory.port", sport);
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

	// The getters and setters
	
	private void setTo(String[] toArr) {
		this.to = toArr;
	}

	private void setFrom(String string) {
		this.from = string;
	}

	private void setSubject(String string) {
		this.subject = string;
	}
	
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Observer pattern for incoming calls
	 */
	@Override
	public void callNotification(CallInfo callInfo) {
		ArrayList<String> emailAddresses = callInfo.getEmailAddresses();
		if(emailAddresses.size() > 0) {
			Log.d(TAG, "Sending email to " + emailAddresses.toString());
			String[] toArr = new String[emailAddresses.size()];
			emailAddresses.toArray(toArr);
			setTo(toArr);
			setFrom(DEFAULT_FROM_EMAIL);
			setSubject(DEFAULT_SUBJECT);
			setBody("Francisco is busy at this time. Please call him late.");
			try {
				if (send()) {
					Log.d(TAG, "Email sent.");
				} else {
					Log.d(TAG, "Email was not sent.");
				}
			} catch (Exception e) {
				Log.e(TAG, "Could not send email", e);
			}
		} else {
			Toast.makeText(callInfo.getContext(), R.string.no_email_addresses_found, Toast.LENGTH_SHORT).show();
		}
	}

}
