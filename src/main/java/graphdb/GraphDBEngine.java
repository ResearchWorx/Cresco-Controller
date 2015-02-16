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
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
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
			/*
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
			        dbCheck() )
			        .setConfig( GraphDatabaseSettings.read_only, "true" )
			        .newGraphDatabase();
			
			*/
			/*
			graphDb = new GraphDatabaseFactory()
		    .newEmbeddedDatabaseBuilder( dbCheck() )
		    .setConfig( GraphDatabaseSettings.nodestore_mapped_memory_size, "10M" )
		    .setConfig( GraphDatabaseSettings.allow_store_upgrade,"true" )
		    .setConfig( GraphDatabaseSettings.string_block_size, "60" )
		    .setConfig( GraphDatabaseSettings.array_block_size, "300" )
		    .newGraphDatabase();
			*/
		
			engine = new RestCypherQueryEngine(new RestAPIFacade("http://localhost:7474/db/data"));
			//results = engine.query(cypher, null)
					
			//engine = new ExecutionEngine(graphDb,StringLogger.SYSTEM);
			
			/*
			new WrappingNeoServerBootstrapper((GraphDatabaseAPI)graphDb).start();
			*/
			
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

			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Plugin:Plugin) ASSERT Plugin.pluginname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			
			try ( Transaction tx = graphDb.beginTx() )
			{
			String query = "CREATE CONSTRAINT ON (Application:Application) ASSERT Application.applicationname IS UNIQUE;";
			QueryResult<Map<String, Object>> result = engine.query(query, null);
			tx.success();
			}
			
			
	}
	
	public long addAppNode(String application)
	{
		long nodeId = -1l;
			
				nodeId = getAppNodeId(application);
				if(nodeId == -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
					{
						Node aNode = graphDb.createNode( applicationLabel );
						aNode.setProperty( "applicationname", application);
						nodeId = aNode.getId();
						appMap.put(application, nodeId);
						tx.success();
					}
				}
				else
				{
					System.out.println("Application Node Already Exist");
				}
		return nodeId;
		
	}
	
	public long getAppNodeId(String application)
	{
		//check cache
		if(appMap.containsKey(application))
		{
			return appMap.get(application);
		}
		
		QueryResult<Map<String, Object>> result;
		long nodeId = -1;
		int nodeCount = 0;
		
		try ( Transaction tx = graphDb.beginTx() )
		{
					String execStr = "MATCH (a:Application { applicationname:\"" + application + "\" })";
					execStr += "RETURN a";
					result = engine.query(execStr, null);
					
					//result = engine.execute( "match (n {regionname: '" + region + "'}) return n" );	
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					
					if(iterator.hasNext()) { 
					
					   Map<String,Object> row= iterator.next(); 
					   Node node  = (Node) row.get("a");
						
					   nodeCount++;
					   nodeId = node.getId();
					   appMap.put(application, nodeId);
					 }
				
			tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("Error : Checking Application=" + application + " " + ex.toString());
		}
		if(nodeCount > 1)
		{
			System.out.println("Error : duplicate nodes!");
			System.out.println("Could not add Application=" + application);
		}
		//System.out.println("getNodeId=" + nodeId);
		//System.out.println("NodeId : Region=" + region + " agent=" + agent + " plugin=" + plugin + " nodeId=" + nodeId);
		return nodeId;
	}

	public long addNode(String region, String agent, String plugin)
	{
		long nodeId = -1l;
		
			if((region != null) && (agent == null) && (plugin == null)) //region node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId == -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
					{
						Node aNode = graphDb.createNode( regionLabel );
						aNode.setProperty( "regionname", region);
						
						nodeId = aNode.getId();
						tx.success();
					}
				}
				
			}
			else if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId == -1)
				{
					regionNodeId = addNode(region,null,null);
				}
				
				long agentNodeId = getNodeId(region,agent,null);
				if(agentNodeId == -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
					{
						Node aNode = graphDb.createNode( agentLabel );
						aNode.setProperty( "agentname", agent);
						nodeId = aNode.getId();
						//addEdge(regionNodeId,nodeId,RelType.isAgent);
						addEdge(nodeId,regionNodeId,RelType.isAgent);
						tx.success();
					}
				}
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId == -1)
				{
					regionNodeId = addNode(region,null,null);
				}
				
				long agentNodeId = getNodeId(region,agent,null);
				if(agentNodeId == -1)
				{
					agentNodeId = addNode(region,agent,null);
				}
				
				long pluginNodeId = getNodeId(region,agent,plugin);
				if(pluginNodeId == -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
					{
					Node aNode = graphDb.createNode( pluginLabel );
					aNode.setProperty( "pluginname", plugin);
					nodeId = aNode.getId();
					addEdge(nodeId,agentNodeId,RelType.isPlugin);
					tx.success();
					}
				}
				
			}
			
		return nodeId;
		
	}
	
	public Boolean isNode(String region, String agent, String plugin)
	{
		String nodeHash = region + "," + agent + "," + plugin;
		if(nodeMap.containsKey(nodeHash))
		{
			return true;
		}
		
		long nodeId = -1;
		try ( Transaction tx = graphDb.beginTx() )
		{
			nodeId = getNodeId(region, agent, plugin);
			tx.success();
		}
		if(nodeId != -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public long getNodeId2(String region, String agent, String plugin)
	{
		QueryResult<Map<String, Object>> result;
		long nodeId = -1;
		int nodeCount = 0;
		
		try ( Transaction tx = graphDb.beginTx() )
		{
			if((region != null) && (agent == null) && (plugin == null)) //region node
			{
				
					String execStr = "MATCH (r:Region { regionname:\"" + region + "\" })";
					execStr += "RETURN r";
					result = engine.query(execStr, null);
					
					//result = engine.execute( "match (n {regionname: '" + region + "'}) return n" );	
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					
					if(iterator.hasNext()) { 
					
					   Map<String,Object> row= iterator.next(); 
					   Node node  = (Node) row.get("r");
						
					   nodeCount++;
					   nodeId = node.getId();
					 }
				
			}
			else if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
					String execStr = "MATCH (r:Region { regionname:\"" + region + "\" })<-[:isAgent]-(Agent)";
					execStr += "WHERE Agent.agentname = \"" + agent + "\"";
					execStr += "RETURN Agent";
					result = engine.query( execStr,null );
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					 if(iterator.hasNext()) { 
					   Map<String,Object> row= iterator.next(); 
					   //out.print("Total nodes: " + row.get("total"));
					   Node node  = (Node) row.get("Agent");
					   nodeCount++;
					   nodeId = node.getId();
					 }
				
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				long agentNodeId = getNodeId(region, agent, null); //getting the agentNodeId
				if(agentNodeId != -1)
				{
						String execStr = "match (Agent)<-[:isPlugin]-(Plugin { pluginname:\"" + plugin + "\" })"; 
						execStr += "where id(Agent) = " + agentNodeId + " "; 
						execStr += "RETURN Plugin";
						result = engine.query( execStr,null);
						   
						Iterator<Map<String, Object>> iterator=result.iterator(); 
						 if(iterator.hasNext()) { 
							   
						   Map<String,Object> row= iterator.next(); 
						   //out.print("Total nodes: " + row.get("total"));
						   Node node  = (Node) row.get("Plugin");
						   nodeCount++;
						   nodeId = node.getId();
						 }
					
				}
			}
			tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("Error : Checking Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + ex.toString());
		}
		if(nodeCount > 1)
		{
			System.out.println("Error : duplicate nodes!");
			System.out.println("Could not add Region=" + region + " Agent=" + agent + " Plugin=" + plugin);
		}
		//System.out.println("getNodeId=" + nodeId);
		//System.out.println("NodeId : Region=" + region + " agent=" + agent + " plugin=" + plugin + " nodeId=" + nodeId);
		return nodeId;
	}

	public long getNodeId(String region, String agent, String plugin)
	{
		String nodeHash = region + "," + agent + "," + plugin;
		if(nodeMap.containsKey(nodeHash))
		{
			return nodeMap.get(nodeHash);
		}
		
		QueryResult<Map<String, Object>> result;
		long nodeId = -1;
		int nodeCount = 0;
		
		
			if((region != null) && (agent == null) && (plugin == null)) //region node
			{
				try ( Transaction tx = graphDb.beginTx() )
				{
					String execStr = "MATCH (r:Region { regionname:\"" + region + "\" })";
					execStr += "RETURN r";
					result = engine.query(execStr, null);
					
					//result = engine.execute( "match (n {regionname: '" + region + "'}) return n" );	
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					
					if(iterator.hasNext()) { 
					
					   Map<String,Object> row= iterator.next(); 
					   Node node  = (Node) row.get("r");
						
					   nodeCount++;
					   nodeId = node.getId();
					   nodeMap.put(nodeHash, nodeId);
					 }
					tx.success();
					return nodeId;
				}
				
				
			}
			else if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
				try ( Transaction tx = graphDb.beginTx() )
				{
					String execStr = "MATCH (r:Region { regionname:\"" + region + "\" })<-[:isAgent]-(Agent)";
					execStr += "WHERE Agent.agentname = \"" + agent + "\"";
					execStr += "RETURN Agent";
					result = engine.query( execStr,null );
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					 if(iterator.hasNext()) { 
					   Map<String,Object> row= iterator.next(); 
					   //out.print("Total nodes: " + row.get("total"));
					   Node node  = (Node) row.get("Agent");
					   nodeCount++;
					   nodeId = node.getId();
					   nodeMap.put(nodeHash, nodeId);
					 }
					 tx.success();
					 return nodeId;
				}
			}
			/*
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				long agentId = -1;
				try ( Transaction tx = graphDb.beginTx() )
				{
					String execStr = "MATCH (r:Region { regionname:\"" + region + "\" })<-[:isAgent]-(Agent)";
					execStr += "WHERE Agent.agentname = \"" + agent + "\"";
					execStr += "RETURN Agent";
					result = engine.query( execStr,null );
					Iterator<Map<String, Object>> iterator=result.iterator(); 
					 if(iterator.hasNext()) { 
					   Map<String,Object> row= iterator.next(); 
					   //out.print("Total nodes: " + row.get("total"));
					   Node node  = (Node) row.get("Agent");
					   nodeCount++;
					   nodeId = node.getId();
					 }
					 agentId = nodeId;
					 tx.success();
				}
				try ( Transaction tx = graphDb.beginTx() )
				{
					 if(agentId != -1)
					 {
						 String execStr = "match (Agent)<-[:isPlugin]-(Plugin { pluginname:\"" + plugin + "\" })"; 
							execStr += "where id(Agent) = " + agentId + " "; 
							execStr += "RETURN Plugin";
							result = engine.query( execStr,null);
							   
							Iterator<Map<String, Object>> iterator1=result.iterator(); 
							 if(iterator1.hasNext()) { 
								   
							   Map<String,Object> row= iterator1.next(); 
							   //out.print("Total nodes: " + row.get("total"));
							   Node node  = (Node) row.get("Plugin");
							   nodeCount++;
							   nodeId = node.getId();
							 }
					 }
					 tx.success();
					 return nodeId;
				}
				*/
				
				long agentNodeId = getNodeId(region, agent, null); //getting the agentNodeId
				try ( Transaction tx = graphDb.beginTx() )
				{
					if(agentNodeId != -1)
					{
						String execStr = "match (Agent)<-[:isPlugin]-(Plugin { pluginname:\"" + plugin + "\" })"; 
						execStr += "where id(Agent) = " + agentNodeId + " "; 
						execStr += "RETURN Plugin";
						result = engine.query( execStr,null);
						   
						Iterator<Map<String, Object>> iterator=result.iterator(); 
						 if(iterator.hasNext()) { 
							   
						   Map<String,Object> row= iterator.next(); 
						   //out.print("Total nodes: " + row.get("total"));
						   Node node  = (Node) row.get("Plugin");
						   nodeCount++;
						   nodeId = node.getId();
						   nodeMap.put(nodeHash, nodeId);
						 }
					
					}
					tx.success();
					return nodeId;
				}
			
		
	/*
		catch(Exception ex)
		{
			System.out.println("Error : Checking Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + ex.toString());
		}
		*/
		/*		
		if(nodeCount > 1)
		{
			System.out.println("Error : duplicate nodes!");
			System.out.println("Could not add Region=" + region + " Agent=" + agent + " Plugin=" + plugin);
		}
		*/
		//System.out.println("getNodeId=" + nodeId);
		//System.out.println("NodeId : Region=" + region + " agent=" + agent + " plugin=" + plugin + " nodeId=" + nodeId);
		//return nodeId;
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
			nodeId = addNode(region,agent,plugin);
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
	
		nodeMap.clear(); //clear cache on removal of anything
		try ( Transaction tx = graphDb.beginTx() )
		{
		String query = "START n=node(" + nodeId + ") OPTIONAL MATCH n-[r]-() DELETE r, n;";
		QueryResult<Map<String, Object>> result = engine.query(query, null);
		tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("Unable to delete nodes and relations ! " + ex.toString());
		}
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
