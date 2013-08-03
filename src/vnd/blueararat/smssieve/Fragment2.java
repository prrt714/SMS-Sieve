package vnd.blueararat.smssieve;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class Fragment2 extends Fragment {

	// MainActivity ma;
	ListView list;

	public Fragment2() {
		// ma = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_checked,
				MainActivity.addresses) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				final String key = MainActivity.addresses.get(position);
				final int pos = position;
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						boolean b = !MainActivity.filters.contains(key);// !list.isItemChecked(pos);
						boolean b2 = ((CheckedTextView) v).isChecked();
						if (b && b2) {
							Toast.makeText(getContext(),
									R.string.there_is_regex, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						Editor et = MainActivity.filters.edit();
						if (b) {
							et.putInt(key, 0);
						} else {
							et.remove(key);
						}
						et.commit();
						list.setItemChecked(pos, b);
						Fragment1 f2 = (Fragment1) (((MainActivity) getActivity()).fr[0]);
						f2.refresh(null);
					}
				});
				return view;
			}
		};
		View rootView = inflater.inflate(R.layout.fragment2, container, false);
		list = (ListView) rootView.findViewById(R.id.listView1);

		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		refresh();
		return rootView;
	}

	public void refresh() {
		int m = MainActivity.addresses.size();
		for (int i = 0; i < m; i++) {
			list.setItemChecked(i, MainActivity.filters
					.contains(MainActivity.addresses.get(i)));
		}
		Set<String> set = MainActivity.regex_filters.getAll().keySet();
		for (String exp : set) {
			Pattern p = null;
			try {
				p = Pattern.compile(exp);
			} catch (PatternSyntaxException e) {
				continue;
			}
			Matcher mt;
			for (int i = 0; i < m; i++) {
				mt = p.matcher(MainActivity.addresses.get(i));
				if (mt.matches()) {
					list.setItemChecked(i, true);
				}
			}
		}
	}
}