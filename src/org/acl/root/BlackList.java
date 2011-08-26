package org.acl.root;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.acl.root.utils.InstrumentedConcurrentMap;

import android.content.Context;
import android.util.Log;

public enum BlackList {
	
	INSTANCE;
	
	private static final String TAG = "BlackList";
	
	private static final String FILE = "blacklist.txt";
	
	private ConcurrentMap<String, Contact> blackList = 
			new InstrumentedConcurrentMap<String, Contact>(
					new ConcurrentHashMap<String, Contact>());
	
	public ArrayList<Contact> getBlackListAsArrayList() {
		Set<Contact> duplicateContactFilter = new HashSet<Contact>(blackList.values());
		return  new ArrayList<Contact>(duplicateContactFilter);
	}

	public Contact addContactToBlackList(Contact contact) {
		Contact previousValue = null;
		String name = contact.getName();
		
		for(String phone : contact.getPhoneNumbers()) {
			previousValue = blackList.putIfAbsent(phone, contact);
			if(previousValue == null)
				Log.d(TAG, name + " " + phone + " added to Black List");
		}
		return previousValue;
	}

	public Contact removeContactFromBlackList(Contact contact) {
		Contact previousValue = null;
		String name = contact.getName();
		
		for(String phone : contact.getPhoneNumbers()) {
			previousValue = blackList.remove(phone);
			Log.d(TAG, name + " " + phone + " removed from Black List");
		}
		return previousValue;
	}
	
	public boolean containsContact(String contact) {
		return blackList.containsKey(contact);
	}
	
	public Contact getContact(String contact) {
		return blackList.get(contact);
	}
	
	public void loadMapFromFile(Context context){
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = context.openFileInput(FILE);
			ois = new ObjectInputStream(fis);
			
			List<Contact> storedBlackList = (ArrayList<Contact>) ois.readObject();
			for(Contact contact : storedBlackList) {
			   Log.d(TAG, "Reading " + contact);
			   for(String phone : contact.getPhoneNumbers()) {
				   blackList.putIfAbsent(phone, contact);
				   Log.d(TAG, "Phone filtered: " + phone);
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

	public void saveMapToFile(Context context) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = context.openFileOutput(FILE, Context.MODE_PRIVATE);
			oos =  new ObjectOutputStream(fos);
			
			List<Contact> blackListToStore = getBlackListAsArrayList();
			oos.writeObject(blackListToStore);
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
			blackList.clear();
		}
	}

}
