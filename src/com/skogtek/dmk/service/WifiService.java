package com.skogtek.dmk.service;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import android.util.Log;

import com.skogtek.dmk.Constants;
import com.skogtek.dmk.ui.Prefs;

public class WifiService extends DMKService
{
    private ServiceThread serviceThread;
    private boolean go = false;
        
    private DatagramPacket packet;
    private DatagramSocket socket;
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;   

    @Override
    public void onCreate() 
    {
    	Log.e(Constants.LOG_ID, "ServiceThread.onCreate");
    	
    	super.onCreate();
    	
    	serviceThread = new ServiceThread();
        serviceThread.start();
        
        start();
    }
    
    @Override
    public void onDestroy() 
    {
    	broadcastMessage("Service.onDestroy, stopping thread and destroying socket");
    	
    	serviceThread.stopRunning();
    	
    	if(socket != null)
    	{
	    	socket.disconnect();
	    	socket.close();
	    	socket = null;
    	}
    	
        super.onDestroy();
    }
    
    public void start()
    {
    	broadcastMessage("connecting to socket...");
		
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(socket.isConnected())
		{
			broadcastMessage("socket is connected to local_port="+socket.getLocalPort()+" remote_address="+socket.getRemoteSocketAddress().toString());
			go = true;
		}else
		{
			broadcastMessage("socket failed to connect");	
		}	
    }

    public void stop()
    {
    	go = false;
    	
    	if(socket != null)
    	{
	    	socket.disconnect();
	    	socket.close();
	    	socket = null;
	    	//buffer = null;
	    	packet = null;
    	}
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
        		while(go)
        		{
        			//receive data
        			try 
        			{
						socket.receive(packet);
					
        				String msg = new String(packet.getData(), 0, packet.getLength());
        				
        				//just output to screen, don't do any parsing in this app
        				broadcastMessage(msg);
        				
					} 
        			catch (InterruptedIOException ior)
        			{
        				broadcastMessage("socket.receive timeout");
        			}
        			
        			catch (Exception e) 
					{
						//Log.e(LoggerConstants.LOG_ID, "error receiving from socket "+e.getMessage());
        				broadcastMessage("error in read operation: "+ e.getMessage());
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

