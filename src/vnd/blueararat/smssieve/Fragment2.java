package vnd.blueararat.smssieve;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
						boolean b = !MainActivity.filters.contains(key);//!list.isItemChecked(pos);
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
//		 list.setOnItemClickListener(new OnItemClickListener() {
//		
//		 @Override
//		 public void onItemClick(AdapterView<?> parent, View view,
//		 int position, long id) {
//		 CheckedTextView v = (CheckedTextView) view;
//		 boolean b = !v.isChecked();
//		 v.setChecked(b);
//		 Editor et = MainActivity.filters.edit();
//		 String key = MainActivity.addresses.get(position);//
//		 v.getText().toString();
//		 if (b) {
//		 et.putInt(key, 0);
//		 } else {
//		 et.remove(key);
//		 }
//		 et.commit();
//		 Fragment1 f2 = (Fragment1) (((MainActivity) getActivity()).fr[1]);
//		 f2.refresh(null);
//		 }
//		 });
		return rootView;
	}

	public void refresh() {
		int m = MainActivity.addresses.size();
		for (int i = 0; i < m; i++) {
			list.setItemChecked(i, MainActivity.filters
					.contains(MainActivity.addresses.get(i)));
		}
	}
}