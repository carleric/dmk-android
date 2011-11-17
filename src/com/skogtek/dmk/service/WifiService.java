/*
 * 
 */

package com.skogtek.dmk.service;


import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;

import com.skogtek.dmk.R;
import com.skogtek.dmk.ui.Controller;
import com.skogtek.dmk.ui.Prefs;
import com.skogtek.dmk.Constants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 */
public class WifiService extends Service
{
    private NotificationManager notificationManager;
    private ServiceThread serviceThread;
    private boolean doLogging = false;
    private WifiManager wifiManager;
    private int wifiState;
    private int wifiNetId;
    private final ServiceBinder serviceBinder = new ServiceBinder();
    private Controller loggerController;
    
    private DatagramPacket packet;
    private DatagramSocket socket;
    //private byte[] buffer; 
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;
    
    private WifiReceiver wifiReceiver;
    private WifiConfiguration config;
    
    

    @Override
    public void onCreate() 
    {
    	//loggerController.log("LoggerServiceThread.onCreate()");
    	Log.e(Constants.LOG_ID, "LoggerServiceThread.onCreate");
    	
    	notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	showNotification();
    	
    	// start logging thread
        serviceThread = new ServiceThread();
        serviceThread.start();
    }
    
    public void stopLogging()
    {
    	doLogging = false;
    	
    	if(socket != null)
    	{
	    	socket.disconnect();
	    	socket.close();
	    	socket = null;
	    	//buffer = null;
	    	packet = null;
	    	
    	}
    }
    
    public int startLogging()
    {
    	
    	new Thread(new Runnable()
    	{
    		public void run()
    		{
    			loggerController.log("connecting to socket...");
    			
    			localAddress =  new InetSocketAddress(Prefs.localPort);
    			remoteAddress =  new InetSocketAddress(Prefs.remoteIP, Prefs.remotePort);
    			
    			//set up buffer and packet for use while logging
        		byte [] buffer = new byte[1024];
        		packet = new DatagramPacket(buffer, buffer.length);
    			
    			try 
    			{
    				//binds to local address
    				socket = new DatagramSocket(localAddress);
    				socket.setSoTimeout(6000);
    				
    				//connects to remote address
    				socket.connect(remoteAddress);
    				
    				//this code looks useless, but seems to really help initialize the connection!
    				byte [] test = new byte[]{'h','e','l','l','o'};
    				socket.send(new DatagramPacket(test,5));
    				
    				if(socket.isConnected())
    				{
    					loggerController.log("socket is connected to local_port="+socket.getLocalPort()+" remote_address="+socket.getRemoteSocketAddress().toString());
    					doLogging = true;
    				}else
    				{
    					loggerController.log("socket failed to connect");	
    				}
    				
    				
    					
    			} 
    			catch (Exception e) {
    				final String msg = e.getMessage();
    				loggerController.log("error setting up socket "+msg);	
    			}
    		}
    	}).start();
		
		return START_STICKY;
    }
    

    @Override
    public void onDestroy() 
    {
    	loggerController.log("LoggerService.onDestroy, stopping thread and destroying socket");
    	
    	serviceThread.stopRunning();
    	
    	if(socket != null)
    	{
	    	socket.disconnect();
	    	socket.close();
	    	socket = null;
    	}
    	
        // Cancel the notification -- we use the same ID that we had used to start it
        notificationManager.cancel(R.string.service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.service_finished, Toast.LENGTH_SHORT).show();
    }
    
    public class ServiceBinder extends Binder 
    {
    	public void setController(Controller lc)
    	{
    		loggerController = lc;
    	}
    	
        public WifiService getService() 
        {
            return WifiService.this;
        }
        
        protected boolean onTransact (int code, Parcel data, Parcel reply, int flags)
        {
        	return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return serviceBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() 
    {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.string.service_started, notification);
    }

    
    private class ServiceThread extends Thread
    {
    	private boolean run = true; 
    	
    	public void run()
    	{
    		// run is true by default, until stopThread() is called.
    		while(run)
    		{
    			//...spin here until doLogging is set to true
        		while(doLogging)
        		{
        			//receive data
        			try 
        			{
						socket.receive(packet);
					
        				String msg = new String(packet.getData(), 0, packet.getLength());
        				
        				//just output to screen, don't do any parsing in this app
        				loggerController.log(msg);
        				
					} 
        			catch (InterruptedIOException ior)
        			{
        				loggerController.log("socket.receive timeout");
        			}
        			
        			catch (Exception e) 
					{
						//Log.e(LoggerConstants.LOG_ID, "error receiving from socket "+e.getMessage());
						loggerController.log("error in read operation: "+ e.getMessage());
					}
        		}
        		
    		}
    		
    		return;
    		
    	}
    	
    	public void stopRunning()
    	{
    		run = false;
    	}
        
    }
    
    
    /**
     * connect to wifi...this code is not used since we decided to put dmk boxes into infrastructure mode for Android installations.
     * user is expected to manually connect to the same wifi access point that dmk box gets connected to and listen to dmk box there
     */
    private void connectWifi()
    {
    	
		//change system setting to static IP
		Settings.System.putInt(loggerController.getContentResolver(), Settings.System.WIFI_USE_STATIC_IP, 1);
		Settings.System.putString(loggerController.getContentResolver(), Settings.System.WIFI_STATIC_IP, "192.168.1.4");
		Settings.System.putString(loggerController.getContentResolver(), Settings.System.WIFI_STATIC_NETMASK, "255.255.255.0");
		Settings.System.putString(loggerController.getContentResolver(), Settings.System.WIFI_STATIC_GATEWAY, "192.168.1.1");
		//Settings.System.putString(loggerController.getContentResolver(), Settings.System.WIFI_STATIC_DNS1, "192.168.1.1");
		//Settings.System.putString(loggerController.getContentResolver(), Settings.System.WIFI_STATIC_DNS2, "192.168.1.1");
		
		
		//enable wifi
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    
        if(!wifiManager.isWifiEnabled())
        	if(wifiManager.setWifiEnabled(true))
        	{
        		this.loggerController.log("wifi enabled");
        			
        	}else{
        		this.loggerController.log("wifi not enabled");
        	}
        
        //register broadcast receiver to listen to network events
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, wifiReceiver.getIntentFilter());
        
        
        
