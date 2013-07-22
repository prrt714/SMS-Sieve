package vnd.blueararat.smssieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends FragmentActivity {

	static SharedPreferences preferences, filters;
	static List<String> addresses;
	Fragment[] fr = new Fragment[2];
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		addresses = getAddresses();
		filters = getSharedPreferences("filters", MODE_PRIVATE);
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
			final EditText input = new EditText(this);
			new AlertDialog.Builder(this)
					// .setTitle(R.string.input)
					.setMessage(R.string.input)
					.setView(input)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String str = input.getText().toString();
									String[] ss = str.split(";");
									Editor et = MainActivity.filters.edit();
									for (String s : ss) {
										s = s.trim();
										boolean b = !filters.contains(s);
										if (b) {
											et.putInt(s, 0);
										}
									}
									et.commit();
									((Fragment1) fr[0]).refresh(null);
									((Fragment2) fr[1]).refresh();
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();

			break;
		case R.id.export:
			Set<String> set = filters.getAll().keySet();
			String s = "";
			for (String el : set) {
				s += ";\n" + el;
			}
			s = s.substring(2);
			Intent shareIntent = ShareCompat.IntentBuilder.from(this)
					.setType("plain/text").setText(s)
					// .setChooserTitle(R.string.share)
					.setSubject(getString(R.string.subject)).getIntent();
			startActivity(shareIntent);

			break;
		}
		return super.onOptionsItemSelected(item);
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
			}
			return fr[position];
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Locale l = Locale.getDefault();
			switch (position) {
			// case 0:
			// return getString(R.string.title_section1);// .toUpperCase(l);
			case 0:
				return getString(R.string.title_section2);// .toUpperCase(l);
			case 1:
				return getString(R.string.title_section3);// .toUpperCase(l);
			}
			return null;
		}
	}
}