package com.linkingenius.voodoo.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.util.Log;

import com.linkingenius.voodoo.utils.Contact;

/**
 * Singleton and facade to manage the filtered numbers and the contacts
 * associated to them
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public enum BlackList {
	
	INSTANCE;
	
	private static final String TAG = "BlackList";
	
	private static final String FILE = "blacklist.txt";
	
	private ContactMapAutosavedInFileWithPhoneKey blackList = null;

	public ArrayList<Contact> toArrayList(Context context) {
		initialize(context);
		return blackList.toArrayList();
	}

	public Contact addContact(Context context, Contact contact) {
		initialize(context);
		Contact previousValue = null;
		String name = contact.getName();
		
		synchronized(blackList) {
			for(String phone : contact.getPhoneNumbers()) {
				previousValue = blackList.putIfAbsent(phone, contact);
				if(previousValue == null) {
					Log.d(TAG, name + " " + phone + " added to Black List");
				}
			}
		}
		return previousValue;
	}

	public Contact removeContact(Context context, Contact contact) {
		initialize(context);
		Contact previousValue = null;
		String name = contact.getName();
		
		synchronized(blackList) {
			for(String phone : contact.getPhoneNumbers()) {
				previousValue = blackList.remove(phone);
				if(previousValue != null) {
					Log.d(TAG, name + " " + phone + " removed from Black List");
				}
			}
		}
		return previousValue;
	}
	
	public boolean containsContact(Context context, String contact) {
		initialize(context);
		return blackList.containsKey(contact);
	}
	
	public Contact getContact(Context context, String contact) {
		initialize(context);
		return blackList.get(contact);
	}

	public void startAutosaving(Context context) {
		initialize(context);
		blackList.autosaveContacts(true);
	}

	public void stopAutosaving(Context context) {
		initialize(context);
		blackList.autosaveContacts(false);
	}
	
	// ------------------------- Private Methods ----------------------------
	
	private synchronized void initialize(Context context){
		if(blackList == null) {
			blackList = new ContactMapAutosavedInFileWithPhoneKey(
					context,
					new ConcurrentHashMap<String, Contact>(),
					FILE);
		}
	}
}
