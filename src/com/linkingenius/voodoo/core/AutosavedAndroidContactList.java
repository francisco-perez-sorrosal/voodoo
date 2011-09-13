package com.linkingenius.voodoo.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.linkingenius.voodoo.utils.Contact;

public class AutosavedAndroidContactList extends ForwardingConcurrentMap<String, Contact> {
	
	private  final String TAG = "AutosavedAndroidContactList";
	
	private final long AUTOSAVING_PERIOD_IN_SECS = 60;
	
	private ScheduledThreadPoolExecutor autosaveExecutor = new ScheduledThreadPoolExecutor(1);

	private Context context;
	
	private boolean modified = false;
	
	private String fileName;
	
	public AutosavedAndroidContactList(Context context, ConcurrentMap<String, Contact> map, String fileName) {
		super(map);
		this.context = context;
		this.fileName = fileName;
		loadFromFile();
		autosaveExecutor.scheduleAtFixedRate(new Autosaver(this), 
				AUTOSAVING_PERIOD_IN_SECS, 
				AUTOSAVING_PERIOD_IN_SECS, 
				TimeUnit.SECONDS);
	}
	
	@Override
	public synchronized Contact putIfAbsent(String key, Contact value) {
		Contact previousValue = super.putIfAbsent(key, value);
		if(previousValue == null) {
			modified = true;
		}
		return previousValue;
	}
	
	public synchronized Contact remove(String value) {
		Contact previousValue = super.remove(value);
		if(previousValue != null) {
			modified = true;
		}
		return previousValue;
	}
	
	// ------------------------- Private Methods ----------------------------
	
	private ArrayList<Contact> toArrayList() {
		Set<Contact> duplicateContactFilter = new HashSet<Contact>(super.values());
		return  new ArrayList<Contact>(duplicateContactFilter);
	}
	
	private void loadFromFile() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = context.openFileInput(fileName);
			ois = new ObjectInputStream(fis);
			
			int numElements = ois.readInt();

			for (int i = 0; i < numElements; i++) {
			   String contactId = (String) ois.readObject();
			   Contact contact = Contact
					   .getContactFromAndroid(context, contactId).build();
			   synchronized(this) {
				   for(String phone : contact.getPhoneNumbers()) {
					   super.putIfAbsent(phone, contact);
					   Log.d(TAG, "Phone filtered: " + phone);
				   }
			   }
			}
			Log.i(TAG, "Blacklist read");
		} catch (FileNotFoundException e) { 
			Log.e(TAG, "File Still not created. No contacts maybe?");
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "ClassNotFoundException", e);
		} finally {
			try {
				if(ois != null) ois.close();
				if(fis != null) fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveToFile() {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			oos =  new ObjectOutputStream(fos);
			
			List<Contact> blackListToStore = toArrayList();
			oos.writeInt(blackListToStore.size());
			// Write out all elements in the proper order. 
			for (Contact contact : blackListToStore) {
				oos.writeObject(contact.getId());
			}
			Log.i(TAG, "Blacklist saved. # of contacts: " + blackListToStore.size());
		} catch (FileNotFoundException e) {
			Log.d(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} finally {
			try {
				if (oos != null) oos.close();
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			modified = false;
		}
	}
	
	// -------------------------- Private classes -----------------------------
	
	private class Autosaver implements Runnable {
		Object synchronizer;

		public Autosaver(Object synchronizer) {
			this.synchronizer = synchronizer;
		}

		public void run() {
			synchronized (synchronizer) {
				if (modified) {
					saveToFile();
					Log.d(TAG, "Autosaving List of Contacts");
				} else {
					Log.d(TAG, "List of Contacts doesn't need to be saved");
				}
			}
		}
	}
	
}
