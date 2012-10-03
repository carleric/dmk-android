package com.skogtek.dmk.service;

import android.database.Cursor;

import com.skogtek.dmk.db.EmuDataHelper;


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
    	
    	start();
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
	 		emuDataHelper.openDataBase();
	 	}catch(Exception sqle){
	 		throw new Error("Unable to creat & open database");
	 		//broadcastMessage("error opening db"+ sqle.getMessage());
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
    			//...spin here until go is set to true
        		while(go)
        		{
        			try 
        			{
        				Cursor cursor = emuDataHelper.rawQuery("select zdata from data", null);
        				cursor.moveToFirst();
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
        				go = false;
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

