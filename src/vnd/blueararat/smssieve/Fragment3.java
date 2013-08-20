package vnd.blueararat.smssieve;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment3 extends Fragment {

	private TextView log;

	public Fragment3() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment3, container, false);
		String msglog = MainActivity.preferences
				.getString(Receiver.KEY_LOG, "");
		log = (TextView) rootView.findViewById(R.id.log);

		if (!msglog.isEmpty()) {
			log.setText(Html.fromHtml(msglog));
		}
		return rootView;
	}
	
	public void empty() {
		log.setText(R.string.log_empty);
	}
}