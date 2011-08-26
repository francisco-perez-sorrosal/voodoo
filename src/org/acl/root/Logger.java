package org.acl.root;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public enum Logger implements CallObserver {

	INSTANCE;
	
	private static final String TAG = "Logger";
	
	public static final String LOGFILE = "log.txt";

	private List<CallInfo> callLog;

	public List<CallInfo> getCallLog(Context context) {
		initCallLog(context);
		return callLog;
	}
	
	public void clearLog(Context context) {
		if(callLog != null) callLog.clear();
		context.deleteFile(LOGFILE);
	}

	@Override
	public void callNotification(CallInfo callInfo) {
		initCallLog(callInfo.getContext());
		Log.d(TAG, "Adding call to log file...");
		callLog.add(callInfo);
		saveToLogFile(callInfo);
		Log.d(TAG, "Call added to log file.");
	}
		
	// ------------------------- Private methods ------------------------------
	
	private void initCallLog(Context context) {
		if(callLog == null) {
			 callLog = new ArrayList<CallInfo>();
			 loadLogFromFile(context);
		}
	}
	
	private void loadLogFromFile(Context context) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = context.openFileInput(LOGFILE);
			ois = new ObjectInputStream(fis);
			CallInfo callInfo;
			// This throws an IOException when EOF is found
			while((callInfo = (CallInfo) ois.readObject()) != null)
				callLog.add(callInfo);
			Log.i(TAG, "Log read");
		} catch (FileNotFoundException e) { 
			Log.i(TAG, "File Still not created");
		} catch (IOException e) {
			Log.e(TAG, "IOException. No more call info objects??? Probably...	");
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
	
	public void saveToLogFile(CallInfo callInfo) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = callInfo.getContext().openFileOutput(LOGFILE,
					Context.MODE_PRIVATE | Context.MODE_APPEND);
			oos =  new ObjectOutputStream(fos);
			oos.writeObject(callInfo);
			Log.i(TAG, "Entry saved in Log");
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
