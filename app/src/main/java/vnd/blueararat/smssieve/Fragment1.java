package vnd.blueararat.smssieve;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Fragment1 extends Fragment {

    private int red, green;
    private static final int RECEIVE_SMS_PERMISSION_RESULT = 4;

    public Fragment1() {
    }

    private void invalidateEnabled(View rootView, boolean enabled) {
        TextView textView = rootView.findViewById(R.id.enabled);
        final int[] colors = {
                getActivity().getResources().getColor(R.color.disabled),
                getActivity().getResources().getColor(R.color.enabled)};
        final int[] states = {R.string.disabled, R.string.enabled};
        int ind = enabled ? 1 : 0;
        textView.setBackgroundColor(colors[ind]);
        textView.setText(states[ind]);
    }

    private void invalidateEnabled(boolean enabled) {
        invalidateEnabled(getView(), enabled);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment1, container, false);
        // CheckBox cb = (CheckBox) rootView.findViewById(R.id.checkBox1);

        TextView enabled = (TextView) rootView.findViewById(R.id.enabled);
        TextView tv = (TextView) rootView.findViewById(R.id.textView1);
        TextView tv2 = (TextView) rootView.findViewById(R.id.textView2);

        green = getActivity().getResources().getColor(R.color.green);
        red = getActivity().getResources().getColor(R.color.red);

        boolean b = MainActivity.preferences.getBoolean(Receiver.KEY_ENABLED,
                false);
        int i = MainActivity.preferences.getInt(Receiver.KEY_SKIPPED, 0);
        String matches = MainActivity.preferences.getString(
                Receiver.KEY_MATCHES, "");
        if (matches.length() > 0) {
            tv2.setText(getString(R.string.recent) + matches);
        } else {
            tv2.setText(R.string.section2_summary);
        }

        tv.setText(getActivity().getString(R.string.skipped) + i);
        invalidateEnabled(rootView, b);
        enabled.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean b2 = !MainActivity.preferences.getBoolean(
                        Receiver.KEY_ENABLED, false);
                if (b2) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.RECEIVE_SMS},
                                RECEIVE_SMS_PERMISSION_RESULT);
                    } else {
                        storeEnabledSetting(b2);
                    }
                } else {
                    storeEnabledSetting(b2);
                }
            }
        });
        refresh(rootView);
        return rootView;
    }

    private void storeEnabledSetting(boolean enabled) {
        Editor et = MainActivity.preferences.edit();
        et.putBoolean(Receiver.KEY_ENABLED, enabled);
        et.commit();
        invalidateEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECEIVE_SMS_PERMISSION_RESULT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storeEnabledSetting(true);
                }
                break;
            }
        }
    }

    public void refresh(View rootView) {
        if (rootView == null)
            rootView = getView();
        final List<String> l2 = new ArrayList<String>();
        l2.addAll(new TreeSet<String>(MainActivity.exceptions.getAll().keySet()));
        final int part1 = l2.size();
        l2.addAll(new TreeSet<String>(MainActivity.regex_exceptions.getAll()
                .keySet()));
        final int part2 = l2.size();
        l2.addAll(new TreeSet<String>(MainActivity.filters.getAll().keySet()));
        final int part3 = l2.size();
        l2.addAll(new TreeSet<String>(MainActivity.regex_filters.getAll()
                .keySet()));
        if (rootView == null)
            return;
        TextView t = (TextView) rootView.findViewById(R.id.textView2);

        if (l2.isEmpty()) {
            t.setText(R.string.section2_empty);
        }
        ArrayAdapter<String> a2 = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item2, l2) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                TextView text1, text2, description, description2;
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
                    description = (TextView) view
                            .findViewById(R.id.description);
                    description2 = (TextView) view
                            .findViewById(R.id.description2);

                } catch (ClassCastException e) {
                    throw new IllegalStateException(e.toString(), e);
                }
                String s = l2.get(position);
                text1.setText(s);
                if (position < part1) {
                    description2.setVisibility(View.VISIBLE);
                    description.setVisibility(View.GONE);
                    text2.setTextColor(green);
                    text2.setText("" + MainActivity.exceptions.getInt(s, 0));
                } else if (position < part2) {
                    description2.setVisibility(View.VISIBLE);
                    description.setVisibility(View.VISIBLE);
                    text2.setTextColor(green);
                    text2.setText(""
                            + MainActivity.regex_exceptions.getInt(s, 0));
                } else if (position < part3) {
                    description2.setVisibility(View.GONE);
                    description.setVisibility(View.GONE);
                    text2.setTextColor(red);
                    text2.setText("" + MainActivity.filters.getInt(s, 0));
                } else {
                    description2.setVisibility(View.GONE);
                    description.setVisibility(View.VISIBLE);
                    text2.setTextColor(red);
                    text2.setText("" + MainActivity.regex_filters.getInt(s, 0));
                }
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

                final CharSequence[] items = {getString(R.string.remove),
                        getString(R.string.edit)};
                final String key = l2.get(position);
                final int pos = position;

                new AlertDialog.Builder(getActivity()).setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0:
                                        new AlertDialog.Builder(getActivity())
                                                // .setIcon(android.R.drawable.ic_dialog_alert)
                                                // .setTitle("")
                                                .setMessage(
                                                        getActivity().getString(
                                                                R.string.remove)
                                                                + key + " ?")
                                                .setPositiveButton(
                                                        android.R.string.yes,
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                Editor et = null;
                                                                if (pos < part1) {
                                                                    et = MainActivity.exceptions
                                                                            .edit();
                                                                } else if (pos < part2) {
                                                                    et = MainActivity.regex_exceptions
                                                                            .edit();
                                                                } else if (pos < part3) {
                                                                    et = MainActivity.filters
                                                                            .edit();
                                                                } else {
                                                                    et = MainActivity.regex_filters
                                                                            .edit();
                                                                }
                                                                et.remove(key);
                                                                et.commit();
                                                                Fragment2 f3 = (Fragment2) (((MainActivity) getActivity()).fr[1]);
                                                                if (f3 != null)
                                                                    f3.refresh();
                                                                refresh(null);
                                                            }

                                                        })
                                                .setNegativeButton(
                                                        android.R.string.no, null)
                                                .show();
                                        break;
                                    case 1:
                                        final EditText input = new EditText(
                                                getActivity());
                                        input.setText(key);
                                        new AlertDialog.Builder(getActivity())
                                                // .setTitle(R.string.edit)
                                                .setMessage(R.string.edit)
                                                .setView(input)
                                                .setPositiveButton(
                                                        android.R.string.yes,
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int whichButton) {
                                                                String str = input
                                                                        .getText()
                                                                        .toString();
                                                                if (str.equals(key))
                                                                    return;

                                                                SharedPreferences sp = null;
                                                                boolean isRegular = false;
                                                                if (pos < part1) {
                                                                    sp = MainActivity.exceptions;
                                                                } else if (pos < part2) {
                                                                    sp = MainActivity.regex_exceptions;
                                                                    isRegular = true;
                                                                } else if (pos < part3) {
                                                                    sp = MainActivity.filters;
                                                                } else {
                                                                    sp = MainActivity.regex_filters;
                                                                    isRegular = true;
                                                                }

                                                                if (sp.contains(str))
                                                                    return;
                                                                if (isRegular
                                                                        && !((MainActivity) getActivity())
                                                                        .isValid(str)) {
                                                                    return;
                                                                }
                                                                Editor et = sp
                                                                        .edit();
                                                                et.remove(key);
                                                                if (str.length() != 0)
                                                                    et.putInt(str,
                                                                            0);
                                                                et.commit();
                                                                Fragment2 f3 = (Fragment2) (((MainActivity) getActivity()).fr[1]);
                                                                if (f3 != null)
                                                                    f3.refresh();
                                                                refresh(null);
                                                            }
                                                        })
                                                .setNegativeButton(
                                                        android.R.string.no, null)
                                                .show();
                                        break;
                                }
                            }
                        }).show();

                return true;
            }
        });
    }
}