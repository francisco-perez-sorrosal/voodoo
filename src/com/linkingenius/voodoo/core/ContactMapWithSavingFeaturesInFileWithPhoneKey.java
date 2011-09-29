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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.util.Log;

import com.linkingenius.voodoo.utils.Contact;

/**
 * This is an instrumented class that extends the @ForwardingConcurrentMap, 
 * and allows to programatically save the contents of the map onto a file on the 
 * Android file system.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class ContactMapWithSavingFeaturesInFileWithPhoneKey extends ForwardingConcurrentMap<String, Contact> {
	
	private  final String TAG = "ContactMapWithSavingFeaturesInFileWithPhoneKey";
	
	private final Context context;
	
	private final String fileName;
		
	// State. When true, the state of the map has been modified. This is used 
	// in order to save the map in the specified file
	private boolean modified;
	
	public ContactMapWithSavingFeaturesInFileWithPhoneKey(Context context, ConcurrentMap<String, Contact> map, String fileName) {
		super(map);
		this.context = context;
		this.fileName = fileName;
		loadMapFromFile();
	}
	
	@Override
	public synchronized void clear() {
		super.clear();
		modified = true;
	}
	
	@Override
	public synchronized Contact put(String key, Contact value) {
		Contact previousValue = super.put(key, value);
		if(previousValue == null) {
			modified = true;
		}
		return previousValue;
	}

	@Override
	public synchronized void putAll(Map<? extends String, ? extends Contact> map) {
		super.putAll(map);
		modified = true;
	}
	
	@Override
	public synchronized Contact remove(Object value) {
		Contact previousValue = super.remove(value);
		if(previousValue != null) {
			modified = true;
		}
		return previousValue;
	}
	
	@Override
	public synchronized Contact putIfAbsent(String key, Contact value) {
		Contact previousValue = super.putIfAbsent(key, value);
		if(previousValue == null) {
			modified = true;
		}
		return previousValue;
	}
	
	@Override
	public synchronized boolean remove(Object key, Object value) {
		modified = super.remove(key, value);
		return modified;
	}

	@Override
	public synchronized Contact replace(String key, Contact value) {
		Contact previousValue = super.replace(key, value);
		if(previousValue == null) {
			modified = true;
		}
		return previousValue;
	}

	@Override
	public synchronized boolean replace(String key, Contact oldValue, Contact newValue) {
		modified = super.replace(key, oldValue, newValue);
		return modified;
	}
	
	// ------------------------- Public Methods -----------------------------
	
	public ArrayList<Contact> toArrayList() {
		Set<Contact> duplicateContactFilter = new HashSet<Contact>(super.values());
		return  new ArrayList<Contact>(duplicateContactFilter);
	}
	
	// ------------------------- Private Methods ----------------------------
	
	private void loadMapFromFile() {
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
				   modified = false;
			   }
			}
			Log.i(TAG, "Contacts read");
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
	
	public void saveMapToFile() {
		if (modified) {
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
	
			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				oos =  new ObjectOutputStream(fos);
				
				synchronized(this) {
					List<Contact> listToStore = toArrayList();
					oos.writeInt(listToStore.size());
					// Write out all elements in the proper order. 
					for (Contact contact : listToStore) {
						oos.writeObject(contact.getId());
					}
					modified = false;
					Log.i(TAG, "Contacts saved. # of contacts: " + listToStore.size());
				}
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
			}
		} else {
			Log.d(TAG, "The map of contacts doesn't need to be saved");
		}
	}
	
}
