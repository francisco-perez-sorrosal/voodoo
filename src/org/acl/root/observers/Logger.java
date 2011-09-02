package org.acl.root.observers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.acl.root.utils.CallInfo;

import android.content.Context;
import android.util.Log;

/**
 * A singleton that manages the log of incoming calls that occur whilst the
 * filtering service is active. It manages an in-memory list of CallInfo objects
 * that is persisted to a file each time an incoming call is received.
 * NOTE that this is an INEFFICIENT solution. I've tried to append serialized 
 * CallInfo objects to the end of the log file but this is not easy to 
 * implement through serialization in Java. This because when several read 
 * operations are performed on the input stream, only the first one is able to
 * found the header written by the output stream. The next read throws an
 * IOException.
 * 
 * TODO Improve this serializing the required call information as strings
 * or using a database to store the CallInfo objects
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public enum Logger implements CallObserver {

	INSTANCE;
	
	private static final String TAG = "Logger";
	
	public static final String LOGFILE = "log.txt";
	
	private List<CallInfo> callLog = new ArrayList<CallInfo>();
	private boolean callLogCached = false;
	

	public List<CallInfo> getCallLog(Context context) {
		if(!callLogCached) loadLogFromFile(context);
		return callLog;
	}
	
	public void clearLog(Context context) {
		callLog.clear();
		context.deleteFile(LOGFILE);
	}

	@Override
	public void callNotification(CallInfo callInfo) {
		Context context = callInfo.getContext();
		if(!callLogCached) loadLogFromFile(context);
		Log.d(TAG, "Adding call to log file...");
		callLog.add(0, callInfo);
		saveLogToLogFile(context);
	}
		
	// ------------------------- Private methods ------------------------------
	
	@SuppressWarnings("unchecked")
	private void loadLogFromFile(Context context) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = context.openFileInput(LOGFILE);
			ois = new ObjectInputStream(fis);
			callLog = (ArrayList<CallInfo>) ois.readObject();
			callLogCached = true;
			Log.i(TAG, "Log read from file");
		} catch (FileNotFoundException e) { 
			Log.e(TAG, "File Still not created");
			callLogCached = true;
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
	
	private void saveLogToLogFile(Context context) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = context.openFileOutput(LOGFILE, Context.MODE_PRIVATE );
			oos =  new ObjectOutputStream(fos);
			oos.writeObject(callLog);
			Log.i(TAG, "Log saved in file");
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
	}

}
