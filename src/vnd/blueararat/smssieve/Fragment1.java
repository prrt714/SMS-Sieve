package vnd.blueararat.smssieve;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Fragment1 extends Fragment {

	public Fragment1() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment1, container, false);
		// CheckBox cb = (CheckBox) rootView.findViewById(R.id.checkBox1);
		final int[] colors = {
				getActivity().getResources().getColor(R.color.disabled),
				getActivity().getResources().getColor(R.color.enabled) };
		final int[] states = { R.string.disabled, R.string.enabled };

		TextView enabled = (TextView) rootView.findViewById(R.id.enabled);
		TextView tv = (TextView) rootView.findViewById(R.id.textView1);

		boolean b = MainActivity.preferences.getBoolean(
				Receiver.KEY_ENABLED, true);
		int i = MainActivity.preferences.getInt(Receiver.KEY_SKIPPED, 0);
		tv.setText(getActivity().getString(R.string.skipped) + i);
		int ind = b ? 1 : 0;
		enabled.setBackgroundColor(colors[ind]);
		enabled.setText(states[ind]);
		enabled.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean b2 = !MainActivity.preferences.getBoolean(
						Receiver.KEY_ENABLED, true);
				Editor et = MainActivity.preferences.edit();
				et.putBoolean(Receiver.KEY_ENABLED, b2);
				int id = b2 ? 1 : 0;
				v.setBackgroundColor(colors[id]);
				((TextView) v).setText(states[id]);
				et.commit();
			}
		});
		// cb.setChecked(b);
		// cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// Editor et = MainActivity.preferences.edit();
		// et.putBoolean(MainActivity.KEY_ENABLED, isChecked);
		// et.commit();
		// }
		// });

		refresh(rootView);
		return rootView;
	}

	public void refresh(View rootView) {
		if (rootView == null)
			rootView = getView();
		final List<String> l2 = new ArrayList<String>();
		l2.addAll(MainActivity.filters.getAll().keySet());
		TextView t = (TextView) rootView.findViewById(R.id.textView2);

		if (l2.isEmpty()) {
			t.setText(R.string.section2_empty);
		} else {
			t.setText(R.string.section2_summary);
		}
		ArrayAdapter<String> a2 = new ArrayAdapter<String>(getActivity(),
				R.layout.list_item2, l2) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view;
				TextView text1, text2;
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

				if (convertView == null) {
					view = inflater.inflate(R.layout.list_item2, parent, false);
				} else {
					view = convertView;
				}

				try {
					text1 = (TextView) view.findViewById(R.id.text1);
					text2 = (TextView) view.findViewById(R.id.text2);
				} catch (ClassCastException e) {
					throw new IllegalStateException(e.toString(), e);
				}
				String s = l2.get(position);
				text1.setText(s);
				text2.setText("" + MainActivity.filters.getInt(s, 0));
				return view;
			}
		};
		ListView list2 = (ListView) rootView.findViewById(R.id.listView2);
		list2.setAdapter(a2);
		list2.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		list2.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final String key = l2.get(position);// ((TextView)
													// v.findViewById(R.id.text1))
				// .getText().toString();
				new AlertDialog.Builder(getActivity())
						// .setIcon(android.R.drawable.ic_dialog_alert)
						// .setTitle("")
						.setMessage(
								getActivity().getString(R.string.remove) + key
										+ " ?")
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Editor et = MainActivity.filters.edit();
										et.remove(key);
										et.commit();
										Fragment2 f3 = (Fragment2) (((MainActivity) getActivity()).fr[1]);
										f3.refresh();
										refresh(null);
									}

								}).setNegativeButton(android.R.string.no, null)
						.show();
				return true;
			}
		});
	}
}