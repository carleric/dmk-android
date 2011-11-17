package com.skogtek.dmk.ui;

import com.skogtek.dmk.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ListView;

public class Prefs extends PreferenceActivity 
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
    }
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		
	}
	
	
}
