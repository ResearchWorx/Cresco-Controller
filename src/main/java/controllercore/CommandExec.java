package controllercore;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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
							ControllerEngine.gdb.addNode(region, agent,null);								
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
							ControllerEngine.gdb.addNode(region, agent,plugin);
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							ControllerEngine.gdb.addNode(region, agent,null);
						}
						
						System.out.println("addNode region=" + region + " agent=" + agent + " plugin" + plugin);
						
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
							ControllerEngine.gdb.setNodeParam(region, agent, plugin, "configparams", ce.getParam("configparams"));
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							//ControllerEngine.gdb.addNode(region, agent,null);
							ControllerEngine.gdb.setNodeParam(region, agent, null, "configparams", ce.getParam("configparams"));
							//add for agent
							
						}
						
						System.out.println("addNode region=" + region + " agent=" + agent + " plugin" + plugin);
						
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
							ControllerEngine.gdb.removeNode(region, agent,plugin);
						}
						else if((ce.getParam("src_region") != null) && (ce.getParam("src_agent") != null) && (ce.getParam("src_plugin") == null))
						{
							region = ce.getParam("src_region");
							agent = ce.getParam("src_agent");
							ControllerEngine.gdb.removeNode(region, agent,plugin);
						}
						
						System.out.println("removedNode region=" + region + " agent=" + agent + " plugin" + plugin);
						
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
				String application = null;
				
				region = ce.getParam("src_region");
				agent = ce.getParam("src_agent");
				plugin = ce.getParam("src_plugin");
				application = ce.getParam("application");
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
				
				ControllerEngine.gdb.updatePerf(region, agent, plugin, application, params);
				
				ce.setMsgBody("updatedperf");
				return ce;
			}
		return null;
	}
	
	
	
}
