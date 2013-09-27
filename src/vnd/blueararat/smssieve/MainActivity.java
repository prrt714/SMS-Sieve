package vnd.blueararat.smssieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	static SharedPreferences preferences, filters, regex_filters, exceptions,
			regex_exceptions;
	static List<String> addresses;
	static final String REGEX_PART1 = "regex(";
	static final String EXC_PART1 = "excep(";
	Fragment[] fr = new Fragment[3];
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		addresses = getAddresses();
		filters = getSharedPreferences(Receiver.FILTERS, MODE_PRIVATE);
		regex_filters = getSharedPreferences(Receiver.REGEX_FILTERS,
				MODE_PRIVATE);
		exceptions = getSharedPreferences(Receiver.EXCEPTIONS, MODE_PRIVATE);
		regex_exceptions = getSharedPreferences(Receiver.REGEX_EXCEPTIONS,
				MODE_PRIVATE);
		setContentView(R.layout.activity_main);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	public List<String> getAddresses() {
		List<String> a = new ArrayList<String>();
		// a.add("result1");
		// a.add("result2");
		// a.add("result3");

		Uri uri = Uri.parse("content://sms/conversations/");
		Cursor c1 = getContentResolver().query(uri, null, null, null, null);

		final String[] projection = new String[] { "address" };
		Uri uri2 = Uri.parse("content://sms/inbox/");

		while (c1.moveToNext()) {
			int thread_id = c1.getInt(0);
			Cursor c2 = getContentResolver().query(uri2, projection,
					"thread_id" + " = " + thread_id, null, null);

			if (c2.getCount() > 0) {
				c2.moveToFirst();
				String result = c2.getString(0);
				a.add(result);
			}
			c2.close();
		}
		c1.close();
		Collections.sort(a);
		String matches = preferences.getString(Receiver.KEY_MATCHES, "");
		if (!matches.isEmpty()) {
			String newadrs[] = matches.split(" ");
			for (String s : newadrs) {
				if (!s.isEmpty() && !a.contains(s))
					a.add(s);
			}
		}

		return a;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final View view = inflater.inflate(R.layout.input_dialog, null,
					false);
			ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, addresses);

			final AutoCompleteTextView input = (AutoCompleteTextView) view
					.findViewById(R.id.input);
			input.setAdapter(adapter2);
			final CheckBox chb = (CheckBox) view.findViewById(R.id.chb_reg);
			final CheckBox chb_exc = (CheckBox) view.findViewById(R.id.chb_exc);

			new AlertDialog.Builder(this)
					.setMessage(R.string.input)
					.setView(view)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String str = input.getText().toString();
									String[] ss = str.split(";");
									Editor et1 = exceptions.edit();
									Editor et2 = regex_exceptions.edit();
									Editor et3 = filters.edit();
									Editor et4 = regex_filters.edit();
									boolean isException = (chb_exc.isChecked() && !str
											.contains(EXC_PART1));
									boolean isRegex = (chb.isChecked() && !str
											.contains(REGEX_PART1));
									for (String s : ss) {
										s = s.trim();
										if (s.length() == 0)
											continue;
										boolean isexc = s.startsWith(EXC_PART1);
										if (isexc)
											s = s.substring(6,
													s.lastIndexOf(")"));
										boolean isreg = s
												.startsWith(REGEX_PART1);
										if (isreg)
											s = s.substring(6,
													s.lastIndexOf(")"));
										if (isRegex || isreg) {
											if (isException || isexc) {
												if (!regex_exceptions
														.contains(s)) {
													if (isValid(s))
														et2.putInt(s, 0);
												}
											} else {
												if (!regex_filters.contains(s)) {
													if (isValid(s))
														et4.putInt(s, 0);
												}
											}
										} else {
											if (isException || isexc) {
												if (!exceptions.contains(s)) {
													et1.putInt(s, 0);
												}
											} else {
												if (!filters.contains(s)) {
													if (isValid(s))
														et3.putInt(s, 0);
												}
											}

										}
									}
									et1.commit();
									et2.commit();
									et3.commit();
									et4.commit();
									if (fr[0] != null)
										((Fragment1) fr[0]).refresh(null);
									if (fr[1] != null)
										((Fragment2) fr[1]).refresh();
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();

			break;
		case R.id.export:

			Set<String> set = exceptions.getAll().keySet();
			String s = "";
			for (String el : set) {
				s += ";\n" + EXC_PART1 + el + ")";
			}
			set = regex_exceptions.getAll().keySet();
			for (String el : set) {
				s += ";\n" + EXC_PART1 + REGEX_PART1 + el + "))";
			}
			set = filters.getAll().keySet();
			for (String el : set) {
				s += ";\n" + el;
			}
			set = regex_filters.getAll().keySet();
			for (String el : set) {
				s += ";\n" + REGEX_PART1 + el + ")";
			}
			if (s.startsWith(";\n"))
				s = s.substring(2);
			Intent shareIntent = ShareCompat.IntentBuilder.from(this)
					.setType("plain/text").setText(s)
					// .setChooserTitle(R.string.share)
					.setSubject(getString(R.string.subject)).getIntent();
			startActivity(shareIntent);

			break;
		case R.id.help:
			View view2 = LayoutInflater.from(this).inflate(
					R.layout.help_dialog, null, false);
			ListView lv = (ListView) view2.findViewById(R.id.example_list);
			TextView ht = (TextView) view2.findViewById(R.id.title);
			ht.setText(getString(R.string.help_title,
					getString(android.R.string.yes)));
			final String[] titles = getResources().getStringArray(
					R.array.examples_titles);
			final String[] summaries = getResources().getStringArray(
					R.array.examples_explanations);
			final boolean[] checked = { false, false, false, false };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.help_list_item2, titles) {

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					final int n = position;
					final boolean b4 = regex_filters.contains(titles[n]);
					View view;
					TextView title, summary;
					CheckBox ch;
					if (convertView == null) {
						view = LayoutInflater.from(getContext()).inflate(
								R.layout.help_list_item2, parent, false);
					} else {
						view = convertView;
					}

					try {
						title = (TextView) view.findViewById(R.id.title3);
						summary = (TextView) view
								.findViewById(R.id.explanation);
						ch = (CheckBox) view.findViewById(R.id.checkbox2);
					} catch (ClassCastException e) {
						throw new IllegalStateException(e.toString(), e);
					}
					title.setText(titles[position]);
					summary.setText(summaries[position]);
					ch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							checked[n] = isChecked;
						}
					});
					return view;
				}
			};
			lv.setAdapter(adapter);

			new AlertDialog.Builder(this)
					.setView(view2)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									boolean changed = false;
									Editor et = regex_filters.edit();
									for (int i = 0; i < titles.length; i++) {
										if (checked[i]) {
											if (!regex_filters
													.contains(titles[i])) {
												et.putInt(titles[i], 0);
												changed = true;
											}
										}
									}
									if (changed) {
										et.commit();
										if (fr[0] != null)
											((Fragment1) fr[0]).refresh(null);
										if (fr[1] != null)
											((Fragment2) fr[1]).refresh();
									}
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();
			break;
		case R.id.clear_log:
			new AlertDialog.Builder(this)
					// .setIcon(android.R.drawable.ic_dialog_alert)
					// .setTitle("")
					.setMessage(R.string.clear_all_sms)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Editor et = preferences.edit();
									et.remove(Receiver.KEY_LOG);
									et.commit();
									if (fr[2] != null)
										((Fragment3) fr[2]).empty();
								}

							}).setNegativeButton(android.R.string.no, null)
					.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean isValid(String s) {
		Pattern p = null;
		try {
			p = Pattern.compile(s);
		} catch (PatternSyntaxException e) {
			Toast.makeText(
					this,
					getString(R.string.regex) + " \"" + s + "\" "
							+ getString(R.string.wrong_pattern),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				fr[0] = new Fragment1();
				break;
			case 1:
				fr[1] = new Fragment2();
				break;
			case 2:
				fr[2] = new Fragment3();
				break;

			}
			return fr[position];
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Locale l = Locale.getDefault();
			switch (position) {
			// case 0:
			// return getString(R.string.title_section1);// .toUpperCase(l);
			case 0:
				return getString(R.string.title_section1);// .toUpperCase(l);
			case 1:
				return getString(R.string.title_section2);// .toUpperCase(l);
			case 2:
				return getString(R.string.title_section3);// .toUpperCase(l);

			}
			return null;
		}
	}
}