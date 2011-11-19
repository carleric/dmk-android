package com.skogtek.dmk.ui;

import com.skogtek.dmk.Constants;
import com.skogtek.dmk.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Prefs extends PreferenceActivity implements OnPreferenceChangeListener 
{
	public static String remoteIP;
	public static int remotePort;
	public static int localPort;
	public static boolean emulationMode;
	
	private EditTextPreference editTextPref1;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        //setPreferenceScreen(createPreferenceHierarchy());
        Preference prefEmuMode = findPreference("emulation_mode");
        prefEmuMode.setOnPreferenceChangeListener(this);
    }
	
	private PreferenceScreen createPreferenceHierarchy() 
	{
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        
        /*PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle("inline");
        root.addPreference(inlinePrefCat);*/
        
        CheckBoxPreference togglePref = new CheckBoxPreference(this);
        togglePref.setKey("emulation_mode");
        togglePref.setTitle(R.string.emulation_mode);
        togglePref.setSummary(R.string.emulation_mode);
        togglePref.setDisableDependentsState(true);
        togglePref.setOnPreferenceChangeListener(this);
        root.addPreference(togglePref);
        
        // Dialog based preferences
        /*PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
        dialogBasedPrefCat.setTitle("dialog");
        root.addPreference(dialogBasedPrefCat);*/
               
        editTextPref1 = new EditTextPreference(this);
        editTextPref1.setDialogTitle(R.string.remote_ip);
        editTextPref1.setKey("remote_ip");
        editTextPref1.setTitle(R.string.remote_ip);
        editTextPref1.setSummary(R.string.remote_ip);
        
        root.addPreference(editTextPref1);
        
        
        /*EditTextPreference editTextPref2 = new EditTextPreference(this);
        editTextPref2.setDialogTitle(R.string.remote_port);
        editTextPref2.setKey("remote_port");
        editTextPref2.setTitle(R.string.remote_port);
        editTextPref2.setSummary(R.string.remote_port);
        editTextPref2.setDependency("emulation_mode");
        inlinePrefCat.addPreference(editTextPref2);
        
        EditTextPreference editTextPref3 = new EditTextPreference(this);
        editTextPref3.setDialogTitle(R.string.local_port);
        editTextPref3.setKey("local_port");
        editTextPref3.setTitle(R.string.local_port);
        editTextPref3.setSummary(R.string.local_port);
        editTextPref3.setDependency("emulation_mode");
        inlinePrefCat.addPreference(editTextPref3);     
        */  
        
        return root;
    }
	/*
	@Override
	public void onContentChanged ()
	{
		try
		{
			if(editTextPref1 != null)
				editTextPref1.setDependency("emulation_mode");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	*/
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Log.d(Constants.LOG_ID, "preference list item clicked");
		Toast.makeText(getApplicationContext(), ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		emulationMode = Boolean.parseBoolean(newValue.toString());
		Log.d(Constants.LOG_ID, "emulation mode preference changed to:"+newValue.toString());
		return true;
	}
}
