package im.fdx.v2ex.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.cookie.MyCookieJar;
import im.fdx.v2ex.utils.HintUI;
import okhttp3.CookieJar;

import static android.os.Build.VERSION_CODES.M;
import static im.fdx.v2ex.MyApp.USE_API;
import static im.fdx.v2ex.MyApp.USE_WEB;
import static im.fdx.v2ex.R.string.username;
import static im.fdx.v2ex.ui.LoginActivity.action_login;
import static im.fdx.v2ex.ui.LoginActivity.action_logout;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                onBackPressed();
            }
        });

        getFragmentManager().beginTransaction()
                .add(R.id.container, new SettingsFragment())
                .commit();

    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String PREF_WIFI = "pref_wifi";
        public static final String PREF_RATES = "pref_rates";
        //        public static final String PREF_MODE = "pref_http_mode";
        private static final String PREF_LOGOUT = "pref_logout";
        SharedPreferences sharedPreferences;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(action_login)) {

                }
            }
        };


        public SettingsFragment() {
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


            if (MyApp.getInstance().isLogin()) {

                addPreferencesFromResource(R.xml.preference_login);

                findPreference("group_user").setTitle(sharedPreferences.getString("username", getString(R.string.user)));

                findPreference(PREF_LOGOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                                .setTitle("确定要退出吗")
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();

                                    }
                                })
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        removeCookie();
                                        notifyAllActivities();
                                        findPreference(PREF_LOGOUT).setEnabled(false);
                                        sharedPreferences.edit().remove("is_login").apply();
                                        dialog.dismiss();

                                        HintUI.t(getActivity(), "已注销登录");

                                    }
                                });
                        alert.create().show();
                        return true;
                    }
                });
            }

            findPreference(PREF_RATES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        HintUI.t(getActivity(), "没有找到V2EX客户端");
                    }
                    return true;
                }
            });

        }

        private void addSettings() {
            PreferenceScreen screen = this.getPreferenceScreen(); // "null". See onViewCreated.

            // Create the Preferences Manually - so that the key can be set programatically.
            PreferenceCategory category = new PreferenceCategory(screen.getContext());
            category.setTitle("Channel Configuration");
            category.setOrder(0);
            screen.addPreference(category);

            CheckBoxPreference checkBoxPref = new CheckBoxPreference(screen.getContext());
            checkBoxPref.setKey("_ENABLED");
            checkBoxPref.setTitle("Enabled");
            checkBoxPref.setSummary("CCCC");
            checkBoxPref.setChecked(true);

            category.addPreference(checkBoxPref);
        }

        private void notifyAllActivities() {

            MyApp.getInstance().setLogin(false);
            Intent intent = new Intent(action_logout);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

        }

        private void removeCookie() {
            HttpHelper.myCookieJar.clear();
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
            Log.w("PREF", key);

            switch (key) {
            }
        }

    }


}
