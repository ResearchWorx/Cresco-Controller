package graphdb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GraphDBEngine {

	private static GraphDatabaseService graphDb;
	private static RestCypherQueryEngine engine;

	private static HashMap<String,Long> appMap;
	private static HashMap<String,Long> nodeMap;
	private static HashMap<String,Long> edgeMap;
	private static Label regionLabel;
	private static Label agentLabel;
	private static Label pluginLabel;
	private static Label applicationLabel;
	private final Node lockNode;
	
	public GraphDBEngine()
	{
		
			appMap = new HashMap<String,Long>();
			nodeMap = new HashMap<String,Long>();
			edgeMap = new HashMap<String,Long>();
			
			try
			{
				graphDb = new RestGraphDatabase("http://localhost:7474/db/data");
				

			}
			catch(Exception ex)
			{
				System.out.println("Could not init REST DB");
			}
		
			engine = new RestCypherQueryEngine(new RestAPIFacade("http://localhost:7474/db/data"));
			
			try ( Transaction tx = graphDb.beginTx() )
			{
			regionLabel = DynamicLabel.label( "Region" );
			agentLabel = DynamicLabel.label( "Agent" );
	        pluginLabel = DynamicLabel.label( "Plugin" );
	        applicationLabel = DynamicLabel.label( "Application" );
	        tx.success();
			}
			
			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Region:Region) ASSERT Region.regionname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			
			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Agent:Agent) ASSERT Agent.agentname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			/*
			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Plugin:Plugin) ASSERT Plugin.pluginname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			//DROP CONSTRAINT ON (Plugin:Plugin) ASSERT Plugin.pluginname IS UNIQUE
			*/		
			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Application:Application) ASSERT Application.applicationname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			//get lock node
			try ( Transaction tx = graphDb.beginTx() )
			{
			    lockNode = graphDb.createNode();
			    tx.success();
			    //return lockNode;
			}
			
	}
	
	
	public long addAppNode(String application)
	{
			try ( Transaction tx = graphDb.beginTx() )
			{
				Index<Node> usersIndex = graphDb.index().forNodes( "application" );
				Node userNode = usersIndex.get( "applicationname", application ).getSingle();
				if ( userNode != null )
				{
					return userNode.getId();
				}
		
				tx.acquireWriteLock( lockNode );
				userNode = usersIndex.get( "applicationname", application ).getSingle();
				if ( userNode == null )
				{
					System.out.println("Adding Application: " + application);
				
					userNode = graphDb.createNode( applicationLabel );
					usersIndex.add( userNode, "applicationname", application );
					userNode.setProperty( "applicationname", application);
				}
				tx.success();
				System.out.println("Application NodeId: " + userNode.getId());
				return userNode.getId();
			}
	}
	
	public long getAppNodeId(String application)
	{
		long nodeId = -1l;
		
				try ( Transaction tx = graphDb.beginTx() )
				{
					Index<Node> usersIndex = graphDb.index().forNodes( "application" );
					Node userNode = usersIndex.get( "applicationname", application ).getSingle();
					if ( userNode != null )
					{
						tx.success();
				        return userNode.getId();
					}
				    
				}
				
		return nodeId;
		
	}
		
	public long getNodeId(String region, String agent, String plugin)
	{
		long nodeId = -1l;
		String pathname = region + "," + agent + "," + plugin;
		
				try ( Transaction tx = graphDb.beginTx() )
				{
					Index<Node> usersIndex = graphDb.index().forNodes( "nodes" );
					Node userNode = usersIndex.get( "pathname", pathname ).getSingle();
					if ( userNode != null )
					{
						tx.success();
				        return userNode.getId();
					}
				    
				}
				
		return nodeId;
		
	}
	
	public long addNode(String region, String agent, String plugin)
	{
		long nodeId = -1l;
		String pathname = region + "," + agent + "," + plugin;
		
			if((region != null) && (agent == null) && (plugin == null)) //region node
			{
				try ( Transaction tx = graphDb.beginTx() )
				{
					Index<Node> usersIndex = graphDb.index().forNodes( "nodes" );
					Node userNode = usersIndex.get( "pathname", pathname ).getSingle();
					if ( userNode != null )
					{
				        return userNode.getId();
					}
				
					tx.acquireWriteLock( lockNode );
				    userNode = usersIndex.get( "pathname", pathname ).getSingle();
				    if ( userNode == null )
				    {
				    	System.out.println("Adding Region: " + region);
						
				        userNode = graphDb.createNode( regionLabel );
				        usersIndex.add( userNode, "pathname", pathname );
				        userNode.setProperty( "regionname", region);
				    }
				    tx.success();
				    System.out.println("Regional NodeId: " + userNode.getId());
				    return userNode.getId();
				}
			}
			else if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
				long regionNodeId = addNode(region,null,null);
				
				try ( Transaction tx = graphDb.beginTx() )
				{
					Index<Node> usersIndex = graphDb.index().forNodes( "nodes" );
					Node userNode = usersIndex.get( "pathname", pathname ).getSingle();
					if ( userNode != null )
					{
				        return userNode.getId();
					}
				
					tx.acquireWriteLock( lockNode );
				    userNode = usersIndex.get( "pathname", pathname ).getSingle();
				    if ( userNode == null )
				    {
				    	System.out.println("Adding Agent: " + agent);
						userNode = graphDb.createNode( agentLabel );
				        usersIndex.add( userNode, "pathname", pathname );
				        userNode.setProperty( "agentname", agent);
				    }
				    tx.success();
				    
					nodeId = userNode.getId();
					System.out.println("Regional NodeId: " + regionNodeId  + " agent NodeId: " + nodeId);
				    
				}
				System.out.println("Adding AgentConnections");
				addEdge(nodeId,regionNodeId,RelType.isAgent);
			    System.out.println("End AgentConnections");
				return nodeId;
					
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				
				long regionNodeId = addNode(region,null,null);
				long agentNodeId = addNode(region,agent,null);
				
				try ( Transaction tx = graphDb.beginTx() )
				{
					Index<Node> usersIndex = graphDb.index().forNodes( "nodes" );
					Node userNode = usersIndex.get( "pathname", pathname ).getSingle();
					if ( userNode != null )
					{
				        return userNode.getId();
					}
				///
					tx.acquireWriteLock( lockNode );
				    userNode = usersIndex.get( "pathname", pathname ).getSingle();
				    if ( userNode == null )
				    {
				    	System.out.println("GraphDBEngine : addNode() Adding Plugin: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);
						userNode = graphDb.createNode( pluginLabel );
				        usersIndex.add( userNode, "pathname", pathname );
				        userNode.setProperty( "pluginname", plugin);
				    }
				    tx.success();
				    
				    nodeId = userNode.getId();
				    System.out.println("Regional NodeId: " + regionNodeId  + " agent NodeId: " + agentNodeId + " pluginNodeId: " + nodeId);
				    
				    //return userNode.getId();
				}
				System.out.println("Adding Plugin Connections");
				addEdge(nodeId,agentNodeId,RelType.isPlugin);
				System.out.println("End Plugin Connections");
				return nodeId;
				
			}
			
		return nodeId;
		
	}

	
	
	public Map<String,String> getNodeParams(String region, String agent, String plugin)
	{
		Map<String,String> paramMap = new HashMap<String,String>();
		long nodeId = getNodeId(region,agent,plugin);
		if(nodeId != -1)
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				Node pluginNode = graphDb.getNodeById(nodeId);
				for(String propKey : pluginNode.getPropertyKeys())
				{
					paramMap.put(propKey, pluginNode.getProperty(propKey).toString());
					//System.out.println(propKey + " " + pluginNode.getProperty(propKey).toString());
				}
				tx.success();
			}
		}
		return paramMap;
	}
	
	public String getNodeParam(String region, String agent, String plugin, String param)
	{
		String nodeParam = null;
		long nodeId = getNodeId(region,agent,plugin);
		if(nodeId != -1)
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				Node pluginNode = graphDb.getNodeById(nodeId);
				nodeParam = pluginNode.getProperty(param).toString();
				tx.success();
			}
		}
		return param;
	}
	
	public void setNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		
		long nodeId = getNodeId(region,agent,plugin);
		if(nodeId != -1)
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				Node node = graphDb.getNodeById(nodeId);
				
				Iterator it = paramMap.entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pairs = (Map.Entry)it.next();
					//System.out.println(pairs.getKey() + " = " + pairs.getValue());
					//it.remove(); // avoids a ConcurrentModificationException
					node.setProperty( pairs.getKey().toString(), pairs.getValue().toString());
					//System.out.println("Setting:" + pairs.getKey().toString() + " - " + pairs.getValue().toString());
				}
				tx.success();
		    }
		}	
	}
	
	public void setNodeParam(String region, String agent, String plugin, String paramKey, String paramValue)
	{	
		long nodeId = getNodeId(region,agent,plugin);
		if(nodeId != -1)
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				Node node = graphDb.getNodeById(nodeId);
				node.setProperty(paramKey, paramValue);
				tx.success();
		    }
		}	
	}
	
	public boolean updatePerf(String region, String agent, String plugin, String application, Map<String,String> params)
	{
	 try
   	 { 
		long appNodeId = getAppNodeId(application);
		if(appNodeId == -1)
		{
			appNodeId = addAppNode(application);
		}
		long nodeId = getNodeId(region,agent,plugin);
		if(nodeId == -1)
		{
			
			//nodeId = addNode(region,agent,plugin);
			//CODY
			//System.out.println("GraphDBEngine : updatePerf + addNode() Adding Plugin: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);
			System.out.println("GraphDBEngine : updatePerf : Tried to updatePerf before Node was created:" + region + " Agent:" + agent + " Plugin:" + plugin);	
			return false;
		}
		long relId = getEdgeId(nodeId,appNodeId,RelType.isConnected);
		if(relId == -1)
		{
			relId = addEdge(nodeId,appNodeId,RelType.isConnected);
		}
		
		Relationship relationship = graphDb.getRelationshipById(relId);
		for (Map.Entry<String, String> entry : params.entrySet())
		{
		    relationship.setProperty(entry.getKey(), entry.getValue());
		}
		return true;
	}
	catch(Exception ex)
	{
		System.out.println("Controller : GraphDBEngine : Failed to updatePerf");
		return false;
	}

	}
	
	public long getEdgeId(long nodeSource, long nodeDest, RelType type)
	{
		long relId = -1;
		QueryResult<Map<String, Object>> result;
		
		try ( Transaction tx = graphDb.beginTx() )
		{
			//first remove agents
			String execStr = "start x  = node("+ nodeSource + "), n = node(" + nodeDest + ")";
					execStr += " match x-[r]->n";
					execStr += " where type(r) = \"" + type.toString() + "\"";
					//execStr += "return ID(r), TYPE(r)";
					execStr += " return r";
			result = engine.query( execStr,null );
			Iterator<Map<String, Object>> iterator=result.iterator(); 
			 if(iterator.hasNext()) { 
			   Map<String,Object> row= iterator.next(); 
			   //out.print("Total nodes: " + row.get("total"));
			   Relationship relationship  = (Relationship) row.get("r");
			   try{
				   return relationship.getId();
			   }
			   catch(Exception ex)
			   {
				   System.out.println("WTF! " + ex.toString());
			   }
			 }
		tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("removeNode : removing Region " + ex.toString());
		}
		
		return relId;
	}
	
	public long addEdge(long nodeSource, long nodeDest, RelType type)
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			Node nodeFrom = graphDb.getNodeById(nodeSource);
			Node nodeTo = graphDb.getNodeById(nodeDest);
			//Relationship relationship = nodeFrom.createRelationshipTo(nodeTo, RelType.valueOf(reltype));
			Relationship relationship = nodeFrom.createRelationshipTo(nodeTo, type);
			long relationId = relationship.getId();
			//edgeMap.put(nodeSource + "," + nodeDest + "," + reltype, relationId);
			tx.success();
			return relationId;
		}
		
	}
	
	public void removeNode(String region, String agent, String plugin)
	{
		//clear cache on removal of anything
		nodeMap.clear();
		
		if((region != null) && (agent == null) && (plugin == null)) //region node
		{
			QueryResult<Map<String, Object>> result;
			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<String> nodeNames = new ArrayList<String>();
			
			try ( Transaction tx = graphDb.beginTx() )
			{
				//first remove agents
				String execStr = "MATCH (r:Region {regionname: \"" + region + "\"})-[l]-(b:Agent) ";
				execStr += "RETURN b";
				result = engine.query( execStr,null );
				Iterator<Map<String, Object>> iterator=result.iterator(); 
				 if(iterator.hasNext()) { 
				   Map<String,Object> row= iterator.next(); 
				   //out.print("Total nodes: " + row.get("total"));
				   Node node  = (Node) row.get("b");
				   try{
				   nodeNames.add(node.getProperty("agentname").toString());
				   }
				   catch(Exception ex)
				   {
					   System.out.println("WTF! " + ex.toString());
				   }
				 }
			tx.success();
			}
			catch(Exception ex)
			{
				System.out.println("removeNode : removing Region " + ex.toString());
			}
				
				for(String nodeName : nodeNames)
				{
					removeNode(region,nodeName,null);
				}
		
				long regionNodeId = getNodeId(region,null,null);
				//nodes.add(graphDb.getNodeById(regionNodeId));
				if(regionNodeId != -1)
				{
					deleteNodesAndRelationships(regionNodeId);
				}
		}
		else if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			QueryResult<Map<String, Object>> result;
			//ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<String> pluginNames = new ArrayList<String>();
			 			
			try ( Transaction tx = graphDb.beginTx() )
			{
				//first remove plugins
				String execStr = "MATCH (a:Agent {agentname: \"" + agent + "\"})-[r]-(b:Plugin) ";
				execStr += "RETURN b";
				result = engine.query( execStr,null );
				
				 Iterator<Map<String, Object>> iterator=result.iterator(); 
				 if(iterator.hasNext()) 
				 { 
					 for (Map<String,Object> row : result) 
					 {
						   Node x = (Node)row.get("b");
						   //for (String prop : x.getPropertyKeys()) {
						   //   System.out.println(prop +": "+x.getProperty(prop));
						   //}
						   pluginNames.add(x.getProperty("pluginname").toString());
						   
						}
					/* 
				   Map<String,Object> row= iterator.next(); 
				   Node node  = (Node) row.get("b");
				   pluginNames.add(node.getProperty("pluginname").toString());
				   System.out.println("remove plugin2 " + region + " " + agent + " " + " " + node.getProperty("pluginname").toString());
				   */				
				 }
				 tx.success();
			}	
			catch(Exception ex)
			{
				System.out.println("Woops: " + ex.toString());
			}
			for(String pluginName : pluginNames)
			{
				removeNode(region,agent,pluginName);
			}
			
			long agentNodeId = getNodeId(region,agent,null);
			deleteNodesAndRelationships(agentNodeId);
			
			//if no more nodes exist in region remove region
			ArrayList<String> nodeNames = new ArrayList<String>();
			
			try ( Transaction tx = graphDb.beginTx() )
			{
				//first remove agents
				String execStr = "MATCH (r:Region {regionname: \"" + region + "\"})-[l]-(b:Agent) ";
				execStr += "RETURN b";
				result = engine.query( execStr,null );
				Iterator<Map<String, Object>> iterator=result.iterator(); 
				 if(iterator.hasNext()) { 
				   Map<String,Object> row= iterator.next(); 
				   //out.print("Total nodes: " + row.get("total"));
				   Node node  = (Node) row.get("b");
				   try{
				   nodeNames.add(node.getProperty("agentname").toString());
				   }
				   catch(Exception ex)
				   {
					   System.out.println("WTF! " + ex.toString());
				   }
				 }
			tx.success();
			}
			catch(Exception ex)
			{
				System.out.println("removeNode : removing Region " + ex.toString());
			}
			if(nodeNames.isEmpty())
			{
				removeNode(region,null,null);
			}
			else
			{
				System.out.println(nodeNames.size());
			}
			
			
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
				//simply delete the plugin
				long pluginNodeId = getNodeId(region,agent,plugin);
				if(pluginNodeId != -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
					{
						deleteNodesAndRelationships(pluginNodeId);
					}
					catch(Exception ex)
					{
						System.out.println("Problem Removing Plugin " + ex.toString());
					}
				}
				else
				{
					System.out.println("Can't remove Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + " it does not exist");
				}
		}
		
	}
	
	
	private void deleteNodesAndRelationships(long nodeId) {
	
		boolean complete = false;
		int timeout = 0;
		while(!complete && (timeout < 5))
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				String query = "START n=node(" + nodeId + ") OPTIONAL MATCH n-[r]-() DELETE r, n;";
				QueryResult<Map<String, Object>> result = engine.query(query, null);
				tx.success();
				complete = true;
				return;
			}
			catch(Exception ex)
			{
				System.out.println("Unable to delete nodes and relations : Try "+ timeout + " : ! " + ex.toString());
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		System.out.println("Unable to delete nodes and relations : failure timeout ");
		
	}
	
	private static enum RelType implements RelationshipType
	{
	    isConnected,isPlugin,isAgent
	}
	
	private String dbCheck()
	{
		
		//System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
		//String dirPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().toString() + "graph.db";
		String dirPath = "/Users/cody/Documents/Mesh/Work/Development/Cresco/Cresco-Agent/cresco.db";
		File theDir = new File(dirPath);

		
		  // if the directory does not exist, create it
		  if (!theDir.exists()) {
		    System.out.println("creating graph.db directory: " + dirPath);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		     } catch(SecurityException se){
		        //handle it
		     }        
		     if(result) {    
		       System.out.println("graph.db directory created");  
		     }
		  }
		  else
		  {
			  System.out.println("graph.db directory found:" + dirPath); 
		  }
		return dirPath;
	}
}
