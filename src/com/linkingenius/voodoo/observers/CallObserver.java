package com.linkingenius.voodoo.observers;

import com.linkingenius.voodoo.utils.CallInfo;

public interface CallObserver {
	/**
	 * This method is called to deliver information related to incoming calls
	 * 
	 * @param callInfo information related to calls
	 */
	public void callNotification(CallInfo callInfo);
}
