package graphdb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class GraphDBEngine {

	private OrientGraphFactory factory;
	private OrientGraph odb;
	public Cache<String, String> nodePathCache;
	public Cache<String, Edge> appPathCache;
	
	//public Cache<String, Edge> appPathCache;
	
	public Map<String, Long> inProcessNode;
	public Map<String, Long> inProcessEdge;
	
	
	//private List<String> inProcessPaths;
	
	public GraphDBEngine()
	{
		nodePathCache = CacheBuilder.newBuilder()
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
		
		inProcessNode = new ConcurrentHashMap<String,Long>();
		inProcessEdge  = new ConcurrentHashMap<String,Long>();
		
		
		//inProcessPaths = new ArrayList<String>();
		
		//String connection_string = "remote:" + Launcher.conf.getGraphDBServer() + "/" + Launcher.conf.getGraphDBName();
		//String username = Launcher.conf.getGraphDBLogin();
		//String password = Launcher.conf.getGraphDBPassword();
		String connection_string = "remote:" + "127.0.0.1" + "/" + "cresco";
		String username = "root";
		String password = "cody01";
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
        	if(createVertexClass("Application"))
			{
				createVertexKeyIndex("Application","application_id");
				createVertexKeyIndex("Application","application_name");
			}
        	System.out.println("Create Region Vertex Class");
			if(createVertexClass("rNode"))
			{
				createVertexKeyIndex("rNode","node_id");
				createVertexKeyIndex("rNode","node_name");
			}
			System.out.println("Create Agent Vertex Class");
			if(createVertexClass("aNode"))
			{
				createVertexKeyIndex("aNode","node_id");
			}
			System.out.println("Create Plugin Vertex Class");
			if(createVertexClass("pNode"))
			{
				createVertexKeyIndex("pNode","node_id");
			}
			//edge classes
			System.out.println("Create isAgent Edge Class");
			if(createEdgeClass("isAgent"))
			{
				createVertexKeyIndex("isAgent","edge_id");
			}
			System.out.println("Create isPlugin Edge Class");
			if(createEdgeClass("isPlugin"))
			{
				createVertexKeyIndex("isPlugin","edge_id");
			}
			System.out.println("Create isConnected Edge Class");
			if(createEdgeClass("isConnected"))
			{
				createVertexKeyIndex("isConnected","edge_id");
			}
			
        
		}
		catch(Exception ex)
		{
			System.out.println("populateDB Error: " + ex.toString());
		}
        
	}
	
	//DB Handles
	boolean createVertexClass(String className) 
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
	boolean createEdgeClass(String className) 
	{
		boolean wasCreated = false;
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (!schema.existsClass(className)) 
        {
        	txGraph.createEdgeType(className);
        	//txGraph.createKeyIndex(key, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        	wasCreated = true;
        }
        txGraph.commit();
        txGraph.shutdown();
        return wasCreated;
    }
	void createVertexKeyIndex(String className, String key) 
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
	void createEdgeKeyIndex(String className, String key) 
	{
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (schema.existsClass(className)) 
        {
        	txGraph.createKeyIndex(key, Edge.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        }
        txGraph.commit();
        txGraph.shutdown();
    }
	Boolean addEdge(Vertex fromV, Vertex toV, String className) 
	{
		//Vertex rNode = odb.addVertex("class:rNode");
		
		try
		{
			odb.begin();
			Edge isEdge = odb.addEdge("class:" + className, fromV, toV, className);
			odb.commit();
			return true;
			
		}
		catch(Exception ex)
		{
			System.out.println("addEdge Error: " + ex.toString());
		}
		return false;
		
    }
	//end DB
	
	public String addAppNode(String application_name)
	{
		try
		{
			odb.begin();
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
	public String getPathname(String region, String agent, String plugin)
	{
		return region + "," + agent + "," + plugin;
		
	}
	public String getNodeId(String region, String agent, String plugin)
	{
		try
		{
			String pathname = getPathname(region,agent,plugin);
			/*
			while(inProcessNode.containsKey(pathname))
			{
				System.out.println("getNodeId: inProcessPaths: "+ pathname + " Sleeping....");
				Thread.sleep(1000);
			}
			*/
			
			//String node_id = nodePathCache.getIfPresent(pathname);
			
			String node_id = null;
			if(node_id != null)
			{
				return node_id;
			}	
			else
			{
				//check region
				if((region != null) && (agent == null) && (plugin == null))
				{
					System.out.println("getnodeid Region " + region);
					Vertex rNode = odb.getVertexByKey("rNode.node_name", region);
					if(rNode != null)
					{
						node_id = rNode.getProperty("node_id");
						//nodePathCache.put(pathname, node_id);
						System.out.println("getnodeid Region " + region + " " + node_id);
						return node_id;
					}
					
				}
				//check agent
				else if((region != null) && (agent != null) && (plugin == null))
				{
					System.out.println("getnodeid agent " + agent);
					
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
								//nodePathCache.put(pathname, node_id);
								System.out.println("getnodeid agent " + agent + " " + node_id);
								
								return node_id;
							}
							
						}
					}
				}
				//check plugin
				else if((region != null) && (agent != null) && (plugin != null))
				{
					System.out.println("getnodeid plugin " + plugin);
					
					String aNode_id = getNodeId(region,agent,null);
					
					if(aNode_id != null)
					{
						System.out.println("getnodeid plugin " + plugin + " got agentid:" + aNode_id);
						
						Vertex aNode = odb.getVertexByKey("aNode.node_id", aNode_id);
						if(aNode != null)
						{
							System.out.println("getnodeid plugin " + plugin + " got agent");
							
							Iterable<Edge> agentEdges = aNode.getEdges(Direction.IN, "isPlugin");
							Iterator<Edge> iter = agentEdges.iterator();
							while (iter.hasNext())
							{
								Edge isPlugin = iter.next();
								Vertex pNode = isPlugin.getVertex(Direction.OUT);
								String pNode_name = pNode.getProperty("node_name");
								System.out.println("getnodeid plugin " + plugin + " got plugin_name:" + pNode_name);
								
								if(pNode_name.equals(plugin))
								{
									node_id = aNode.getProperty("node_id");
									//nodePathCache.put(pathname, node_id);
									System.out.println("getnodeid plugin " + plugin + " " + node_id);
									
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
			//System.out.println("getNodeId: Error:" + ex.toString());
			long threadId = Thread.currentThread().getId();
			System.out.println("getNodeId: thread_id: " + threadId + " Error:" + ex.toString());
			
		}
		return null;
		
	}
	
	public String addNode(String region, String agent, String plugin)
	{
		
		try
		{
			String pathname = getPathname(region,agent,plugin);
			
			/*
			while(inProcessNode.containsKey(pathname))
			{
				System.out.println("getNodeId: inProcessPaths: "+ pathname + " Sleeping....");
				Thread.sleep(1000);
			}
			*/
			
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				System.out.println("Tried to add duplicate Node : " + pathname + " node_id:" + node_id);
				return node_id; 
			}
			
			if(node_id == null)
			{
				//check region
				if((region != null) && (agent == null) && (plugin == null))
				{
					odb.begin();
					
					node_id = UUID.randomUUID().toString();
					
					inProcessNode.put(pathname, System.currentTimeMillis()); //block until added
					
					Vertex rNode = odb.addVertex("class:rNode");
					rNode.setProperty("node_id", node_id);
					rNode.setProperty("node_name", region);
					odb.commit();
					//check that node can be fetched
					rNode = odb.getVertexByKey("rNode.node_name", region);
					//block add request
					while(rNode == null)
					{
						rNode = odb.getVertexByKey("rNode.node_name", region);
						System.out.println("Verifying rNode_id:" + node_id + " ....");
					}
					//nodePathCache.put(pathname, node_id);
					inProcessNode.remove(pathname);
					return node_id;
					
				}
				//check agent
				else if((region != null) && (agent != null) && (plugin == null))
				{
					odb.begin();
					
					node_id = UUID.randomUUID().toString();
					inProcessNode.put(pathname, System.currentTimeMillis()); //block until added
					
					Vertex rNode = odb.getVertexByKey("rNode.node_name", region);
					if(rNode == null)
					{
						String rNode_id = addNode(region,null,null);
						rNode = odb.getVertexByKey("rNode.node_id", rNode_id);
						//return null;
					}
					
					Vertex aNode = odb.addVertex("class:aNode");
					aNode.setProperty("node_id", node_id);
					aNode.setProperty("node_name", agent);
					
					//Edge isAgent = odb.addEdge(null, aNode, rNode, "isAgent");
					if(!addEdge(aNode,rNode,"isAgent"))
					{
						System.out.println("addNode Error: FAILED: adding aNode(" + agent + "->rNode("+ region +") edge: ");
					}
					odb.commit();
					aNode = odb.getVertexByKey("aNode.node_id", node_id);
					//block add request
					while(aNode == null)
					{
						aNode = odb.getVertexByKey("aNode.node_id", node_id);
						System.out.println("Verifying aNode_id:" + node_id + " ....");
					}
					//nodePathCache.put(pathname, node_id);
					inProcessNode.remove(pathname); //block until added
					return node_id;
					
				}
				//check plugin
				else if((region != null) && (agent != null) && (plugin != null))
				{
					odb.begin();
					
					node_id = UUID.randomUUID().toString();
					inProcessNode.put(pathname, System.currentTimeMillis()); //block until added
					
					String aNode_id = getNodeId(region,agent,null);
					
					Vertex aNode = aNode = odb.getVertexByKey("aNode.node_id", aNode_id);
					
					if(aNode == null)
					{
						aNode_id = addNode(region,agent,null);
						aNode = odb.getVertexByKey("aNode.node_id", aNode_id);
					}
					
					node_id = UUID.randomUUID().toString();
					Vertex pNode = odb.addVertex("class:pNode");
					
					pNode.setProperty("node_id", node_id);
					pNode.setProperty("node_name", plugin);
					//Edge isPlugin = odb.addEdge(null, pNode, aNode, "isPlugin");
					if(!addEdge(pNode,aNode,"isPlugin"))
					{
						System.out.println("addNode Error: FAILED: adding pNode(" + plugin + "->aNode("+ agent +") edge: ");
					}
					odb.commit();
					pNode = odb.getVertexByKey("pNode.node_id", node_id);
					//block add request
					while(pNode == null)
					{
						pNode = odb.getVertexByKey("pNode.node_id", node_id);
						System.out.println("Verifying pNode_id:" + node_id + " ....");
					}
					
					//nodePathCache.put(pathname, node_id);
					inProcessNode.remove(pathname); //block until added
					return node_id;
					
				}
			}
			
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("addNode: thread_id: " + threadId + " Error " + ex.toString());
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
			odb.begin();
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				String pathname = getPathname(region,agent,plugin);
				inProcessNode.put(pathname, System.currentTimeMillis()); //block until added
				
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					Iterator it = paramMap.entrySet().iterator();
					while (it.hasNext()) 
					{
						Map.Entry pairs = (Map.Entry)it.next();
						Node.setProperty( pairs.getKey().toString(), pairs.getValue().toString());
					}
					odb.commit();
					inProcessNode.remove(pathname);
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
			odb.begin();
			
			String node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				String pathname = getPathname(region,agent,plugin);
				inProcessNode.put(pathname, System.currentTimeMillis()); //block until added
				
				Vertex Node = odb.getVertexByKey(getNodeClass(region,agent,plugin) + ".node_id", node_id);
				if(Node != null)
				{
					Node.setProperty(paramKey, paramValue);
					odb.commit();
					inProcessNode.remove(pathname);
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
				odb.begin();
				
				for (Map.Entry<String, String> entry : params.entrySet())
				{
				    edge.setProperty(entry.getKey(), entry.getValue());
				}
				odb.commit();
			
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
	
	public Boolean removeNode(String region, String agent, String plugin)
	{
		try
		{
			String pathname = getPathname(region,agent,plugin);
			System.out.println("Removing pathname: " + pathname);
			/*
			while(inProcessNode.containsKey(pathname))
			{
				System.out.println("removeNode: inProcessPaths: " + pathname + " Sleeping....");
				Thread.sleep(1000);
			}
			*/
			
			String node_id = getNodeId(region,agent,plugin);
			
			//remove from cache
			nodePathCache.invalidate(pathname);
			
			if(node_id == null)
			{
				//remove node
				System.out.println("removeNode: Error: Node Path: " + pathname + " does not exist!");
				return false;
			}	
			else
			{
				
				//check region
				if((region != null) && (agent == null) && (plugin == null))
				{
					System.out.println("Removing region: " + region);
					
					odb.begin();
					//remove region
					Vertex rNode = odb.getVertexByKey("rNode.node_id", node_id);
					Iterable<Edge> agentEdges = rNode.getEdges(Direction.IN, "isAgent");
					Iterator<Edge> iter = agentEdges.iterator();
					while (iter.hasNext())
					{
						Edge isAgent = iter.next();
						Vertex aNode = isAgent.getVertex(Direction.OUT);
						String agent_name = aNode.getProperty("node_name"); 
						if(!removeNode(region,agent_name,null))
						{
							System.out.println("removeNode: Error: Unable to remove aNode_id:" + aNode.getProperty("node_id")  + " aNode_name:" + aNode.getProperty("node_name"));
							return false;
						}
					}
					odb.removeVertex(rNode);
					odb.commit();
					return true;
				}
				//check agent
				else if((region != null) && (agent != null) && (plugin == null))
				{
					System.out.println("Removing agent: " + agent);
					
						odb.begin();
						Vertex aNode = odb.getVertexByKey("aNode.node_id", node_id);
						if(aNode == null)
						{
							System.out.println("Can't remove agent:" + agent + " does no exist");
							return true;
						}
						Iterable<Edge> pluginEdges = aNode.getEdges(Direction.IN, "isPlugin");
						Iterator<Edge> iter = pluginEdges.iterator();
						while (iter.hasNext())
						{
							Edge isPlugin = iter.next();
							Vertex pNode = isPlugin.getVertex(Direction.OUT);
							String plugin_name = pNode.getProperty("node_name"); 
							if(!removeNode(region,agent,plugin_name))
							{
								System.out.println("removeNode: Error: Unable to remove pNode_id:" + pNode.getProperty("node_id")  + " pNode_name:" + pNode.getProperty("node_name"));
								return false;
							}
						}
						
						odb.removeVertex(aNode);
						odb.commit();
						return true;
					
				}
				//check plugin
				else if((region != null) && (agent != null) && (plugin != null))
				{
					odb.begin();
					
					Vertex pNode = odb.getVertexByKey("pNode.node_id", node_id);
					if(pNode == null)
					{
						System.out.println("Can't remove plugin:" + plugin + " does no exist");
						return true;
					}
					
					for(String propKey : pNode.getPropertyKeys())
					{
						if(pNode.getProperty(propKey) !=null)
						{
							System.out.println("REMOVING key: " + propKey + " value:" + pNode.getProperty(propKey).toString());
						}
						else
						{
							System.out.println("REMOVING key: " + propKey);
							
						}
							
					
					}
					odb.removeVertex(pNode);
					odb.commit();
					return true;
				}
			}
			
		}
		catch(Exception ex)
		{
			//System.out.println("getNodeId: Error:" + ex.toString());
			long threadId = Thread.currentThread().getId();
			System.out.println("removeNode: thread_id: " + threadId + " Error:" + ex.toString());
			
		}
		return null;
		
	}
		

}
