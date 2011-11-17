package com.skogtek.dmk.ui;

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

import com.skogtek.dmk.R;
import com.skogtek.dmk.Constants;
import com.skogtek.dmk.service.DMKService;
import com.skogtek.dmk.service.WifiService;
import com.skogtek.dmk.service.WifiService.ServiceBinder;

public class Controller extends ListActivity 
{
    private Intent serviceIntent;
	private boolean serviceBound = false;
	private DMKService dmkService;
	private ServiceBinder serviceBinder;
	private ListAdapter listAdapter;
	private ListView listView;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Restore preferences
        Resources res = getResources();
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
        Prefs.remoteIP = settings.getString("remote_ip", "192.168.1.5");
        Prefs.remotePort = settings.getInt("remote_port", 1703);
        Prefs.localPort = settings.getInt("local_port", 1703);
        Prefs.emulationMode = settings.getBoolean("emulation_mode", false);
        
        //create the service and bind this Activity to it
        serviceIntent = new Intent(this, WifiService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        
        listAdapter = new ListAdapter(this);
        setListAdapter(listAdapter);
        
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
    
	private ServiceConnection serviceConnection = new ServiceConnection() 
	{
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
        	Log.e(Constants.LOG_ID, "service is connected, setting bridge between controller and service");
        	serviceBinder = ((ServiceBinder)service);
        	serviceBinder.setController(Controller.this);
        	dmkService = serviceBinder.getService();
            
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
            serviceBound = false;
        }
    };
    
    
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
	        		dmkService.start();
	        	}
	            return true;
	        case R.id.stop_logging:
	        	if(serviceBound)
	        	{
		            dmkService.stop();
	        	}
	            return true;
	        case R.id.preferences:
	        	Intent prefsActivity = new Intent(getBaseContext(),Prefs.class);
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
            	listAdapter.appendItem(msg);
            	
            	//listView.setSelectionFromTop(loggerListAdapter.getCount(), 20);
            	listView.smoothScrollToPosition(listAdapter.getCount());
    		}
    	});
    	
    	//send to debug log too
    	Log.d(Constants.LOG_ID, msg);
    	
    	
    }
    
    

    
}
