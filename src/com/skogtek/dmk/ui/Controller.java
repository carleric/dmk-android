package com.skogtek.dmk.ui;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skogtek.dmk.Constants;
import com.skogtek.dmk.R;
import com.skogtek.dmk.service.DMKService;
import com.skogtek.dmk.service.EmulatorService;
import com.skogtek.dmk.service.WifiService;

public class Controller extends ListActivity 
{
    private Intent serviceIntent;
	private boolean serviceBound = false;
	//private DMKService dmkService;
	//private ServiceBinder serviceBinder;
	private ListAdapter listAdapter;
	private ListView listView;
	
	 /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    
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
    
    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DMKService.MSG_SET_VALUE:
                    //mCallbackText.setText("Received from service: " + msg.arg1);
                	if(msg.obj != null)
                		log(msg.obj.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            //mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        DMKService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                
                // Give it some value as an example.
                msg = Message.obtain(null,
                        DMKService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            
            // As part of the sample, tell the user what happened.
            Toast.makeText(Controller.this, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            //mCallbackText.setText("Disconnected.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(Controller.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    void doBindService() {
    	
    	Class c = null;
    	if(Prefs.emulationMode){
    		c = EmulatorService.class;
    	}else{
    		c = WifiService.class;
    	}
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(Controller.this, c), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        //mCallbackText.setText("Binding.");
    }
    
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            DMKService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //mCallbackText.setText("Unbinding.");
        }
    }
    
    void startService() {
    	if (mIsBound){
    		 if (mService != null) {
                 try {
                     Message msg = Message.obtain(null,
                             DMKService.MSG_START_SERVING);
                     msg.replyTo = mMessenger;
                     mService.send(msg);
                 } catch (RemoteException e) {
                     // There is nothing special we need to do if the service
                     // has crashed.
                 }
             }
    	}
    }
    
    void stopService() {
    	if (mIsBound){
    		 if (mService != null) {
                 try {
                     Message msg = Message.obtain(null,
                             DMKService.MSG_STOP_SERVING);
                     msg.replyTo = mMessenger;
                     mService.send(msg);
                 } catch (RemoteException e) {
                     // There is nothing special we need to do if the service
                     // has crashed.
                 }
             }
    	}
    }
    
    
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
	        	doBindService();
	        	startService();
	        	return true;
	        case R.id.stop_service:
	        	stopService();
	        	doUnbindService();
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