        //setup wifi access point config(dmk box)
        config = new WifiConfiguration();
        config.SSID = Constants.SSID;
        config.hiddenSSID = false;
        //config.BSSID = LoggerConstants.BSSID;
        //String [] wepKeys = {"0123456789"};
        //config.wepKeys = wepKeys;
        //config.wepTxKeyIndex = 0;
        //config.preSharedKey = LoggerConstants.PRESHAREDKEY;
        config.priority = 1;
        config.status = WifiConfiguration.Status.ENABLED;  
        //config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        //config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        //boolean configExists = false;
        for(int i=0; i<configs.size(); i++)
        {
        	if(configs.get(i).SSID.compareTo(config.SSID) == 0)
        	{
        		//configExists = true;
        		configs.remove(i);
        	}
        }
        //if(!configExists)
        //{
        	wifiNetId = wifiManager.addNetwork(config);
        //}
        
        boolean enabled = wifiManager.enableNetwork(wifiNetId, true);
        if(enabled)
        {
        	//config.networkId = wifiNetId;
        	wifiManager.saveConfiguration();
        }
        loggerController.log("wifiManager.enableNetwork(dmk config) returned "+enabled);
       
    }
    
    //this class runs in the main activity(UI) thread, 
    //as defined in the BroadcastReceiver
    private class WifiReceiver extends BroadcastReceiver
    {
    	private IntentFilter intentFilter;
    	
    	WifiReceiver()
    	{
    		super();
    		
    		intentFilter = new IntentFilter();
    		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
    	}
    	
    	public IntentFilter getIntentFilter()
    	{
    		return intentFilter;
    	}
        
        
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
			{
				SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				loggerController.log("supplicant state changed to "+state.name());
				
				if(state.compareTo(SupplicantState.SCANNING) == 0)
				{
					//wifiManager.reassociate();
					//wifiManager.reconnect();
					//wifiManager.disconnect();
				}
				else if(state.compareTo(SupplicantState.INACTIVE) == 0)
				{
					wifiManager.reconnect();
				}
			}
			else if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
			{
				loggerController.log("WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION...");
				
				if(intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
				{
					//get conn info
					WifiInfo info = wifiManager.getConnectionInfo();
					final int IP = info.getIpAddress();
					loggerController.log("wifi my IP="+IP);
					
					//if(IP >= 1921681102 && IP <= 1921681104)
					if(true)
					{
						
						loggerController.log("Wifi is enabled, connecting to socket");
						//connect to socket
						try 
						{
							socket = new DatagramSocket(new InetSocketAddress("0.0.0.0", 1703));//new InetSocketAddress("0.0.0.0", 1703));/
							byte [] test = new byte[]{'h','e','l','l','o'};
							socket.send(new DatagramPacket(test,5));
							socket.connect(new InetSocketAddress("0.0.0.0", 1703));
							if(socket.isConnected())
							{
								loggerController.log("socket is connected to localport="+socket.getLocalPort()+" port="+socket.getPort());	
							}else
							{
								loggerController.log("socket is not connected");	
							}
								
						} 
						catch (Exception e) {
							// TODO Auto-generated catch block
							
							e.printStackTrace();
							final String msg = e.getMessage();
							loggerController.log("error setting up socket"+msg);	
						}
					}
				}
				
				
			}
			else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			{
				wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
				loggerController.log("WifiReceiver.onReceive WifiManager.WIFI_STATE_CHANGED_ACTION wifiState="+wifiState);
				switch(wifiState)
				{
					case WifiManager.WIFI_STATE_DISABLED:
					case WifiManager.WIFI_STATE_DISABLING:
						loggerController.log("system says wifi is disabling/disabled, stop listening to port");
						doLogging = false;
						break;
					case WifiManager.WIFI_STATE_ENABLED:
						loggerController.log("wifi is enabled");
						break;
					case WifiManager.WIFI_STATE_ENABLING:
						loggerController.log("wifi is enabling");
						break;
					case WifiManager.WIFI_STATE_UNKNOWN:
						break;
				}
			}
			else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			{
				List<ScanResult> results = wifiManager.getScanResults();
				loggerController.log("scan results returned, got "+results.size()+" configs. connected to="+wifiManager.getConnectionInfo().getSSID()+" state="+wifiManager.getConnectionInfo().getSupplicantState().name());
				for(int i=0; i<results.size(); i++)
				{
					loggerController.log("...got BSSID="+results.get(i).BSSID+" SSID="+results.get(i).SSID);
					if(results.get(i).SSID.equals(Constants.SSID))
					{
						config.BSSID = results.get(i).BSSID;
						
						//wifiManager.enableNetwork(wifiNetId, true);
						loggerController.log("attempt something to connect to DMK?");
					}
				}
			}
			else if(action.equals(WifiManager.EXTRA_SUPPLICANT_ERROR))
			{
				int err = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
				loggerController.log("EXTRA_SUPPLICANT_ERROR="+err+" (1==authentication error)");
			}
			else
			{
				loggerController.log("got an unsubscribed action="+action);
			}
			
		}
    }
}

