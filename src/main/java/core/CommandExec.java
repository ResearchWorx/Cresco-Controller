package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import shared.MsgEvent;
import shared.MsgEventType;

public class CommandExec {

	public CommandExec()
	{
		
	}
	
	public MsgEvent cmdExec(MsgEvent ce)
	{
			if(ce.getMsgType() == MsgEventType.CONFIG)
			{
				if(ce.getParam("controllercmd") != null)
				{
					if(ce.getParam("controllercmd").equals("registercontroller"))
					{
						if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") != null))
				    	{
							ce.setMsgBody("controllerregistered");
							String region = null;
							String agent = null;
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							String node_id = ControllerEngine.gdb.addNode(region, agent,null);
							if(node_id != null)
							{
								System.out.println("SUCCESS: registered Node region=" + region + " agent=" + agent);		
							}
							else
							{
								System.out.println("FAILED: registered Node region=" + region + " agent=" + agent);	
							}
							return ce;
				    	}
						else
						{
							System.out.println("Controller : CommandExec : EnableController Error.. Bad Params");
						}
					}
					else if(ce.getParam("controllercmd").equals("addnode"))
					{
						//add node
						//ControllerEngine.gdb.addNode("regionName", "agentName",null);
						String region = null;
						String agent = null;
						String plugin = null;
						if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") != null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							plugin = ce.getParam("src_plugin");
							String node_id = ControllerEngine.gdb.addNode(region, agent,plugin);
							if(node_id != null)
							{
								System.out.println("CommandExec : addNode() SUCCESS: Adding pNode: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);	
							}
							else
							{
								System.out.println("CommandExec : addNode() FAILED: Adding pNode: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);
							}
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							
							String node_id = ControllerEngine.gdb.addNode(region, agent, null);
							if(node_id != null)
							{
								System.out.println("CommandExec : addNode() SUCCESS: Adding aNode: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);	
							}
							else
							{
								System.out.println("CommandExec : addNode() FAILED: Adding aNode: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);
							}
						}
						
						//System.out.println("addNode region=" + region + " agent=" + agent + " plugin" + plugin);
						
						ce.setMsgBody("nodeadded");
						return ce;
					}
					
					else if(ce.getParam("controllercmd").equals("setparams"))
					{
						//add node
						//ControllerEngine.gdb.addNode("regionName", "agentName",null);
						String region = null;
						String agent = null;
						String plugin = null;
						if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") != null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							plugin = ce.getParam("src_plugin");
							//add for plugin
							//ControllerEngine.gdb.addNode(region, agent,plugin);
							int timeout = 0;
							//this to to avoid problem related to runcase
							/*
							while((ControllerEngine.gdb.getNodeId(region, agent, plugin) == null) && (timeout < 5))
							{
								try {
									Thread.sleep(1000);
									System.out.println("CommandExec : setParams() Wait on Node Create: Region:" + region + " Agent:" + agent + " Plugin:" + plugin);
									
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							*/
							if(ControllerEngine.gdb.getNodeId(region, agent, plugin) != null)
							{
								ControllerEngine.gdb.setNodeParam(region, agent, plugin, "configparams", ce.getParam("configparams"));
							}
							
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							//ControllerEngine.gdb.addNode(region, agent,null);
							int timeout = 0;
							//this to to avoid problem related to runcase
							while((ControllerEngine.gdb.getNodeId(region, agent, null) == null) && (timeout < 5))
							{
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if(ControllerEngine.gdb.getNodeId(region, agent, null) != null)
							{
								ControllerEngine.gdb.setNodeParam(region, agent, null, "configparams", ce.getParam("configparams"));
							}
							//add for agent
						}
						
						System.out.println("setParams region=" + region + " agent=" + agent + " plugin" + plugin);
						
						ce.setMsgBody("paramsadded");
						return ce;
					}
					
					else if(ce.getParam("controllercmd").equals("removenode"))
					{
						String region = null;
						String agent = null;
						String plugin = null;
						
						if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") != null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							plugin = ce.getParam("src_plugin");
							if(ControllerEngine.gdb.removeNode(region, agent,plugin))
							{
								System.out.println("SUCCESS: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
							else
							{
								System.out.println("FAILURE: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
							
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							//ControllerEngine.gdb.removeNode(region, agent,plugin);
							if(ControllerEngine.gdb.removeNode(region, agent,plugin))
							{
								System.out.println("SUCCESS: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
							else
							{
								System.out.println("FAILURE: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") == null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							//agent = ce.getParam("src_agent");
							//ControllerEngine.gdb.removeNode(region, agent,plugin);
							if(ControllerEngine.gdb.removeNode(region, agent,plugin))
							{
								System.out.println("SUCCESS: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
							else
							{
								System.out.println("FAILURE: removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
							}
						}
						
						
						ce.setMsgBody("noderemoved");
						return ce;
					}
					else if(ce.getParam("controllercmd").equals("regioncmd"))
					{
							ce.removeParam("controllercmd");
							if((ce.getParam("dst_region")  != null) && (ce.getParam("dst_agent")  != null) && (ce.getParam("configtype")  != null))
							{
								if(!ControllerEngine.regionalMsgMap.containsKey(ce.getParam("dst_region")))
								{
									ConcurrentLinkedQueue<MsgEvent> cmq = new ConcurrentLinkedQueue<MsgEvent>();
									cmq.add(ce);
									ControllerEngine.regionalMsgMap.put(ce.getParam("dst_region"), cmq);
								}
								else
								{
									ConcurrentLinkedQueue<MsgEvent> cmq = ControllerEngine.regionalMsgMap.get(ce.getParam("dst_region"));
									cmq.add(ce);
								}
								System.out.println("External Message to: " + ce.getParam("dst_region") + " " + ce.getParam("dst_agent") + " " + ce.getParamsString());
								System.out.println("Queue:" + ce.getParam("dst_region") + " has " + ControllerEngine.regionalMsgMap.get(ce.getParam("dst_region")).size() + " messages");
							}
						
					}
					
				}
				else if(ce.getParam("globalcmd") != null)
				{
					if(ce.getParam("globalcmd").equals("addplugin"))
					{
						if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null) && (ce.getParam("configparams") != null))
						{
							if(ControllerEngine.gdb.getINodeId(ce.getParam("resource_id"),ce.getParam("inode_id")) == null)
							{
								if(ControllerEngine.gdb.addINode(ce.getParam("resource_id"),ce.getParam("inode_id")) != null)
								{
									if((ControllerEngine.gdb.setINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_code","0")) &&
										(ControllerEngine.gdb.setINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_desc","iNode Scheduled.")) &&
										(ControllerEngine.gdb.setINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"configparams",ce.getParam("configparams"))))
									{
										ce.setParam("status_code","0");
										ce.setParam("status_desc","iNode Scheduled");
										ControllerEngine.resourceScheduleQueue.offer(ce);
									}
									else
									{
										ce.setParam("status_code","1");
										ce.setParam("status_desc","Could not set iNode params");
									}
								}
								else
								{
									ce.setParam("status_code","1");
									ce.setParam("status_desc","Could not create iNode_id!");	
								}
							}
							else
							{
								ce.setParam("status_code","1");
								ce.setParam("status_desc","iNode_id already exist!");
							}
						}
						else
						{
							ce.setParam("status_code","1");
							ce.setParam("status_desc","No iNode_id found in payload!");	
						}
							
						return ce;
					}
					else if(ce.getParam("globalcmd").equals("removeplugin"))
					{
						if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null))
						{
							if(ControllerEngine.gdb.getINodeId(ce.getParam("resource_id"),ce.getParam("inode_id")) != null)
							{
								if((ControllerEngine.gdb.setINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_code","10")) &&
								(ControllerEngine.gdb.setINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_desc","iNode scheduled for removal.")))
								{
									ce.setParam("status_code","10");
									ce.setParam("status_desc","iNode scheduled for removal.");
									ControllerEngine.resourceScheduleQueue.offer(ce);
								}
								else
								{
									ce.setParam("status_code","1");
									ce.setParam("status_desc","Could not set iNode params");
								}
							}
							else
							{
								ce.setParam("status_code","1");
								ce.setParam("status_desc","iNode_id does not exist in DB!");	
							}
						}
						else
						{
							ce.setParam("status_code","1");
							ce.setParam("status_desc","No resource_id or iNode_id found in payload!");	
						}
							
						return ce;
					}
					else if(ce.getParam("globalcmd").equals("plugininfo"))
					{
						try
						{
							if(ce.getParam("plugin_id") != null)
							{
								String plugin_id = ce.getParam("plugin_id");
								List<String> pluginFiles = getPluginFiles();
							
								if(pluginFiles != null)
								{
									for (String pluginPath : pluginFiles) 
									{
										String found_plugin_id = getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath);
										if(plugin_id.equals(found_plugin_id))
										{
											String params = getPluginParams(pluginPath);
											if(params != null)
											{
												System.out.println("Found Plugin: " + plugin_id);
												ce.setParam("node_name",getPluginName(pluginPath));
												ce.setParam("node_id",plugin_id);
												ce.setParam("params",params);
											}
											
										}
									}
								}
								else
								{
									ce.setMsgBody("Plugin does not exist");
								}
							}
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
							ce.setMsgBody("Error: " + ex.toString());
						}
						return ce;   
					}
					else if(ce.getParam("globalcmd").equals("getenvstatus"))
					{
						try
						{
							if((ce.getParam("environment_id") != null) && (ce.getParam("environment_value") != null))
							{
								String indexName = ce.getParam("environment_id");
								String indexValue = ce.getParam("environment_value");
								
								List<String> envNodeList = ControllerEngine.gdb.getANodeFromIndex(indexName, indexValue);
								ce.setParam("count",String.valueOf(envNodeList.size()));
							}
							else
							{
								ce.setParam("count","unknown");
							}					
							
						}
						catch(Exception ex)
						{
							ce.setParam("count","unknown");
						}
						return ce;
					}
					else if(ce.getParam("globalcmd").equals("getpluginstatus"))
					{
						try
						{
							if((ce.getParam("inode_id") != null) && (ce.getParam("resource_id") != null))
							{
								String status_code = ControllerEngine.gdb.getINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_code");
								String status_desc = ControllerEngine.gdb.getINodeParam(ce.getParam("resource_id"),ce.getParam("inode_id"),"status_desc");
								if((status_code != null) && (status_desc != null))
								{
									ce.setParam("status_code",status_code);
									ce.setParam("status_desc",status_desc);
								}
								else
								{
									ce.setParam("status_code","1");
									ce.setParam("status_desc","Could not read iNode params");
								}
							}
							else
							{
								ce.setParam("status_code","1");
								ce.setParam("status_desc","No iNode_id found in payload!");	
							}					
							
						}
						catch(Exception ex)
						{
							ce.setParam("status_code","1");
							ce.setParam("status_desc",ex.toString());	
						}
						return ce;
					}
					else if(ce.getParam("globalcmd").equals("resourceinventory"))
					{
						try
						{
							String rt = ControllerEngine.se.getResourceTotal();
							
							if(rt != null)
							{
								ce.setParam("resourceinventory", rt);
								ce.setMsgBody("Inventory found.");
							}
							else
							{
								ce.setMsgBody("No plugin directory exist to inventory");
							}
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
							ce.setMsgBody("Error: " + ex.toString());
						}
						return ce;   
					}
					else if(ce.getParam("globalcmd").equals("plugininventory"))
					{
						try
						{
							List<String> pluginFiles = getPluginFiles();
							
							if(pluginFiles != null)
							{
								String pluginList = null;
								for (String pluginPath : pluginFiles) 
								{
									pluginList = pluginList + getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath) + ",";
								}
								pluginList = pluginList.substring(0, pluginList.length() - 1);
								System.out.println("pluginList=" + pluginList);
								ce.setParam("pluginlist", pluginList);
								ce.setMsgBody("There were " + pluginFiles.size() + " plugins found.");
							}
							else
							{
								ce.setMsgBody("No plugin directory exist to inventory");
							}
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
							ce.setMsgBody("Error: " + ex.toString());
						}
						return ce;   
					}
					else if(ce.getParam("globalcmd").equals("plugindownload"))
					{
						try
						{
						String baseUrl = ce.getParam("pluginurl");
						if(!baseUrl.endsWith("/"))
						{
							baseUrl = baseUrl + "/";
						}
						
						URL website = new URL(baseUrl + ce.getParam("plugin"));
						ReadableByteChannel rbc = Channels.newChannel(website.openStream());
						
						File jarLocation = new File(ControllerEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
						String parentDirName = jarLocation.getParent(); // to get the parent dir name
						String pluginDir = parentDirName + "/plugins";
						//check if directory exist, if not create it
						File pluginDirfile = new File(pluginDir);
						if (!pluginDirfile.exists()) {
							if (pluginDirfile.mkdir()) {
								System.out.println("Directory " + pluginDir + " didn't exist and was created.");
							} else {
								System.out.println("Directory " + pluginDir + " didn't exist and we failed to create it!");
							}
						}
						String pluginFile = parentDirName + "/plugins/" + ce.getParam("plugin");
						boolean forceDownload = false;
						if(ce.getParam("forceplugindownload") != null)
						{
							forceDownload = true;
							System.out.println("Forcing Plugin Download");
						}
						
						File pluginFileObject = new File(pluginFile);
						if (!pluginFileObject.exists() || forceDownload) 
						{
							FileOutputStream fos = new FileOutputStream(parentDirName + "/plugins/" + ce.getParam("plugin"));
							
							fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
							if(pluginFileObject.exists())
							{
								ce.setParam("hasplugin", ce.getParam("plugin"));
								ce.setMsgBody("Downloaded Plugin:" + ce.getParam("plugin"));
								System.out.println("Downloaded Plugin:" + ce.getParam("plugin"));
							}
							else
							{
								ce.setMsgBody("Problem Downloading Plugin:" + ce.getParam("plugin"));
								System.out.println("Problem Downloading Plugin:" + ce.getParam("plugin"));
							}
						}
						else
						{
							ce.setMsgBody("Plugin already exists:" + ce.getParam("plugin"));
							ce.setParam("hasplugin", ce.getParam("plugin"));
							System.out.println("Plugin already exists:" + ce.getParam("plugin"));
						}
						
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
							ce.setMsgBody("Error: " + ex.toString());
						}
						return ce;
					}
					
				}
				
			}
			else if(ce.getMsgType() == MsgEventType.EXEC)
			{
				if(ce.getParam("cmd").equals("getmsg"))
				{
					try
					{
						String getRegion = ce.getParam("getregion");
						//System.out.println("Get for Region: " + getRegion);
						if(ControllerEngine.regionalMsgMap.containsKey(getRegion))
						{
							if(!ControllerEngine.regionalMsgMap.isEmpty())
							{
								//return if something is on the queue, else return null
								return ControllerEngine.regionalMsgMap.get(getRegion).poll();
							}
						}
					}
					catch(Exception ex)
					{
						System.out.println("global controller msgexec " + ex.toString());
					}
				}
				
			}
			else if(ce.getMsgType() == MsgEventType.WATCHDOG)
			{
				String region = null;
				String agent = null;
				String plugin = null;
				String resource_id = null;
				String inode_id = null;
				
				region = ce.getParam("src_region");
				agent = ce.getParam("src_agent");
				plugin = ce.getParam("src_plugin");
				resource_id = ce.getParam("resource_id");
				inode_id = ce.getParam("inode_id");
				
				//clean params for edge
				/*
				ce.removeParam("loop");
				ce.removeParam("isGlobal");
				ce.removeParam("src_agent");
				ce.removeParam("src_region");
				ce.removeParam("src_plugin");
				ce.removeParam("dst_agent");
				ce.removeParam("dst_region");
				ce.removeParam("dst_plugin");
				*/
				Map<String,String> params = ce.getParams();
				
				ControllerEngine.gdb.updatePerf(region, agent, plugin, resource_id, inode_id, params);
				
				ce.setMsgBody("updatedperf");
				return ce;
			}
		return null;
	}
	
	public static String getPluginName(String jarFile) //This should pull the version information from jar Meta data
	{
			   String version;
			   try{
			   //String jarFile = AgentEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			   //System.out.println("JARFILE:" + jarFile);
			   //File file = new File(jarFile.substring(5, (jarFile.length() )));
			   File file = new File(jarFile);
	          FileInputStream fis = new FileInputStream(file);
	          @SuppressWarnings("resource")
			   JarInputStream jarStream = new JarInputStream(fis);
			   Manifest mf = jarStream.getManifest();
			   
			   Attributes mainAttribs = mf.getMainAttributes();
	          version = mainAttribs.getValue("artifactId");
			   }
			   catch(Exception ex)
			   {
				   String msg = "Unable to determine Plugin Version " + ex.toString();
				   System.err.println(msg);
				   version = "Unable to determine Version";
			   }
			   return version;
	}
	
	public static Map<String,String> getPluginParamMap(String jarFileName)
	{
		Map<String,String> phm = null;
		try 
		{
			phm = new HashMap<String,String>();
	        JarFile jarFile = new JarFile(jarFileName);
            JarEntry je = jarFile.getJarEntry("plugin.conf");
            InputStream in = jarFile.getInputStream(je);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) 
            {
              	line = line.replaceAll("\\s+","");
              	String[] sline = line.split("=");
               	if((sline[0] != null) && (sline[1] != null))
              	{
               		phm.put(sline[0], sline[1]);
                }
            }
            reader.close();
            in.close();
            jarFile.close();
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }
		return phm;
	}
	
	public static String getPluginParams(String jarFileName)
	{
		String params = "";
		try 
		{
			JarFile jarFile = new JarFile(jarFileName);
            JarEntry je = jarFile.getJarEntry("plugin.conf");
            InputStream in = jarFile.getInputStream(je);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) 
            {
              	line = line.replaceAll("\\s+","");
              	if(line.contains("="))
              	{
              		String[] sline = line.split("=");
              		if((sline[0] != null) && (sline[1] != null))
              		{
              			//phm.put(sline[0], sline[1]);
              			if((sline[1].equals("required")) || sline[1].equals("optional"))
              			{
              				params = params + sline[0] + ":" + sline[1] + ",";
              			}
              		}
              	}
            }
            reader.close();
            in.close();
            jarFile.close();
            if(params.length() == 0)
            {
            	params = null;
            }
            else
            {
            	params = params.substring(0,params.length() -1);
            }
        } 
		catch (IOException e) 
		{
			params = null;
            e.printStackTrace();
        }
		return params;
	}
	
	public static String getPluginVersion(String jarFile) //This should pull the version information from jar Meta data
	{
			   String version;
			   try{
			   //String jarFile = AgentEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			   //System.out.println("JARFILE:" + jarFile);
			   //File file = new File(jarFile.substring(5, (jarFile.length() )));
			   File file = new File(jarFile);
	          FileInputStream fis = new FileInputStream(file);
	          @SuppressWarnings("resource")
			   JarInputStream jarStream = new JarInputStream(fis);
			   Manifest mf = jarStream.getManifest();
			   
			   Attributes mainAttribs = mf.getMainAttributes();
	          version = mainAttribs.getValue("Implementation-Version");
			   }
			   catch(Exception ex)
			   {
				   String msg = "Unable to determine Plugin Version " + ex.toString();
				   System.err.println(msg);
				   version = "Unable to determine Version";
			   }
			   return version;
	}
	public static List<String> getPluginFiles()
	{
		List<String> pluginFiles = null;
		try
		{
			//String pluginList = "";
			File jarLocation = new File(ControllerEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String parentDirName = jarLocation.getParent(); // to get the parent dir name
		
			File folder = new File(parentDirName + "/" + ControllerEngine.config.getParam("localpluginrepo"));
			if(folder.exists())
			{
				pluginFiles = new ArrayList<String>();
				File[] listOfFiles = folder.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) 
				{
					if (listOfFiles[i].isFile()) 
					{
						pluginFiles.add(listOfFiles[i].getAbsolutePath());
						//System.out.println(listOfFiles[i].toPath());
					} 
		      
				}
				if(pluginFiles.isEmpty())
				{
					pluginFiles = null;
				}
			}
		
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
			pluginFiles = null;
		}
		return pluginFiles;
	}
	
	
}
