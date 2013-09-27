package vnd.blueararat.smssieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Fragment2 extends Fragment {

	// MainActivity ma;
	ListView list;
	private int[] images;

	private class AddressItem {

		String address;
		int state;

		public AddressItem(String address, int state) {
			this.address = address;
			this.state = state;
		}

		public String getAddress() {
			return address;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}
	}

	private List<AddressItem> addresses2;

	public Fragment2() {
		images = new int[] { R.drawable.check0, R.drawable.check1,
				R.drawable.check2 };
		// ma = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		addresses2 = new ArrayList<AddressItem>();
		for (String adr : MainActivity.addresses) {
			addresses2.add(new AddressItem(adr, 0));
		}
		ArrayAdapter<AddressItem> adapter = new ArrayAdapter<AddressItem>(
				getActivity(), R.layout.list_item, addresses2) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view;
				final AddressItem ai = getItem(position);
				final TextView title;
				final ImageView image;
				LayoutInflater inflater = (LayoutInflater) getActivity()
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

				if (convertView == null) {
					view = inflater.inflate(R.layout.list_item, parent, false);
				} else {
					view = convertView;
				}

				try {
					title = (TextView) view.findViewById(R.id.title);
					image = (ImageView) view.findViewById(R.id.image);
				} catch (ClassCastException e) {
					throw new IllegalStateException(e.toString(), e);
				}
				title.setText(ai.getAddress());
				image.setImageResource(images[ai.getState()]);
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (ai.getState() == 1
								&& !MainActivity.exceptions.contains(ai
										.getAddress())) {
							Toast.makeText(getContext(),
									R.string.there_is_regex, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						if (ai.getState() == 2
								&& !MainActivity.filters.contains(ai
										.getAddress())) {
							Editor et1 = MainActivity.exceptions.edit();
							et1.putInt(ai.getAddress(), 0);
							et1.commit();
							ai.setState(1);
							image.setImageResource(images[1]);
						} else if (ai.getState() == 2) {
							Editor et = MainActivity.filters.edit();
							et.remove(ai.getAddress());
							et.commit();
							ai.setState(0);
							Set<String> set = MainActivity.regex_filters
									.getAll().keySet();
							for (String exp : set) {
								Pattern p = null;
								try {
									p = Pattern.compile(exp);
								} catch (PatternSyntaxException e) {
									continue;
								}
								Matcher mt = p.matcher(ai.getAddress());
								if (mt.matches()) {
									Toast.makeText(getContext(),
											R.string.there_is_regex,
											Toast.LENGTH_SHORT).show();
									ai.setState(2);
								}
							}
							image.setImageResource(images[ai.getState()]);
						} else if (ai.getState() == 1) {
							Editor et1 = MainActivity.exceptions.edit();
							Editor et2 = MainActivity.filters.edit();
							et1.remove(ai.getAddress());
							et1.commit();
							et2.putInt(ai.getAddress(), 0);
							et2.commit();
							ai.setState(2);
							image.setImageResource(images[2]);
						} else {
							Editor et1 = MainActivity.exceptions.edit();
							et1.putInt(ai.getAddress(), 0);
							et1.commit();
							ai.setState(1);
							image.setImageResource(images[1]);
						}
						Fragment1 f2 = (Fragment1) (((MainActivity) getActivity()).fr[0]);
						if (f2 != null)
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
		for (AddressItem ai : addresses2) {
			if (MainActivity.exceptions.contains(ai.getAddress())) {
				ai.setState(1);
			} else if (MainActivity.filters.contains(ai.getAddress())) {
				ai.setState(2);
			} else {
				ai.setState(0);
			}
		}
		Set<String> set = MainActivity.regex_exceptions.getAll().keySet();
		for (String exp : set) {
			Pattern p = null;
			try {
				p = Pattern.compile(exp);
			} catch (PatternSyntaxException e) {
				continue;
			}
			Matcher mt;
			for (AddressItem ai : addresses2) {

				if (ai.getState() != 1) {
					mt = p.matcher(ai.getAddress());
					if (mt.matches()) {
						ai.setState(1);
					}
				}
			}
		}

		set = MainActivity.regex_filters.getAll().keySet();
		for (String exp : set) {
			Pattern p = null;
			try {
				p = Pattern.compile(exp);
			} catch (PatternSyntaxException e) {
				continue;
			}
			Matcher mt;
			for (AddressItem ai : addresses2) {
				if (ai.getState() == 0) {
					mt = p.matcher(ai.getAddress());
					if (mt.matches()) {
						ai.setState(2);
					}
				}
			}
		}
	}
}