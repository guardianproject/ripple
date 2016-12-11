package info.guardianproject.ripple.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import info.guardianproject.ripple.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
