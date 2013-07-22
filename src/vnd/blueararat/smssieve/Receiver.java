package vnd.blueararat.smssieve;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

public class Receiver extends BroadcastReceiver {

	static final String KEY_ENABLED = "enabled";
	static final String KEY_SKIPPED = "skipped";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean b = preferences.getBoolean(KEY_ENABLED, true);
			if (!b)
				return;
			SharedPreferences filters = context.getSharedPreferences("filters",
					context.MODE_PRIVATE);
			if (filters.getAll().isEmpty())
				return;
			// Toast.makeText(context, "sms arrived", 0).show();
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] msgs = new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				String adr = msgs[0].getOriginatingAddress();
				if (filters.contains(adr)) {
					int i = preferences.getInt(KEY_SKIPPED, 0) + 1;
					int j = filters.getInt(adr, 0) + 1;
					Editor et = preferences.edit();
					et.putInt(KEY_SKIPPED, i);
					et.commit();
					Editor et2 = filters.edit();
					et2.putInt(adr, j);
					et2.commit();
					abortBroadcast();
				}
			}
		} catch (Exception e) {
		}
	}
}