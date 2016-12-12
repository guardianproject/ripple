package info.guardianproject.ripple.settings;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import info.guardianproject.ripple.R;
import info.guardianproject.ripple.RippleConstants;
import info.guardianproject.ripple.lockedscreen.PasswordFailsReceiver;

public class SettingsActivity extends PreferenceActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener mSettingsObserver;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mSettingsObserver = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.pref_login_action))) {
                    DevicePolicyManager devicePolicyManager
                            = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName deviceAdminComponentName
                            = new ComponentName(SettingsActivity.this, PasswordFailsReceiver.class);

                    if (!devicePolicyManager.isAdminActive(deviceAdminComponentName)) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.monitor_login_failues));
                        startActivityForResult(intent, RippleConstants.DEVICE_ADMIN_ACTIVATION_REQUEST);
                    }
                }
            }
        };

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mPrefs.registerOnSharedPreferenceChangeListener(mSettingsObserver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RippleConstants.DEVICE_ADMIN_ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_CANCELED) {
                    SharedPreferences.Editor edit = mPrefs.edit();
                    edit.putBoolean(getString(R.string.pref_login_action), false);
                    edit.apply();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
