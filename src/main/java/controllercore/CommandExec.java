package controllercore;

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
				}
				
			}
	
		return null;
	}
	
	
	
}
