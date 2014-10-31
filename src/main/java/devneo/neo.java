package devneo;

import graphdb.graphDBEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import devneo.PeerDiscovery.Peer;

public class neo {
public static GraphDatabaseService graphDb;
	
	public static void main(String[] args) throws Exception 
	    {
		
		graphDBEngine gdb = new graphDBEngine();
		
		//gdb.addNode("regionName", "agentName", "pluginName");
		gdb.addNode("regionName", null,null);
		gdb.addNode("regionName", "agentName",null);
		gdb.addNode("regionName", "agentName","pluginName");
		gdb.addNode("regionName", "agentName","pluginName2");
		
		gdb.addNode("regionName", "agentName2",null);
		gdb.addNode("regionName", "agentName","pluginName");
		
		
		//GraphDatabaseService gds = new RestGraphDatabase("http://localhost:7474/db/data");
		//GraphDatabaseService gds = new RestGraphDatabase("http://localhost:7474/db/data",username,password);
		
		
		
		//gdb.theBoom();
		
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
