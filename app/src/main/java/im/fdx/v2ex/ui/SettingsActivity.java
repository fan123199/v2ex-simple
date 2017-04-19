package im.fdx.v2ex.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.elvishew.xlog.XLog;

import im.fdx.v2ex.AlarmReceiver;
import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.UpdateService;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;

import static im.fdx.v2ex.R.xml.preference;
import static im.fdx.v2ex.utils.Keys.ACTION_LOGOUT;

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
        private ListPreference listPreference;

        private int count;

        public SettingsFragment() {
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(preference);

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
                                        HintUI.t(getActivity(), "已退出登录");
                                    }
                                });
                        alert.create().show();
                        return true;
                    }
                });

                listPreference = (ListPreference) findPreference("pref_msg_period");
                if (listPreference.getEntry() != null) {
                    listPreference.setSummary(listPreference.getEntry());//初始化时设置summary
                }

                if (!sharedPreferences.getBoolean("pref_msg", false)) {
                    findPreference("pref_msg_period").setEnabled(false);
                    findPreference("pref_background_msg").setEnabled(false);

                }
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

            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info;
            try {
                info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                findPreference("pref_version").setSummary(info.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            final String[] jiang = getResources().getStringArray(R.array.j);
            count = 7;
            findPreference("pref_version").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (count < 0) {
                        count = 7;
                        HintUI.t(getActivity(), jiang[(int) ((System.currentTimeMillis() / 100) % 3)]);
                    }
                    count--;
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
            Intent intent = new Intent(ACTION_LOGOUT);
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
            Intent intent = new Intent(getActivity(), UpdateService.class);
            switch (key) {
                case "pref_msg":

                    if (sharedPreferences.getBoolean(key, false)) {
                        getActivity().startService(intent);
                        findPreference("pref_msg_period").setEnabled(true);
                        findPreference("pref_background_msg").setEnabled(true);
                    } else {
                        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancel(AlarmReceiver.notifyID);
                        getActivity().stopService(intent);
                        findPreference("pref_msg_period").setEnabled(false);
                        findPreference("pref_background_msg").setEnabled(false);

                    }
                    break;
                case "pref_background_msg":
//不需要发送广播了，通过sharedpreference就可以获得变化了
                    break;
                case "pref_msg_period":
                    listPreference.setSummary(listPreference.getEntry());
                    getActivity().startService(intent);
                    XLog.d("pref_msg_period changed");
                    break;
            }
        }

    }
}
