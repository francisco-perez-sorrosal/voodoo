package org.acl.root;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.util.Log;

public class LogHelper implements CallObserver {

	private static final String TAG = "LogHelper";

	private static LogHelper instance;

	public static LogHelper getInstance(Context context) {
		if (instance == null)
			instance = new LogHelper(context);
		return instance;
	}

	private Context context;

	private LogHelper(Context context) {
		this.context = context;
	}

	@Override
	public void callNotification(CallInfo callInfo) {
		Log.d(TAG, "Adding call to log file...");
		String logMsg = callInfo.getDate()
				+ " " 
				+ callInfo.getCaller() 
				+ " "
				+ callInfo.getCallNumber() + "\n";
		saveLogToFile(logMsg);
		Log.d(TAG, "Call added to log file.");
	}

	private void saveLogToFile(String data) {
		FileOutputStream fos = null;
		OutputStreamWriter outputwriter = null;
		BufferedWriter buffwriter = null;

		try {
			fos = context.openFileOutput(ShowLogActivity.LOGFILE,
					Context.MODE_PRIVATE | Context.MODE_APPEND);

			outputwriter = new OutputStreamWriter(fos);
			buffwriter = new BufferedWriter(outputwriter);

			buffwriter.append(data);
			Log.i(TAG, "Call saved in log");
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} finally {
			try {
				if (buffwriter != null)
					buffwriter.close();
				if (outputwriter != null)
					outputwriter.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
