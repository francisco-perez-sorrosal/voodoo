package org.acl.root;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.acl.root.utils.InstrumentedConcurrentMap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public enum BlackList {
	
	INSTANCE;
	
	private static final String TAG = "BlackList";
	
	private static final String FILE = "blacklist.txt";
	
	private ConcurrentMap<String, String> blackList = 
			new InstrumentedConcurrentMap<String, String>(
					new ConcurrentHashMap<String, String>());
	
	public ArrayList<CharSequence> getBlackListAsArrayList() {
		return  new ArrayList<CharSequence>(blackList.values());
	}

	public String addContactToBlackList(Contact contact) {
		String name = contact.getName();
		String phone = contact.getPhoneNumbers().get(0);
		String previousValue = blackList.putIfAbsent(phone, name + " (" + phone + ")");
		Log.d(TAG, name + " " + phone + " added to Black List in service method");
		return previousValue;
	}

	public String removeContactFromBlackList(String key) {
		String previousValue = blackList.remove(key);
		Log.d(TAG, previousValue + " removed from Black List in service method");
		return previousValue;
	}
	
	public boolean containsContact(String contact) {
		return blackList.containsKey(contact);
	}
	
	public String getContact(String contact) {
		return blackList.get(contact);
	}
	
	public void loadMapFromFile(Context context){
		FileInputStream fis = null;
		InputStreamReader inputreader = null;
		BufferedReader buffreader = null;
		
		try {
			fis = context.openFileInput(FILE);
			inputreader = new InputStreamReader(fis);
			buffreader = new BufferedReader(inputreader);

			String line;

			// read every line of the file into the line-variable, on line at the time
			while ((line = buffreader.readLine()) != null) {
				// do something with the settings from the file
				Log.d(TAG, "Reading " + line);
				String [] data = line.split("-");	
				blackList.putIfAbsent(data[0], data[1]);
			}
			Log.i(TAG, "Blacklist read");
		} catch (FileNotFoundException e) { 
			Log.e(TAG, "File Still not created", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} finally {
			try {
				if(buffreader != null) buffreader.close();
				if(inputreader != null) inputreader.close();
				if(fis != null) fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveMapToFile(Context context) {
		FileOutputStream fos = null;
		OutputStreamWriter outputwriter = null;
		BufferedWriter buffwriter = null;

		try {
			fos = context.openFileOutput(FILE, Context.MODE_PRIVATE);

			outputwriter = new OutputStreamWriter(fos);
			buffwriter = new BufferedWriter(outputwriter);

			for (Map.Entry<String, String> entry : blackList.entrySet()) {
				String data = entry.getKey() + "-" + entry.getValue() + "\n";
				Log.d(TAG, "Writting " + data);
				buffwriter.write(data);
			}
			Log.i(TAG, "Blacklist saved");
		} catch (FileNotFoundException e) {
			Log.d(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} finally {
			try {
				if (buffwriter != null) buffwriter.close();
				if (outputwriter != null) outputwriter.close();
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			blackList.clear();
		}
	}

}
