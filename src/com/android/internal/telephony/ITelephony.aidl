package com.android.internal.telephony;

/**
 * Interface used to interact with the phone. Mostly this is used by the
 * TelephonyManager class. A few places are still using this directly.
 * Please clean them up if possible and use TelephonyManager instead.
 *
 */
interface ITelephony {

	boolean endCall();
	
    /**
     * Answer the currently-ringing call.
     *
     * If there's already a current active call, that call will be
     * automatically put on hold.  If both lines are currently in use, the
     * current active call will be ended.
     *
     * TODO: provide a flag to let the caller specify what policy to use
     * if both lines are in use.  (The current behavior is hard-wired to
     * "answer incoming, end ongoing", which is how the CALL button
     * is expected to behave.)
     *
     * TODO: this should be a one-way call (especially since it's called
     * directly from the key queue thread).
     */
    void answerRingingCall();

    /**
     * Silence the ringer if an incoming call is currently ringing.
     * (If vibrating, stop the vibrator also.)
     *
     * It's safe to call this if the ringer has already been silenced, or
     * even if there's no incoming call.  (If so, this method will do nothing.)
     *
     * TODO: this should be a one-way call too (see above).
     *       (Actually *all* the methods here that return void can
     *       probably be one-way.)
     */
    void silenceRinger();
}