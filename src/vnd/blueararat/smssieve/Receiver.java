package vnd.blueararat.smssieve;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
	static final String FILTERS = "filters";
	static final String REGEX_FILTERS = "regex";
	static final String KEY_MATCHES = "matches";
	static final int MAX_LENGTH = 100;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean b = preferences.getBoolean(KEY_ENABLED, true);
			if (!b)
				return;
			SharedPreferences filters = context.getSharedPreferences(FILTERS,
					Context.MODE_PRIVATE);
			SharedPreferences regex_filters = context.getSharedPreferences(
					REGEX_FILTERS, Context.MODE_PRIVATE);

			if (filters.getAll().isEmpty() && regex_filters.getAll().isEmpty())
				return;

			Set<String> set = regex_filters.getAll().keySet();

			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] msgs = new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				String adr = msgs[0].getOriginatingAddress();
				if (filters.contains(adr)) {
					String matches = " " + adr
							+ preferences.getString(KEY_MATCHES, "");
					if (matches.length() > MAX_LENGTH) {
						matches = matches
								.substring(0, matches.lastIndexOf(" "));
					}
					int i = preferences.getInt(KEY_SKIPPED, 0) + 1;
					int j = filters.getInt(adr, 0) + 1;
					Editor et = preferences.edit();
					et.putInt(KEY_SKIPPED, i);
					et.putString(KEY_MATCHES, matches);
					et.commit();
					Editor et2 = filters.edit();
					et2.putInt(adr, j);
					et2.commit();
					abortBroadcast();
				} else {
					for (String exp : set) {
						Pattern p = null;
						try {
							p = Pattern.compile(exp);
						} catch (PatternSyntaxException e) {
							continue;
						}
						Matcher m = p.matcher(adr);
						if (m.matches()) {
							String matches = " " + adr
									+ preferences.getString(KEY_MATCHES, "");
							if (matches.length() > MAX_LENGTH) {
								matches = matches.substring(0,
										matches.lastIndexOf(" "));
							}
							int i = preferences.getInt(KEY_SKIPPED, 0) + 1;
							int j = regex_filters.getInt(exp, 0) + 1;
							Editor et = preferences.edit();
							et.putInt(KEY_SKIPPED, i);
							et.putString(KEY_MATCHES, matches);
							et.commit();

							Editor et2 = regex_filters.edit();
							et2.putInt(exp, j);
							et2.commit();
							abortBroadcast();
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
}