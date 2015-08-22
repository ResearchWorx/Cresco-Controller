package graphdb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class newGraphDBEngine {

	private OrientGraphFactory factory;
	private OrientGraph odb;
	public Cache<String, String> pathCache;
	public Cache<String, Edge> appPathCache;
	
	
	public newGraphDBEngine()
	{
		pathCache = CacheBuilder.newBuilder()
			    .concurrencyLevel(4)
			    .softValues()
			    .maximumSize(100000)
			    .expireAfterWrite(15, TimeUnit.MINUTES)
			    .build();
		
		appPathCache = CacheBuilder.newBuilder()
			    .concurrencyLevel(4)
			    .softValues()
			    .maximumSize(100000)
			    .expireAfterWrite(15, TimeUnit.MINUTES)
			    .build();
		
		//String connection_string = "remote:" + Launcher.conf.getGraphDBServer() + "/" + Launcher.conf.getGraphDBName();
		//String username = Launcher.conf.getGraphDBLogin();
		//String password = Launcher.conf.getGraphDBPassword();
		String connection_string = "remote:" + "127.0.0.1" + "/" + "cresco";
		String username = "cresco";
		String password = "u$cresco01";
		System.out.println(connection_string);
		System.out.println(username);
		System.out.println(password);
		
        factory = new OrientGraphFactory(connection_string,username,password);
        odb = factory.getTx();
        
        populateDB();
        	
	}
	
	public void populateDB()
	{
		try
		{
			//adding custom classes and constraints
			System.out.println("Create Application Class");
        	if(createClass("Application"))
			{
				createKeyIndex("Application","application_id");
				createKeyIndex("Application","application_name");
			}
        	//node classes
			System.out.println("Create Region Class");
			if(createClass("rNode"))
			{
				createKeyIndex("rNode","node_id");
				createKeyIndex("rNode","node_name");
			}
			//node classes
			System.out.println("Create Agent Class");
			if(createClass("aNode"))
			{
				createKeyIndex("aNode","node_id");
			}
			//node classes
			System.out.println("Create Plugin Class");
			if(createClass("pNode"))
			{
				createKeyIndex("pNode","node_id");
			}
        
		}
		catch(Exception ex)
		{
			System.out.println("populateDB Error: " + ex.toString());
		}
        
	}
	
	//DB Handles
	boolean createClass(String className) 
	{
		boolean wasCreated = false;
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (!schema.existsClass(className)) 
        {
        	txGraph.createVertexType(className);
        	//txGraph.createKeyIndex(key, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        	wasCreated = true;
        }
        txGraph.commit();
        txGraph.shutdown();
        return wasCreated;
    }
	void createKeyIndex(String className, String key) 
	{
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (schema.existsClass(className)) 
        {
        	txGraph.createKeyIndex(key, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        }
        txGraph.commit();
        txGraph.shutdown();
    }
	
	//end DB
	
	public String addAppNode(String application_name)
	{
		try
		{
			String application_id = UUID.randomUUID().toString();
			
			Vertex Application = odb.addVertex("class:Application");
			Application.setProperty("application_id", application_id);
			Application.setProperty("application_name", application_name);
			odb.commit();
			return application_id;
		}
		catch(Exception ex)
		{
			System.out.println("addAppNode: Error " + ex.toString());
		}
		return null;
		
	}
	
	public String getAppNodeId(String application_name)
	{
		
		try
		{
			Vertex Application = odb.getVertexByKey("Application.application_name", application_name);
			if(Application != null)
			{
				return Application.getProperty("application_id");
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getAppNodeId: Error " + ex.toString());
		}
		return null;
		
	}

	public String getNodeId(String region, String agent, String plugin)
	{
		
		try
		{
			String pathname = region + "," + agent + "," + plugin;
			String node_id = pathCache.getIfPresent(pathname);
			if(node_id == null)
			{
				//check region
				if((region != null) && (agent == null) && (plugin == null))
				{
					Vertex rNode = odb.getVertexByKey("rNode.node_name", region);
					if(rNode != null)
					{
						node_id = rNode.getProperty("node_id");
						pathCache.put(pathname, node_id);
						return node_id;
					}
				}
				//check agent
				else if((region != null) && (agent != null) && (plugin == null))
				{
					Vertex rNode = odb.getVertexByKey("rNode.node_name", region);
					if(rNode != null)
					{
						Iterable<Edge> agentEdges = rNode.getEdges(Direction.IN, "isAgent");
						
						Iterator<Edge> iter = agentEdges.iterator();
						while (iter.hasNext())
						{
							Edge isAgent = iter.next();
							Vertex aNode = isAgent.getVertex(Direction.OUT);
							String aNode_name = aNode.getProperty("node_name");
							if(aNode_name.equals(agent))
							{
								node_id = aNode.getProperty("node_id");
								pathCache.put(pathname, node_id);
								return node_id;
							}
							
						}
					}
				}
				//check plugin
				else if((region != null) && (agent != null) && (plugin != null))
				{
					String aNode_id = getNodeId(region,agent,null);
					if(aNode_id != null)
					{
						Vertex aNode = odb.getVertexByKey("aNode.node_id", aNode_id);
						if(aNode != null)
						{
							Iterable<Edge> agentEdges = aNode.getEdges(Direction.IN, "isPlugin");
							Iterator<Edge> iter = agentEdges.iterator();
							while (iter.hasNext())
							{
								Edge isPlugin = iter.next();
								Vertex pNode = isPlugin.getVertex(Direction.OUT);
								String pNode_name = pNode.getProperty("node_name");
								if(pNode_name.equals("plugin"))
								{
									node_id = aNode.getProperty("node_id");
									pathCache.put(pathname, node_id);
									return node_id;
								}
							}
						}
					}
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeId: Error " + ex.toString());
		}
		return null;
		
	}
	
	public String addNode(String region, String agent, String plugin)
	{
		
		try
		{
			String pathname = region + "," + agent + "," + plugin;
			String node_id = pathCache.getIfPresent(pathname);
			
			if(node_id != null)
			{
				System.out.println("Tried to add duplicate Node Path: " + pathname);
			}
			else
			{
				//check region
				if((region != null) && (agent == null) && (plugin == null))
				{
					node_id = UUID.randomUUID().toString();
					
					Vertex rNode = odb.addVertex("class:rNode");
					rNode.setProperty("node_id", node_id);
					rNode.setProperty("node_name", region);
					odb.commit();
					pathCache.put(pathname, node_id);
					return node_id;
					
				}
				//check agent
				else if((region != null) && (agent != null) && (plugin == null))
				{
					Vertex rNode = odb.getVertexByKey("rNode.node_name", region);
					if(rNode == null)
					{
						String rNode_id = addNode(region,null,null);
						rNode = odb.getVertexByKey("rNode.node_id", rNode_id);
					}
					
					node_id = UUID.randomUUID().toString();
					Vertex aNode = odb.addVertex("class:aNode");
					aNode.setProperty("node_id", node_id);
					aNode.setProperty("node_name", agent);
					Edge isAgent = odb.addEdge(null, aNode, rNode, "isAgent");
					odb.commit();
					pathCache.put(pathname, node_id);
					return node_id;
					
				}
				//check plugin
				else if((region != null) && (agent != null) && (plugin != null))
				{
					String aNode_id = getNodeId(region,agent,null);
					Vertex aNode = null;
					if(aNode_id == null)
					{
						aNode_id = addNode(region,agent,null);
						aNode = odb.getVertexByKey("aNode.node_id", aNode_id);
					}
					node_id = UUID.randomUUID().toString();
					Vertex pNode = odb.addVertex("class:pNode");
					aNode.setProperty("node_id", node_id);
					aNode.setProperty("node_name", plugin);
					Edge isPlugin = odb.addEdge(null, pNode, aNode, "isPlugin");
					odb.commit();
					pathCache.put(pathname, node_id);
					return node_id;
					
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeId: Error " + ex.toString());
		}
		return null;
		
	}
	
	public String getNodeClass(String region, String agent, String plugin)
	{
		try
		{
			if((region != null) && (agent == null) && (plugin == null))
			{
				return "rNode";
			}
			else if((region != null) && (agent != null) && (plugin == null))
			{
				return "aNode";
			}
			else if((region != null) && (agent != null) && (plugin != null))
			{
				return "pNode";
			}			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeClass: Error " + ex.toString());
		}
		return null;
		
	}
	
	public Map<String,String> getNodeParams(String region, String agent, String plugin)
	{
		try
		{
			Map<String,String> paramMap = new HashMap<String,String>();
			
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					for(String propKey : Node.getPropertyKeys())
					{
						paramMap.put(propKey, Node.getProperty(propKey).toString());
					}
					return paramMap;
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeParams: Error " + ex.toString());
		}
		return null;
		
	}
	
	public String getNodeParam(String region, String agent, String plugin, String param)
	{
		try
		{
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					String Node_param = Node.getProperty(param).toString();
					if(Node_param != null)
					{
						return Node_param; 
					}
					
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeParam: Error " + ex.toString());
		}
		return null;
		
	}
	
	public Boolean setNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		try
		{
			
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					Iterator it = paramMap.entrySet().iterator();
					while (it.hasNext()) 
					{
						Map.Entry pairs = (Map.Entry)it.next();
						Node.setProperty( pairs.getKey().toString(), pairs.getValue().toString());
					}
					return true;
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("setNodeParams: Error " + ex.toString());
		}
		return false;
		
	}
	
	public Boolean setNodeParam(String region, String agent, String plugin, String paramKey, String paramValue)
	{
		try
		{
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					Node.setProperty(paramKey, paramValue);
					return true;
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getNodeParam: Error " + ex.toString());
		}
		return false;
		
	}
	
	public boolean updateEdge(Edge edge, Map<String,String> params)
	{
		try
		{
			if(edge != null)
			{
				for (Map.Entry<String, String> entry : params.entrySet())
				{
				    edge.setProperty(entry.getKey(), entry.getValue());
				}
				return true;
			}
		}
		catch(Exception ex)
		{
			System.out.println("updateEdge: Error " + ex.toString());
		}
		return false;
	}
	
	public boolean updatePerf(String region, String agent, String plugin, String application, Map<String,String> params)
	{
		try
   	 	{ 
			//precheck input
			String node_class = getNodeClass(region,agent,plugin);
			if(node_class == null)
			{
				System.out.println("GraphDBEngine : updatePerf : Null nodeClass:" + region + " Agent:" + agent + " Plugin:" + plugin);	
				return false;
			}
			if(!node_class.equals("pNode"))
			{
				System.out.println("GraphDBEngine : updatePerf : nodeClass != pNode:" + region + " Agent:" + agent + " Plugin:" + plugin);	
				return false;
			}
			
			//check if app-node-path is in cache
			String appPath = application + "," + region + "," + agent + "," + plugin;
			Edge appPathEdge = appPathCache.getIfPresent(appPath);
			if(appPathEdge != null)
			{
				//ok you have both a know app link and pNode : you can update the edge
				if(updateEdge(appPathEdge,params))
				{
					//if edge was updated return true;
					odb.commit();
					return true;
				}
			}
			else
			{
				//appPathEdge was no found.. created it
				//first check that the pNodeid exist
				
				String node_id = getNodeId(region,agent,plugin);
				if(node_id == null)
				{
					System.out.println("GraphDBEngine : updatePerf : Tried to updatePerf before node_id was created:" + region + " Agent:" + agent + " Plugin:" + plugin);	
					return false;
				}
			
				//second check that the application exist
				
				String application_id = getAppNodeId(application);
				if(application_id == null)
				{
					//create application
					application_id = addAppNode(application);
				}
				//make sure edge exist between pNode and Application
				Vertex Application = odb.getVertexByKey("Application.application_id", application_id);
				
				Iterable<Edge> agentEdges = Application.getEdges(Direction.IN, "isConnected");
				
				Iterator<Edge> iter = agentEdges.iterator();
				while (iter.hasNext())
				{
					Edge isConnected = iter.next();
					Vertex pNode = isConnected.getVertex(Direction.OUT);
					String pNode_id = pNode.getProperty("node_id");
					if(pNode_id.equals(node_id))
					{
						//ok you have both a know app link and pNode : you can update the edge
						if(updateEdge(isConnected,params))
						{
							//we have found the Edge, cache it
							appPathCache.put(appPath, isConnected);
							//if edge was updated return true;
							return true;
						}
					}
					
				}
				//looks like the Edge does not exist, create it
				//grab the pNode Vertex
				Vertex pNode = odb.getVertexByKey("pNode.node_id", node_id);
				appPathEdge = odb.addEdge(null, pNode, Application, "isConnected");
				odb.commit();
				if(updateEdge(appPathEdge,params))
				{
					//if edge was updated return true;
					appPathCache.put(appPath, appPathEdge);
					return true;
				}
				
			}
		
   	 	}
		catch(Exception ex)
		{
			System.out.println("Controller : GraphDBEngine : Failed to updatePerf");
		
		}
		return false;
	}
	
	public void removeNode(String region, String agent, String plugin)
	{
		/*
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
		*/
	}
		
	private void deleteNodesAndRelationships(long nodeId) 
	{
		/*
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
		*/
	}
	
	/*
	private static enum RelType implements RelationshipType
	{
	    isConnected,isPlugin,isAgent
	}
	*/
	

}
