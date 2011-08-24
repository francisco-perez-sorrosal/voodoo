package org.acl.root;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
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
		return  new ArrayList<Contact>(blackList.values());
	}

	public Contact addContactToBlackList(Contact contact) {
		Contact previousValue = null;
		String name = contact.getName();
		
		for(String phone : contact.getPhoneNumbers()) {
			previousValue = blackList.putIfAbsent(phone, contact);
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
			
			Contact contact;
			
			int size = ois.readInt(); //restore how many contacts  
			for(int i=0; i < size; i++)  {
			   contact = (Contact) ois.readObject();
			   Log.d(TAG, "Reading " + contact);
			   blackList.putIfAbsent(contact.getPhoneNumbers().get(0), contact);
			}
			Log.i(TAG, "Blacklist read");
		} catch (FileNotFoundException e) { 
			Log.e(TAG, "File Still not created", e);
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

			oos.writeInt(blackList.entrySet().size());
			for (Map.Entry<String, Contact> entry : blackList.entrySet()) {
				Log.d(TAG, "Writting " + entry.getValue());
				oos.writeObject(entry.getValue());
			}
			Log.i(TAG, "Blacklist saved");
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
