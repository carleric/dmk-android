package com.skogtek.dmk.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.skogtek.dmk.Constants;
import com.skogtek.dmk.R;

public class Prefs extends PreferenceActivity implements OnPreferenceChangeListener 
{
	public static String remoteIP;
	public static int remotePort;
	public static int localPort;
	public static boolean emulationMode;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        Preference prefEmuMode = findPreference("emulation_mode");
        prefEmuMode.setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		emulationMode = Boolean.parseBoolean(newValue.toString());
		Log.d(Constants.LOG_ID, "emulation mode preference changed to:"+newValue.toString());
		return true;
	}
}
