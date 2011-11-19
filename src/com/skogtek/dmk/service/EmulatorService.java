package com.skogtek.dmk.service;


public class EmulatorService extends DMKService
{
    private ServiceThread serviceThread;
    private boolean doSend = false;
    private static String LOG_ID = "EmulatorService";   
    

    @Override
    public void onCreate() 
    {
    	super.onCreate();
    	
    	serviceThread = new ServiceThread();
    	serviceThread.start();
    }
    
    protected void stop()
    {
    	doSend = false;
    }
    
    protected void start()
    {
    	new Thread(new Runnable()
    	{
    		public void run()
    		{
    			//do things that must be done once on start
    			
    			//open emulation database
    			
    		}
    	}).start();
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
        		while(doSend)
        		{
        			//receive data
        			try 
        			{
						//do something
        				broadcastMessage(".");
        				
					} 
        			catch (Exception e) 
					{
						//controller.log("error in read operation: "+ e.getMessage());
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

