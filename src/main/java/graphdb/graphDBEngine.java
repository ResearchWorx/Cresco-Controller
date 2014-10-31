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

public class graphDBEngine {

	private static GraphDatabaseService graphDb;
	private static RestCypherQueryEngine engine;

	private static HashMap<String,Long> nodeMap;
	private static HashMap<String,Long> edgeMap;
	private static Label regionLabel;
	private static Label agentLabel;
	private static Label pluginLabel;
	
	
	public graphDBEngine()
	{
		
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
	        tx.success();
			}

			
	}
	
	
	@SuppressWarnings("unused")
	private Traverser getFriends(final Node person )       
	{
	    TraversalDescription td = graphDb.traversalDescription()
	            .breadthFirst()
	            .relationships( RelType.isConnected, Direction.BOTH )
	            .relationships( RelType.isAgent, Direction.BOTH )
	            .relationships( RelType.isPlugin, Direction.BOTH )
	            
	            .evaluator( Evaluators.excludeStartPosition() );
	    return td.traverse( person );
	}
	
	
	public void theBoom()
	{
		System.out.println("Starting the boom!");
		try ( Transaction tx = graphDb.beginTx() )
		{
			Node daNode = graphDb.getNodeById(getNodeId("test",null,null));
			
			
			Traverser traverser = getFriends(daNode);
			
			//Set<Node> nodes = new HashSet<Node>();
			//Set<Relationship> edges = new HashSet<Relationship>();

			Gson gson = new GsonBuilder().create();
			
			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<Relationship> edges = new ArrayList<Relationship>();
			
			
			for (Node n : traverser.nodes())
			{
			    nodes.add(n);
			    
			    System.out.println(n.toString());
			    System.out.println(gson.toJson(n));
			}

			for (Node node : nodes)
			{
			    for (Relationship rel : node.getRelationships())
			    {
			        if (nodes.contains(rel.getOtherNode(node)))
			        {
			            edges.add(rel);
			            System.out.println(rel.toString());
			        }
			    }
			}

			//String nodesJson = gson.toJson(nodes);
			//String edgesJson = gson.toJson(edges);
			//System.out.println(nodesJson);
			/*
			 	private MsgEvent meFromJson(String jsonMe)
				{
				Gson gson = new GsonBuilder().create();
        		MsgEvent me = gson.fromJson(jsonMe, MsgEvent.class);
        		//System.out.println(p);
        		return me;
				}
			 */
			
			//Gson gson = new GsonBuilder().create();
	        //MsgEvent me = gson.fromJson(jsonMe, MsgEvent.class);
	    
			System.out.println("end the boom!");
			
			tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("Error : " + ex.toString());
		}
		
	}
	
	public long getNodeId(String region, String agent, String plugin)
	{
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
					 }
					
					tx.success();
				}
				catch(Exception ex)
				{
					System.out.println("Error : Checking Region=" + region + " " + ex.toString());
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
					 }
					tx.success();
				}
				catch(Exception ex)
				{
					System.out.println("Error : Checking Region=" + region + " Agent=" + agent + " " + ex.toString());
				}
				
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				long agentNodeId = getNodeId(region, agent, null); //getting the agentNodeId
				if(agentNodeId != -1)
				{
					try ( Transaction tx = graphDb.beginTx() )
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
						tx.success();
					}
					catch(Exception ex)
					{
						System.out.println("Error : Checking Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + ex.toString());
					}
					
				}
			}
		if(nodeCount > 1)
		{
			System.out.println("Error : duplicate nodes!");
			System.out.println("Could not add Region=" + region + " Agent=" + agent + " Plugin=" + plugin);
		}
		//System.out.println("getNodeId=" + nodeId);
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
	
	public long addNode(String region, String agent, String plugin)
	{
		long nodeId = -1l;
		try ( Transaction tx = graphDb.beginTx() )
		{
			if((region != null) && (agent == null) && (plugin == null)) //region node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId == -1)
				{
					Node aNode = graphDb.createNode( regionLabel );
					aNode.setProperty( "regionname", region);
					nodeId = aNode.getId();
				}
				else
				{
					System.out.println("Region=" + region + " already exists");
				}
			}
			else if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId != -1)
				{
					long agentNodeId = getNodeId(region,agent,null);
					if(agentNodeId == -1)
					{
						Node aNode = graphDb.createNode( agentLabel );
						aNode.setProperty( "agentname", agent);
						nodeId = aNode.getId();
						//addEdge(regionNodeId,nodeId,RelType.isAgent);
						addEdge(nodeId,regionNodeId,RelType.isAgent);
						
					}
					else
					{
						System.out.println("Region=" + region + " Agent=" + agent + " already exists");
					}
				}
				else
				{
					System.out.println("Region=" + region + "  Does not Exist!");
				}
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				long regionNodeId = getNodeId(region,null,null);
				if(regionNodeId != -1)
				{
					long agentNodeId = getNodeId(region,agent,null);
					if(agentNodeId != -1)
					{					
						long pluginNodeId = getNodeId(region,agent,plugin);
						if(pluginNodeId == -1)
						{
							//System.out.println("we should add plugin here!");
							
							Node aNode = graphDb.createNode( pluginLabel );
							aNode.setProperty( "pluginname", plugin);
							nodeId = aNode.getId();
							//addEdge(agentNodeId,nodeId,RelType.isPlugin);
							addEdge(nodeId,agentNodeId,RelType.isPlugin);
							
						}
					}
					else
					{
						System.out.println("Region=" + region + " Agent=" + agent + " Does not Exist!");
					}
				}
				else
				{
					System.out.println("Region=" + region + "  Does not Exist!");
				}
			}
			tx.success();
		}
		catch(Exception ex)
		{
			System.out.println("Error : addNode :" + ex.toString());
		}
		if(nodeId == -1)
		{
			System.out.println("Could not add Region=" + region + " Agent=" + agent + " Plugin=" + plugin);
		}
		return nodeId;
		
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
				   nodeNames.add(node.getProperty("agentname").toString());
				 }
				
				tx.success();
				
				for(String nodeName : nodeNames)
				{
					removeNode(region,nodeName,null);
				}
				
				long regionNodeId = getNodeId(region,null,null);
				nodes.add(graphDb.getNodeById(regionNodeId));
				deleteNodes(nodes);
				
			}
			catch(Exception ex)
			{
				System.out.println("Error : Removing Region=" + region + " " + ex.toString());
			}
		}
		else if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			QueryResult<Map<String, Object>> result;
			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<String> nodeNames = new ArrayList<String>();
			
			try ( Transaction tx = graphDb.beginTx() )
			{
				//System.out.println("Start Removing Agent:" + agent);
				
				//first add plugins
				String execStr = "MATCH (a:Agent {agentname: \"" + agent + "\"})-[r]-(b:Plugin) ";
				execStr += "RETURN b";
				result = engine.query( execStr,null );
				
				Iterator<Map<String, Object>> iterator=result.iterator(); 
				 if(iterator.hasNext()) { 
				   Map<String,Object> row= iterator.next(); 
				   //out.print("Total nodes: " + row.get("total"));
				   Node node  = (Node) row.get("b");
				   nodeNames.add(node.getProperty("agentname").toString());
				 }
				
				tx.success();
				
				for(String nodeName : nodeNames)
				{
					removeNode(region,agent,nodeName);
				}
				//next add the agent
				long agentNodeId = getNodeId(region,agent,null);
				nodes.add(graphDb.getNodeById(agentNodeId));
				deleteNodesAndRelationships(nodes);			
				
			}
			catch(Exception ex)
			{
				System.out.println("Error : Removing Region=" + region + " Agent=" + agent + " " + ex.toString());
			}
			
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				//System.out.println("Start Removing Plugin:" + plugin);
				
				//simply delete the plugin
				long pluginNodeId = getNodeId(region,agent,plugin);
				if(pluginNodeId != -1)
				{
					ArrayList<Node> nodes = new ArrayList<Node>();
					nodes.add(graphDb.getNodeById(pluginNodeId));
					deleteNodesAndRelationships(nodes);
				}
				else
				{
					System.out.println("Can't remove Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + " it does not exist");
				}
				tx.success();
					 
			}
			catch(Exception ex)
			{
				System.out.println("Error : Removing Region=" + region + " Agent=" + agent + " Plugin=" + plugin + " " + ex.toString());
			}
		}
		
	}
	private void deleteNodesWithRelationships2(ArrayList nodes) {
		try ( Transaction tx = graphDb.beginTx() )
		{
		if (nodes == null || nodes.size() == 0) {
		return;
		} else {
		Map params = new HashMap();
		params.put("nodes", nodes);
		String query = "START n = node({nodes}) MATCH n-[r]-() DELETE n, r";
		QueryResult<Map<String, Object>> result = engine.query(query, params);
		
		}
		tx.success();
		}
	}
	private void deleteNodesAndRelationships(ArrayList nodes) {
		try ( Transaction tx = graphDb.beginTx() )
		{
		Map params = new HashMap();
		params.put("nodes", nodes);
		String query = "START n = node({nodes}) MATCH n-[r]-() DELETE n, r";
		QueryResult<Map<String, Object>> result = engine.query(query, params);
		tx.success();
		}
	}
	private void deleteNodes(ArrayList nodes) {
		try ( Transaction tx = graphDb.beginTx() )
		{
		Map params = new HashMap();
		params.put("nodes", nodes);
		String query = "START n = node({nodes}) MATCH n DELETE n";
		QueryResult<Map<String, Object>> result = engine.query(query, params);
		tx.success();
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
