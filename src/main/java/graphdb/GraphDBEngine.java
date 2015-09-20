package graphdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class GraphDBEngine {
	
	public OrientGraphFactory factory;
	private OrientGraph odb;
	//public Cache<String, String> nodePathCache;
	//public Cache<String, Edge> appPathCache;
	
	public int retryCount = 50;
	
	public GraphDBEngine()
	{
		/*
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
		*/
		
		//String connection_string = "remote:" + Launcher.conf.getGraphDBServer() + "/" + Launcher.conf.getGraphDBName();
		//String username = Launcher.conf.getGraphDBLogin();
		//String password = Launcher.conf.getGraphDBPassword();
		String connection_string = "remote:" + "127.0.0.1" + "/" + "cresco";
		String username = "root";
		String password = "cody01";
		System.out.println(connection_string);
		System.out.println(username);
		System.out.println(password);
		
        factory = new OrientGraphFactory(connection_string,username,password).setupPool(10, 100);
        
        initCrescoDB();
	}
	
	//new database functions
	//READS
	public String getINodeId(String resource_id, String inode_id)
	{
		String node_id = null;
		OrientGraph graph = null;
		try
		{
			graph = factory.getTx();
			
			Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:iNode.nodePath WHERE key = [\"" + resource_id + "\",\"" + node_id + "\"]")).execute();
					
			Iterator<Vertex> iter = resultIterator.iterator();
			if(iter.hasNext())
			{
				Vertex v = iter.next();
				node_id = v.getProperty("rid").toString();
			}
			if(node_id != null)
			{
				node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : getINodeID : Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
	}
	
	public String getResourceNodeId(String resource_id)
	{
		String node_id = null;
		OrientGraph graph = null;
		try
		{
			graph = factory.getTx();
			
			Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:resourceNode.nodePath WHERE key = '" + resource_id + "'")).execute();
					
			Iterator<Vertex> iter = resultIterator.iterator();
			if(iter.hasNext())
			{
				Vertex v = iter.next();
				node_id = v.getProperty("rid").toString();
			}
			if(node_id != null)
			{
				node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : getResourceNodeID : Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
	}
		
	public String getNodeId(String region, String agent, String plugin)
	{
		String node_id = null;
		OrientGraph graph = null;
		
		try
		{
			
			if((region != null) && (agent == null) && (plugin == null))
			{
				//OrientGraphNoTx graph = factory.getNoTx();
				graph = factory.getTx();
				
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:rNode.nodePath WHERE key = '" + region + "'")).execute();
						
				Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					Vertex v = iter.next();
					node_id = v.getProperty("rid").toString();
				}
				if(node_id != null)
				{
					node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
				}
				//return isFound;
			}
			else if((region != null) && (agent != null) && (plugin == null))
			{
				//OrientGraph graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				graph = factory.getTx();
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:aNode.nodePath WHERE key = [\"" + region + "\",\"" + agent + "\"]")).execute();
			    Iterator<Vertex> iter = resultIterator.iterator();
			    if(iter.hasNext())
				{
					Vertex v = iter.next();
					node_id = v.getProperty("rid").toString();
				}
				//graph.shutdown();
				//return node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			    //node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			    if(node_id != null)
				{
					node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
				}
			}
			else if((region != null) && (agent != null) && (plugin != null))
			{
				//OrientGraph graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				graph = factory.getTx();
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:pNode.nodePath WHERE key = [\"" + region + "\",\"" + agent + "\",\"" + plugin +"\"]")).execute();
			    Iterator<Vertex> iter = resultIterator.iterator();
			    if(iter.hasNext())
				{
					Vertex v = iter.next();
					node_id = v.getProperty("rid").toString();
				}
				//graph.shutdown();
				//return node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			    //node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
			    if(node_id != null)
				{
					node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : getNodeID : Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
	}
	
	public List<String> getNodeList(String region, String agent, String plugin)
	{
		List<String> node_list = null;
		OrientGraph graph = null;
		try
		{
			
			if((region == null) && (agent == null) && (plugin == null))
			{
				//OrientGraph graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				graph = factory.getTx();
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:rNode.nodePath")).execute();
						
				Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					node_list = new ArrayList<String>();
					while(iter.hasNext())
					{
						Vertex v = iter.next();
						String node_id = v.getProperty("rid").toString();
						node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
						Vertex rNode = graph.getVertex(node_id);
						node_list.add(rNode.getProperty("region").toString()); 
					}
				}
				
			}
			else if((region != null) && (agent == null) && (plugin == null))
			{
				graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:aNode.nodePath WHERE key = [\"" + region + "\"]")).execute();
			    Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					node_list = new ArrayList<String>();
					while(iter.hasNext())
					{
						Vertex v = iter.next();
						String node_id = v.getProperty("rid").toString();
						node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
						Vertex aNode = graph.getVertex(node_id);
						node_list.add(aNode.getProperty("agent").toString());
					}
				}
				
			}
			else if((region != null) && (agent != null) && (plugin == null))
			{
				graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:pNode.nodePath WHERE key = [\"" + region + "\",\"" + agent +"\"]")).execute();
				Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					node_list = new ArrayList<String>();
					while(iter.hasNext())
					{
						Vertex v = iter.next();
						String node_id = v.getProperty("rid").toString();
						node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
						Vertex pNode = graph.getVertex(node_id);
						node_list.add(pNode.getProperty("plugin").toString());
					}
				}
				//graph.shutdown();
				//return node_list;
				
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("GrapgDBEngine : getNodeList : Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_list;
	}
	
	public List<String> getresourceNodeList(String resource_id, String inode_id)
	{
		List<String> node_list = null;
		OrientGraph graph = null;
		try
		{
			
			if((resource_id == null) && (inode_id == null))
			{
				graph = factory.getTx();
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:resourceNode.nodePath")).execute();
						
				Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					node_list = new ArrayList<String>();
					while(iter.hasNext())
					{
						Vertex v = iter.next();
						String node_id = v.getProperty("rid").toString();
						node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
						Vertex rNode = graph.getVertex(node_id);
						node_list.add(rNode.getProperty("resource_id").toString()); 
					}
				}
				
			}
			else if((resource_id != null) && (inode_id == null))
			{
				graph = factory.getTx();
				//OrientGraphNoTx graph = factory.getNoTx();
				
				Iterable<Vertex> resultIterator = graph.command(new OCommandSQL("SELECT rid FROM INDEX:iNode.nodePath WHERE key = [\"" + resource_id + "\"]")).execute();
			    Iterator<Vertex> iter = resultIterator.iterator();
				if(iter.hasNext())
				{
					node_list = new ArrayList<String>();
					while(iter.hasNext())
					{
						Vertex v = iter.next();
						String node_id = v.getProperty("rid").toString();
						node_id = node_id.substring(node_id.indexOf("[") + 1, node_id.indexOf("]"));
						Vertex aNode = graph.getVertex(node_id);
						node_list.add(aNode.getProperty("inode_id").toString());
					}
				}
				
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("GrapgDBEngine : getNodeList : Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_list;
	}
		
	public String getINodeParam(String resource_id,String inode_id, String param)
	{
		String iNode_param = null;
		String node_id = null;
		OrientGraph graph = null;
		
		try
		{
			node_id = getINodeId(resource_id,inode_id);
			if(node_id != null)
			{
				graph = factory.getTx();
				Vertex iNode = graph.getVertex(inode_id);
				iNode_param = iNode.getProperty(param).toString();
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("getINodeParam: Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return iNode_param;
	}
	
	//WRITES
	
	public String addINode(String resource_id, String inode_id)
	{
		String node_id = null;
		
		int count = 0;
		try
		{
			
			while((node_id == null) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("ADDNODE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				node_id = IaddINode(resource_id, inode_id);
				count++;
				
			}
			
			if((node_id == null) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : addINode : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : addINode : Error " + ex.toString());
		}
		
		return node_id;
	}
	
	private String IaddNode(String region, String agent, String plugin)
	{
		String node_id = null;
		OrientGraph graph = null;
		try
		{
			node_id = getNodeId(region,agent,plugin);
			
			if(node_id != null)
			{
				//System.out.println("Node already Exist: region=" + region + " agent=" + agent + " plugin=" + plugin);
			}
			else
			{
				//System.out.println("Adding Node : region=" + region + " agent=" + agent + " plugin=" + plugin);
				if((region != null) && (agent == null) && (plugin == null))
				{
					graph = factory.getTx();
					Vertex v = graph.addVertex("class:rNode");
					v.setProperty("region", region);
					graph.commit();
					node_id = v.getId().toString();
				}
				else if((region != null) && (agent != null) && (plugin == null))
				{
					String region_id = getNodeId(region,null,null);
				
					if(region_id == null)
					{
						//System.out.println("Must add region=" + region + " before adding agent=" + agent);
						region_id = addNode(region,null,null);
						
					}
					if(region_id != null)
					{
						graph = factory.getTx();
						Vertex v = graph.addVertex("class:aNode");
						v.setProperty("region", region);
						v.setProperty("agent", agent);
				    
						Vertex fromV = graph.getVertex(v.getId().toString());
						Vertex toV = graph.getVertex(region_id);
					
						graph.addEdge("class:isAgent", fromV, toV, "isAgent");
						graph.commit();
						node_id = v.getId().toString();
						/*
				    	//add edges
				    
				    	String edge_id = addEdge(region,agent,null,region,null,null,"isAgent");
				    	if(edge_id == null)
				    	{
				    		System.out.println("Unable to add isAgent Edge between region=" + region + " and agent=" + agent);
				    	}
						 */
					}
				}
				else if((region != null) && (agent != null) && (plugin != null))
				{
					//System.out.println("Adding Plugin : region=" + region + " agent=" + agent + " plugin=" + plugin);
					
					String agent_id = getNodeId(region,agent,null);
					if(agent_id == null)
					{
						//System.out.println("For region=" + region + " we must add agent=" + agent + " before adding plugin=" + plugin);
						agent_id = addNode(region,agent,null);
						
					}
					
					if(agent_id != null)
					{
						graph = factory.getTx();
						Vertex v = graph.addVertex("class:pNode");
					    v.setProperty("region", region);
					    v.setProperty("agent", agent);
					    v.setProperty("plugin", plugin);
					    
					    Vertex fromV = graph.getVertex(v.getId().toString());
						Vertex toV = graph.getVertex(agent_id);
						
						graph.addEdge("class:isPlugin", fromV, toV, "isPlugin");
						graph.commit();
					    /*
					    //add Edge
					    String edge_id = addEdge(region,agent,plugin,region,agent,null,"isPlugin");
					    if(edge_id == null)
					    {
					    	System.out.println("Unable to add isPlugin Edge between region=" + region + " agent=" + "agent=" + region + " and agent=" + agent);
					    }
					    */
					    node_id = v.getId().toString();
					}
				}
			}
			
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("IaddNode: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
		
	}
	
	public String addNode(String region, String agent, String plugin)
	{
		String node_id = null;
		int count = 0;
		try
		{
			
			while((node_id == null) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("ADDNODE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				node_id = IaddNode(region, agent, plugin);
				count++;
				
			}
			
			if((node_id == null) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : addNode : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : addNode : Error " + ex.toString());
		}
		
		return node_id;
	}
	
	private String IaddINode(String resource_id, String inode_id)
	{
		String node_id = null;
		String resource_node_id = null;
		
		OrientGraph graph = null;
		try
		{
			node_id = getINodeId(resource_id,inode_id);
			if(node_id != null)
			{
				//System.out.println("Node already Exist: region=" + region + " agent=" + agent + " plugin=" + plugin);
			}
			else
			{
				
				resource_node_id = getResourceNodeId(resource_id);
				if(resource_node_id == null)
				{
					resource_node_id = addResourceNode(resource_id);
				}
				
				if(resource_node_id != null)
				{
					graph = factory.getTx();
					
					Vertex fromV = graph.addVertex("class:iNode");
					fromV.setProperty("inode_id", inode_id);
					
					
					//ADD EDGE TO RESOURCE
					Vertex toV = graph.getVertex(resource_node_id);
					graph.addEdge("class:isResource", fromV, toV, "isResource");
					graph.commit();
					node_id = fromV.getId().toString();
				}
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("IaddINode: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
		
	}
	
	public String addResourceNode(String resource_id)
	{
		String node_id = null;
		
		int count = 0;
		try
		{
			
			while((node_id == null) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("ADDNODE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				node_id = IaddResourceNode(resource_id);
				count++;
				
			}
			
			if((node_id == null) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : addINode : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : addINode : Error " + ex.toString());
		}
		
		return node_id;
	}
	
	private String IaddResourceNode(String resource_id)
	{
		String node_id = null;
		OrientGraph graph = null;
		try
		{
			node_id = getResourceNodeId(resource_id);
			
			if(node_id != null)
			{
				//System.out.println("Node already Exist: region=" + region + " agent=" + agent + " plugin=" + plugin);
			}
			else
			{
				graph = factory.getTx();
				//add something
				
				Vertex v = graph.addVertex("class:resourceNode");
				v.setProperty("resource_id", resource_id);
				graph.commit();
				node_id = v.getId().toString();
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("addResourceNode: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return node_id;
		
	}
	
	public String addEdge(String src_region, String src_agent, String src_plugin, String dst_region, String dst_agent, String dst_plugin, String className)
	{
		String edge_id = null;
		int count = 0;
		try
		{
			
			while((edge_id == null) && (count != retryCount))
			{
				edge_id = IaddEdge(src_region, src_agent, src_plugin, dst_region, dst_agent, dst_plugin, className);
				count++;
			}
			
			if((edge_id == null) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : addEdge : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : addEdge : Error " + ex.toString());
		}
		
		return edge_id;
	}
	
	private String IaddEdge(String src_region, String src_agent, String src_plugin, String dst_region, String dst_agent, String dst_plugin, String className) 
	{
		String edge_id = null;
		try
		{
			String src_node_id = getNodeId(src_region,src_agent,src_plugin);
			String dst_node_id = getNodeId(dst_region,dst_agent,dst_plugin);
			
			OrientGraph graph = factory.getTx();
		    Vertex fromV = graph.getVertex(src_node_id);
			Vertex toV = graph.getVertex(dst_node_id);
			
			Edge isEdge = graph.addEdge("class:" + className, fromV, toV, className);
			graph.commit();
		    graph.shutdown();
		    edge_id = isEdge.getId().toString();
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception
		}
		catch(Exception ex)
		{
			System.out.println("addEdge Error: " + ex.toString());
			
		}
		return edge_id;
		
    }
	
	public boolean removeNode(String region, String agent, String plugin)
	{
		boolean nodeRemoved = false;
		int count = 0;
		try
		{
			
			while((!nodeRemoved) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("REMOVENODE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				nodeRemoved = IremoveNode(region, agent, plugin);
				count++;
				
			}
			
			if((!nodeRemoved) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : removeNode : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : removeNode : Error " + ex.toString());
		}
		
		return nodeRemoved;
	}
	
	private boolean IremoveNode(String region, String agent, String plugin)
	{
		boolean nodeRemoved = false;
		OrientGraph graph = null;
		try
		{
			//String pathname = getPathname(region,agent,plugin);
			String node_id = getNodeId(region,agent,plugin);
			if(node_id == null)
			{
				//System.out.println("Tried to remove missing node : " + pathname);
				nodeRemoved = true; 
			}
			else
			{
				if((region != null) && (agent == null) && (plugin == null))
				{
					List<String> agentList = getNodeList(region,null,null);
					if(agentList != null)
					{
						for(String removeAgent : agentList)
						{
							removeNode(region,removeAgent,null);
						}
					}
					agentList = getNodeList(region,null,null);
					if(agentList == null)
					{
						graph = factory.getTx();
					    Vertex rNode = graph.getVertex(node_id);
					    graph.removeVertex(rNode);
						graph.commit();
						nodeRemoved = true;
					}
				
			}
			if((region != null) && (agent != null) && (plugin == null))
			{
				
				List<String> pluginList = getNodeList(region,agent,null);
				if(pluginList != null)
				{
					for(String removePlugin : pluginList)
					{
						removeNode(region,agent,removePlugin);
					}
				}
				pluginList = getNodeList(region,agent,null);
				if(pluginList == null)
				{
					graph = factory.getTx();
					Vertex aNode = graph.getVertex(node_id);
				    graph.removeVertex(aNode);
					graph.commit();
					nodeRemoved = true;
				}
			}
			if((region != null) && (agent != null) && (plugin != null))
			{
				graph = factory.getTx();
				Vertex pNode = graph.getVertex(node_id);
				graph.removeVertex(pNode);
				graph.commit();
				nodeRemoved = true; 
			}
			
		}
	}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("GrapgDBEngine : removeNode :  thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return nodeRemoved;
		
	}

	public boolean removeINode(String resource_id, String inode_id)
	{
		boolean nodeRemoved = false;
		int count = 0;
		try
		{
			
			while((!nodeRemoved) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("REMOVENODE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				nodeRemoved = IremoveINode(resource_id, inode_id);
				count++;
				
			}
			
			if((!nodeRemoved) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : removeINode : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : removeINode : Error " + ex.toString());
		}
		
		return nodeRemoved;
	}
	
	private boolean IremoveINode(String resource_id, String inode_id)
	{
		boolean nodeRemoved = false;
		OrientGraph graph = null;
		try
		{
			//String pathname = getPathname(region,agent,plugin);
			String node_id = getINodeId(resource_id, inode_id);
			if(node_id == null)
			{
				//System.out.println("Tried to remove missing node : " + pathname);
				nodeRemoved = true; 
			}
			else
			{
					graph = factory.getTx();
					Vertex rNode = graph.getVertex(node_id);
					graph.removeVertex(rNode);
					graph.commit();
					nodeRemoved = true;
			}
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("GrapgDBEngine : removeNode :  thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return nodeRemoved;
		
	}
	
	public boolean IsetINodeParams(String resource_id, String inode_id, Map<String,String> paramMap)
	{
		boolean isUpdated = false;
		OrientGraph graph = null;
		String node_id = null;
		try
		{
			node_id = getINodeId(resource_id, inode_id);
			if(node_id != null)
			{
				graph = factory.getTx();
				Vertex iNode = graph.getVertex(node_id);
				Iterator it = paramMap.entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pairs = (Map.Entry)it.next();
					iNode.setProperty( pairs.getKey().toString(), pairs.getValue().toString());
				}
				graph.commit();
				isUpdated = true;
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("setINodeParams: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return isUpdated;
	}
		
	public boolean setINodeParams(String resource_id, String inode_id, Map<String,String> paramMap)
	{
		boolean isUpdated = false;
		int count = 0;
		try
		{
			
			while((!isUpdated) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("iNODEUPDATE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				isUpdated = IsetINodeParams(resource_id,inode_id, paramMap);
				count++;
				
			}
			
			if((!isUpdated) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : setINodeParams : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : setINodeParams : Error " + ex.toString());
		}
		
		return isUpdated;
	}
	
	public boolean setNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		boolean isUpdated = false;
		int count = 0;
		try
		{
			
			while((!isUpdated) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("iNODEUPDATE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				isUpdated = IsetNodeParams(region, agent, plugin, paramMap);
				count++;
				
			}
			
			if((!isUpdated) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : setINodeParams : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : setINodeParams : Error " + ex.toString());
		}
		
		return isUpdated;
	}
	
	public boolean IsetNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		boolean isUpdated = false;
		OrientGraph graph = null;
		String node_id = null;
		try
		{
			node_id = getNodeId(region,agent,plugin);
			if(node_id != null)
			{
				graph = factory.getTx();
				Vertex iNode = graph.getVertex(node_id);
				Iterator it = paramMap.entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pairs = (Map.Entry)it.next();
					iNode.setProperty( pairs.getKey().toString(), pairs.getValue().toString());
				}
				graph.commit();
				isUpdated = true;
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("setINodeParams: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return isUpdated;
	}
	
	public boolean setINodeParam(String resource_id, String inode_id, String paramKey, String paramValue)
	{
		boolean isUpdated = false;
		int count = 0;
		try
		{
			
			while((!isUpdated) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("iNODEUPDATE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				isUpdated = IsetINodeParam(resource_id, inode_id, paramKey, paramValue);
				count++;
				
			}
			
			if((!isUpdated) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : setINodeParam : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : setINodeParam : Error " + ex.toString());
		}
		
		return isUpdated;
	}
	
	public boolean IsetINodeParam(String resource_id, String inode_id, String paramKey, String paramValue)
	{
		boolean isUpdated = false;
		OrientGraph graph = null;
		String node_id = null;
		try
		{
			node_id = getINodeId(resource_id, inode_id);
			if(node_id != null)
			{
				graph = factory.getTx();
				Vertex iNode = graph.getVertex(node_id);
				iNode.setProperty( paramKey, paramValue);
				graph.commit();
				isUpdated = true;
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("setINodeParams: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return isUpdated;
	}
	
	public boolean setNodeParam(String region, String agent, String plugin, String paramKey, String paramValue)
	{
		boolean isUpdated = false;
		int count = 0;
		try
		{
			
			while((!isUpdated) && (count != retryCount))
			{
				if(count > 0)
				{
					//System.out.println("iNODEUPDATE RETRY : region=" + region + " agent=" + agent + " plugin" + plugin);
					Thread.sleep((long)(Math.random() * 1000)); //random wait to prevent sync error
				}
				isUpdated = IsetNodeParam(region, agent, plugin, paramKey, paramValue);
				count++;
				
			}
			
			if((!isUpdated) && (count == retryCount))
			{
				System.out.println("GraphDBEngine : setINodeParam : Failed to add node in " + count + " retrys");
			}
		}
		catch(Exception ex)
		{
			System.out.println("GraphDBEngine : setINodeParam : Error " + ex.toString());
		}
		
		return isUpdated;
	}
	
	public boolean IsetNodeParam(String region, String agent, String plugin, String paramKey, String paramValue)
	{
		boolean isUpdated = false;
		OrientGraph graph = null;
		String node_id = null;
		try
		{
			node_id = getNodeId(region, agent, plugin);
			if(node_id != null)
			{
				graph = factory.getTx();
				Vertex iNode = graph.getVertex(node_id);
				iNode.setProperty( paramKey, paramValue);
				graph.commit();
				isUpdated = true;
			}
		}
		catch(com.orientechnologies.orient.core.storage.ORecordDuplicatedException exc)
		{
			//eat exception.. this is not normal and should log somewhere
		}
		catch(com.orientechnologies.orient.core.exception.OConcurrentModificationException exc)
		{
			//eat exception.. this is normal
		}
		catch(Exception ex)
		{
			long threadId = Thread.currentThread().getId();
			System.out.println("setINodeParams: thread_id: " + threadId + " Error " + ex.toString());
		}
		finally
		{
			if(graph != null)
			{
				graph.shutdown();
			}
		}
		return isUpdated;
	}
	
	
	//INIT Functions
	public void initCrescoDB()
	{
		try
		{
			//index properties
			
			System.out.println("Create Region Vertex Class");
			String[] rProps = {"region"}; //Property names
			createVertexClass("rNode", rProps);
			System.out.println("Create Agent Vertex Class");
			String[] aProps = {"region", "agent"}; //Property names
			createVertexClass("aNode", aProps);
			System.out.println("Create Plugin Vertex Class");
			String[] pProps = {"region", "agent", "plugin"}; //Property names
			createVertexClass("pNode", pProps);
		    
		    
		    System.out.println("Create resourceNode Vertex Class");
		    String[] resourceProps = {"resource_id"}; //Property names
		    createVertexClass("resourceNode", resourceProps);
		    
		    System.out.println("Create iNode Vertex Class");
		    String[] iProps = {"resource_id", "inode_id"}; //Property names
		    createVertexClass("iNode", iProps);
		    
		    
		    System.out.println("Create isAgent Edge Class");
		    String[] isAgentProps = {"edge_id"}; //Property names
		    createEdgeClass("isAgent",isAgentProps);
		    
		    System.out.println("Create isPlugin Edge Class");
		    String[] isPluginProps = {"edge_id"}; //Property names
		    createEdgeClass("isPlugin",isPluginProps);
		    
		    System.out.println("Create isConnected Edge Class");
		    String[] isConnectedProps = {"edge_id"}; //Property names
		    createEdgeClass("isConnected",isConnectedProps);
		    
		    System.out.println("Create isResource Edge Class");
		    String[] isResourceProps = {"edge_id"}; //Property names
		    createEdgeClass("isResource",isResourceProps);
		    
			
		}
		catch(Exception ex)
		{
			System.out.println("initCrescoDB Error: " + ex.toString());
		}
	}
	
	boolean createVertexClass(String className, String[] props) 
	{
		boolean wasCreated = false;
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (!schema.existsClass(className)) 
        {
        	OClass vt = txGraph.createVertexType(className);
        	for(String prop : props)
        		  vt.createProperty(prop, OType.STRING);
        	//Create unique composite index using all properties
        	vt.createIndex(className + ".nodePath", OClass.INDEX_TYPE.UNIQUE, props);
        	
        	//txGraph.createKeyIndex(key, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        	wasCreated = true;
        }
        else
        {
        	/*
        	OClass vt = txGraph.getVertexType(className);
        	for(String prop : props)
      		  vt.createProperty(prop, OType.STRING);
        	//Create unique composite index using all properties
        	vt.createIndex(className + ".nodePath", OClass.INDEX_TYPE.UNIQUE, props);
        	wasCreated = true;
        	*/
        }
        txGraph.commit();
        txGraph.shutdown();
        return wasCreated;
    }
	
	boolean createEdgeClass(String className, String[] props) 
	{
		boolean wasCreated = false;
		OrientGraphNoTx txGraph = factory.getNoTx();
        //OSchema schema = ((OrientGraph)odb).getRawGraph().getMetadata().getSchema();
        OSchema schema = ((OrientGraphNoTx)txGraph).getRawGraph().getMetadata().getSchema();
        
        if (!schema.existsClass(className)) 
        {
        	OClass et = txGraph.createEdgeType(className);
        	for(String prop : props)
      		  et.createProperty(prop, OType.STRING);
        	
        	et.createIndex(className + ".edgeProp", OClass.INDEX_TYPE.UNIQUE, props);
        	wasCreated = true;
        }
        txGraph.commit();
        txGraph.shutdown();
        return wasCreated;
    }
	
	//CLIENT FUNCTIONS
	
	//client DB
	
	
	//end new database functions
	
	
	public String getLowAgent()
	{
		String agent_path = null;
		int plugin_count = -1;
		try
		{
			List<String> regionList = getNodeList(null,null,null);
			System.out.println("Region Count: " + regionList.size());
			for(String region : regionList)
			{
				List<String> agentList = getNodeList(region,null,null);
				System.out.println("Agent Count: " + agentList.size());
				for(String agent: agentList)
				{
					List<String> pluginList = getNodeList(region,agent,null);
					System.out.println("Plugin Count: " + pluginList.size());
					if(pluginList.size() > plugin_count)
					{
						agent_path = region + "," + agent; 
						plugin_count = pluginList.size();
					}
				}
			}
		}
		catch(Exception ex)
		{
			
		}
		
		return agent_path;
	}

	
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
							//if edge was updated return true;
							return true;
						}
					}
					
				}
				//looks like the Edge does not exist, create it
				//grab the pNode Vertex
				Vertex pNode = odb.getVertexByKey("pNode.node_id", node_id);
				odb.commit();
				
		
   	 	}
		catch(Exception ex)
		{
			System.out.println("Controller : GraphDBEngine : Failed to updatePerf");
		
		}
		return false;
	}
			

}
