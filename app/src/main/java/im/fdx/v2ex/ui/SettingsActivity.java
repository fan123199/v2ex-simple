package im.fdx.v2ex.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import im.fdx.v2ex.R;
import im.fdx.v2ex.utils.L;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new SettingsFragment())
                .commit();

    }




    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public final static String CONF_WIFI = "perf_wifi";
        SharedPreferences sharedPreferences;

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            findPreference("pref_about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                       L.t(getActivity(), "没有找到V2EX客户端");
                    }
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            super.onPause();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // TODO: 2015/9/15
        }


    }



}
