package com.skogtek.android.dmklogger;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ListView;

public class LoggerPrefs extends PreferenceActivity 
{
	public static String remoteIP;
	public static int remotePort;
	public static int localPort;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.logger_prefs);
        
        
    }
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		
	}
	
	
}
