package com.skogtek.dmk.ui;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
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
import com.skogtek.dmk.service.EmulatorService;
import com.skogtek.dmk.service.IRemoteService;
import com.skogtek.dmk.service.IRemoteServiceCallback;
import com.skogtek.dmk.service.ISecondary;
import com.skogtek.dmk.service.WifiService;
//import com.skogtek.dmk.service.WifiService.ServiceBinder;

public class Controller extends ListActivity 
{
    private Intent serviceIntent;
	private boolean serviceBound = false;
	//private DMKService dmkService;
	//private ServiceBinder serviceBinder;
	private ListAdapter listAdapter;
	private ListView listView;
	
	/** The primary interface we will be calling on the service. */
    IRemoteService mService = null;
    /** Another interface we use on the service. */
    ISecondary mSecondaryService = null;
    
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
        	/*serviceBinder = ((ServiceBinder)service);
        	serviceBinder.setController(Controller.this);
        	dmkService = serviceBinder.getService();
        	dmkService.start();
            serviceBound = true;*/
        	mService = IRemoteService.Stub.asInterface(service);
            //mKillButton.setEnabled(true);
            //mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            
            // As part of the sample, tell the user what happened.
            //Toast.makeText(Binding.this, R.string.remote_service_connected, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
            serviceBound = false;
        }
    };
    
    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void valueChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }
    };
    
    private static final int BUMP_MSG = 1;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    //mCallbackText.setText("Received from service: " + msg.arg1);
                	Log.d(Constants.LOG_ID, String.valueOf(msg.arg1));
                	log( String.valueOf(msg.arg1));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	//super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) 
        {
	        case R.id.start_service:
	        	if(Prefs.emulationMode){
		        	//create the service and bind this Activity to it
		            serviceIntent = new Intent(this, EmulatorService.class);
	        	}else{
	        		//create the service and bind this Activity to it
		            serviceIntent = new Intent(this, WifiService.class);
	        	}
	            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
	        	return true;
	        case R.id.stop_service:
	        	if(serviceBound)
	        	{
		            //dmkService.stop();
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
    
    private void log(final String msg)
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
