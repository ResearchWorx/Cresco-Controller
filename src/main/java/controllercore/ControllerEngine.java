package controllercore;

import graphdb.GraphDBEngine;
import httpserv.httpServerEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import controllercore.PeerDiscovery.Peer;

public class ControllerEngine {

	public static CommandExec commandExec;
	public static GraphDBEngine gdb;
	
	public static void main(String[] args) throws Exception 
	{
		commandExec = new CommandExec(); //create command channel
		
		gdb = new GraphDBEngine(); //create graphdb connector
		
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
    		System.out.println("Starting HTTP Service");
			httpServerEngine httpEngine = new httpServerEngine();
			Thread httpServerThread = new Thread(httpEngine);
	    	httpServerThread.start();		    
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
