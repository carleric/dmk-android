package com.skogtek.dmk.service;

import java.io.IOException;

import com.skogtek.dmk.db.EmuDataHelper;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class EmulatorService extends DMKService
{
    private ServiceThread serviceThread;
    private boolean go = false;
    private static String LOG_ID = "EmulatorService";   
    EmuDataHelper emuDataHelper;

    @Override
    public void onCreate() 
    {
    	super.onCreate();
    	
    	serviceThread = new ServiceThread();
    	serviceThread.start();
    }
    
    protected void stop()
    {
    	go = false;
    }
    
    protected void start()
    {
    	//connect to db
    	emuDataHelper = new EmuDataHelper(this);
 
	    try {
	    	emuDataHelper.createDataBase();
	 	} catch (IOException ioe) {
	 		throw new Error("Unable to create database");
	 	}
	 
	 	try {
	 		emuDataHelper.openDataBase();
	 	}catch(SQLException sqle){
	 		broadcastMessage("error opening db"+ sqle.getMessage());
	 	}
	 	
	 	go = true;
	 	
    	broadcastMessage("connected to db:");
    	
    }

    @Override
    public void onDestroy() 
    {
    	serviceThread.stopRunning();
    	super.onDestroy();
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
        		if(go)
        		{
        			try 
        			{
        				Cursor cursor = emuDataHelper.rawQuery("select zdata from zpacket", null);
        				while (cursor != null && !cursor.isLast())
        				{
	        				String s = new String(cursor.getBlob(0));
	        				broadcastMessage(s);
	        				cursor.moveToNext(); 
        				}
        				cursor.close();
        				emuDataHelper.close();
					} 
        			catch (Exception e) 
					{
						broadcastMessage(e.getMessage());
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

