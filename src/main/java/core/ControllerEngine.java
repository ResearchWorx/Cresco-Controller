package core;

import graphdb.GraphDBEngine;
import httpserv.httpServerEngineDownloads;
import httpserv.httpServerEngineExternal;
import httpserv.httpServerEngineInternal;
import httpserv.httpServerEnginePerf;
import scheduler.SchedulerEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.MsgEvent;
import core.PeerDiscovery.Peer;

public class ControllerEngine {

	public static ConcurrentHashMap<String,ConcurrentLinkedQueue<MsgEvent>> regionalMsgMap;
	public static CommandExec commandExec;
	public static GraphDBEngine gdb;
	public static boolean SchedulerActive = false;
	public static SchedulerEngine se;
	public static ConcurrentLinkedQueue<MsgEvent> resourceScheduleQueue;
	
	public static Config config;
	
	public static void main(String[] args) throws Exception 
	{
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
			
			String configFile = checkConfig(args);
        	
			//Make sure config file
        	config = new Config(configFile);
    		
        	commandExec = new CommandExec(); //create command channel
    		
    		gdb = new GraphDBEngine(); //create graphdb connector
    		
    		regionalMsgMap = new ConcurrentHashMap<String,ConcurrentLinkedQueue<MsgEvent>>();
    		
    		resourceScheduleQueue = new ConcurrentLinkedQueue<MsgEvent>();
    		
			
			/*
    		System.out.println("Starting Broker");
    		BrokerEngine be = new BrokerEngine();
    		*/
			System.out.println("Starting SR Scheduler");
			se = new SchedulerEngine();
			Thread se_thread = new Thread(se);
	    	se_thread.start();
			
	    	while(!ControllerEngine.SchedulerActive)
	    	{
	    		System.out.println("Waiting on SchedulerActive...");
	    		Thread.sleep(1000);;
	    	}
			
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
	
	public static String checkConfig(String[] args)
	{
		String errorMgs = "Cresco-Controller\n" +
    			"Usage: java -jar Cresco-Controller.jar" +
    			" -f <configuration_file>\n";
    			
    	if (args.length != 2)
    	{
    	  System.err.println(errorMgs);
    	  System.err.println("ERROR: Invalid number of arguements.");
      	  System.exit(1);
    	}
    	else if(!args[0].equals("-f"))
    	{
    	  System.err.println(errorMgs);
    	  System.err.println("ERROR: Must specify configuration file.");
      	  System.exit(1);
    	}
    	else
    	{
    		File f = new File(args[1]);
    		if(!f.exists())
    		{
    			System.err.println("The specified configuration file: " + args[1] + " is invalid");
    			System.exit(1);	
    		}
    	}
    return args[1];	
	}
   
	
}
