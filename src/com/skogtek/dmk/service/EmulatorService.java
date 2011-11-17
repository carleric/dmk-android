package com.skogtek.dmk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;

import com.skogtek.dmk.ui.Controller;

public class EmulatorService extends Service
{
    private ServiceThread serviceThread;
    private boolean doSend = false;
    private Controller controller;
    private final ServiceBinder serviceBinder = new ServiceBinder();
    private static String LOG_ID = "ServiceEmulator";    

    @Override
    public void onCreate() 
    {
    	serviceThread = new ServiceThread();
    	serviceThread.start();
    }
    
    public void stopSending()
    {
    	doSend = false;
    }
    
    public int startSending()
    {
    	new Thread(new Runnable()
    	{
    		public void run()
    		{
    			
    			
    		}
    	}).start();
		
		return START_STICKY;
    }

    @Override
    public void onDestroy() 
    {
    	controller.log("LoggerService.onDestroy, stopping thread and destroying socket");
    	
    	serviceThread.stopRunning();
    }
    
    public class ServiceBinder extends Binder 
    {
    	EmulatorService getService() 
        {
            return EmulatorService.this;
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
    
    private class ServiceThread extends Thread
    {
    	private boolean run = true; 
    	
    	public void run()
    	{
    		// run is true by default, until stopThread() is called.
    		while(run)
    		{
    			//...spin here until doSend is set to true
        		while(doSend)
        		{
        			//receive data
        			try 
        			{
						//do something
        				
					} 
        			catch (Exception e) 
					{
						controller.log("error in read operation: "+ e.getMessage());
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
}

