/*
 */

package com.skogtek.android.dmklogger;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.skogtek.android.dmklogger.LoggerService.LoggerBinder;


/**
 * 
 */
public class LoggerController extends ListActivity 
{
    private Intent loggerServiceIntent;
	private boolean serviceBound = false;
	private LoggerService loggerService;
	private LoggerBinder loggerBinder;
	private LoggerListAdapter loggerListAdapter;
	private ListView listView;
	
	private ServiceConnection serviceConnection = new ServiceConnection() 
	{
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
        	Log.e(LoggerConstants.LOG_ID, "service is connected, setting bridge between controller and service");
        	loggerBinder = ((LoggerBinder)service);
        	loggerBinder.setLoggerController(LoggerController.this);
            loggerService = loggerBinder.getService();
            
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
            serviceBound = false;
        }
    };
    
    @Override
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Restore preferences
        Resources res = getResources();
        SharedPreferences settings = getSharedPreferences(LoggerConstants.PREFS_FILE_NAME, 0);
        LoggerPrefs.remoteIP = settings.getString("remote_ip", "192.168.1.5");
        LoggerPrefs.remotePort = settings.getInt("remote_port", 1703);
        LoggerPrefs.localPort = settings.getInt("local_port", 1703);
        

        //create the service and bind this Activity to it
        loggerServiceIntent = new Intent(this, LoggerService.class);
        bindService(loggerServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        
        
        loggerListAdapter = new LoggerListAdapter(this);
        setListAdapter(loggerListAdapter);
        
        listView = getListView();
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new OnItemClickListener() 
        {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                Toast.LENGTH_SHORT).show();
          }
        });
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	//super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logger_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) 
        {
	        case R.id.start_logging:
	        	if(serviceBound)
	        	{
	        		loggerService.startLogging();
	        	}
	            return true;
	        case R.id.stop_logging:
	        	if(serviceBound)
	        	{
		            loggerService.stopLogging();
	        	}
	            return true;
	        case R.id.preferences:
	        	Intent prefsActivity = new Intent(getBaseContext(),LoggerPrefs.class);
	        	startActivity(prefsActivity);
	            return true;
	        case R.id.exit_app:
	        	finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    public void log(final String msg)
    {
    	this.runOnUiThread(new Runnable()
    	{
    		public void run()
    		{
    			//post msg to listview
            	loggerListAdapter.appendItem(msg);
            	
            	//listView.setSelectionFromTop(loggerListAdapter.getCount(), 20);
            	listView.smoothScrollToPosition(loggerListAdapter.getCount());
            	
    		}
    	});
    	
    	//send to debug log too
    	Log.d(LoggerConstants.LOG_ID, msg);
    	
    	
    }
    
    

    
}
