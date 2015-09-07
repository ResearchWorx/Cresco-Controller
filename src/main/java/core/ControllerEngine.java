package core;

import graphdb.GraphDBEngine;
import httpserv.httpServerEngineDownloads;
import httpserv.httpServerEngineExternal;
import httpserv.httpServerEngineInternal;
import httpserv.httpServerEnginePerf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.MsgEvent;
import shared.MsgEventType;
import core.PeerDiscovery.Peer;

public class ControllerEngine {

	public static ConcurrentHashMap<String,ConcurrentLinkedQueue<MsgEvent>> regionalMsgMap;
	public static CommandExec commandExec;
	public static GraphDBEngine gdb;
	
	public static void main(String[] args) throws Exception 
	{
		commandExec = new CommandExec(); //create command channel
		
		gdb = new GraphDBEngine(); //create graphdb connector
		
		regionalMsgMap = new ConcurrentHashMap<String,ConcurrentLinkedQueue<MsgEvent>>();
		/*
		gdb.addNode("regionName", null,null);
		gdb.addNode("regionName", "agentName",null);
		gdb.addNode("regionName", "agentName","pluginName");
		gdb.addNode("regionName", "agentName","pluginName2");
		
		gdb.addNode("regionName", "agentName2",null);
		gdb.addNode("regionName", "agentName","pluginName");
		*/
		
		try
    	{
    		
			System.out.println("Starting HTTPInternal Service");
			httpServerEngineDownloads httpEngineDownloads = new httpServerEngineDownloads();
			Thread httpServerThreadDownloads = new Thread(httpEngineDownloads);
	    	httpServerThreadDownloads.start();
	    	
			System.out.println("Starting HTTPInternal Service");
			httpServerEngineInternal httpEngineInternal = new httpServerEngineInternal();
			Thread httpServerThreadInternal = new Thread(httpEngineInternal);
	    	httpServerThreadInternal.start();
	    	
	    	System.out.println("Starting HTTPInternal Service");
			httpServerEngineExternal httpEngineExternal = new httpServerEngineExternal();
			Thread httpServerThreadExternal = new Thread(httpEngineExternal);
	    	httpServerThreadExternal.start();
	    	
	    	System.out.println("Starting HTTPPerf Service");
			httpServerEnginePerf httpEnginePerf = new httpServerEnginePerf();
			Thread httpServerThreadPerf = new Thread(httpEnginePerf);
	    	httpServerThreadPerf.start();
	    	
	    	//
	    	/*
	    	System.out.println("adding msg for test2 and test3");
	    	ConcurrentLinkedQueue<MsgEvent> cmq = new ConcurrentLinkedQueue<MsgEvent>();
	    	MsgEvent me = new MsgEvent(MsgEventType.EXEC,"test2","controller2",null,"test message for test2");
	    	me.setParam("src_region", "test2");
			me.setParam("dst_region", "test2");
			me.setParam("dst_agent", "controller2");
			cmq.add(me);
			regionalMsgMap.put("test2", cmq);
	    	*/
	    	//
	    	
    	}
    	catch(Exception ex)
    	{
    		System.out.println("Unable to Start HTTP Service : " + ex.toString());
    	}
			
	}	
	
	public static void discover()
	{
	  try
	  {
	    int group = 6969;

	    PeerDiscovery mp = new PeerDiscovery( group, 6969 );

	    boolean stop = false;

	    BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );

	    while( !stop )
	    {
	      System.out.println( "enter \"q\" to quit, or anything else to query peers" );
	      String s = br.readLine();

	      if( s.equals( "q" ) )
	      {
	        System.out.print( "Closing down..." );
	        mp.disconnect();
	        System.out.println( " done" );
	        stop = true;
	      }
	      else
	      {
	        System.out.println( "Querying" );

	        Peer[] peers = mp.getPeers( 100, ( byte ) 0 );

	        System.out.println( peers.length + " peers found" );
	        for( Peer p : peers )
	        {
	          System.out.println( "\t" + p );
	        }
	      }
	    }
	  }
	  catch( Exception e )
	  {
	    e.printStackTrace();
	  }
	}
	
	
}
