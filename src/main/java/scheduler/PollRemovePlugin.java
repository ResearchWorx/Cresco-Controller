package scheduler;

import java.util.List;

import core.ControllerEngine;
import shared.MsgEvent;
import shared.MsgEventType;

public class PollRemovePlugin implements Runnable { 

	private String resource_id  = null;
	private String inode_id = null;
	
	public PollRemovePlugin(String resource_id,String inode_id)
	{
		this.resource_id = resource_id;
		this.inode_id = inode_id;
		
	}

	public void run() {
        try 
        {
        	String edge_id = ControllerEngine.gdb.getResourceEdgeId(resource_id, inode_id);
    		if(edge_id != null)
    		{
    			String pnode_node_id = ControllerEngine.gdb.getIsAssignedParam(edge_id, "out");
    			if(pnode_node_id != null)
				{
    				pnode_node_id = pnode_node_id.substring(pnode_node_id.indexOf("[") + 1, pnode_node_id.indexOf("]"));
    				
    				String region = ControllerEngine.gdb.getIsAssignedParam(edge_id, "region");
    				String agent = ControllerEngine.gdb.getIsAssignedParam(edge_id, "agent");
    				String plugin = ControllerEngine.gdb.getIsAssignedParam(edge_id, "plugin");
    				
    				String pnode_node_id_match = ControllerEngine.gdb.getNodeId(region, agent, plugin);
    				if(pnode_node_id.equals(pnode_node_id_match))
    				{
    					//fire off remove command
        				MsgEvent me = removePlugin(region,agent,plugin);
        				ControllerEngine.commandExec.cmdExec(me);
        				//loop until remove is completed
        				
    					int count = 0;
	        			boolean isRemoved = false;
	        			while((!isRemoved) && (count < 30))
	        			{
	        				if(ControllerEngine.gdb.getNodeId(region, agent, plugin) == null)
	        				{
	        					isRemoved = true;
	        				}
	        				else
	        				{
	        					Thread.sleep(1000);
	        				}
	        			}
	        			if(isRemoved)
	        			{
	        				System.out.println("Deactivated iNode: " + inode_id);
							
	        			}
	        			else
	        			{
	        				System.out.println("SchedulerEngine : pollRemovePlugin : unable to verify iNode deactivation!");
	        			}
	        			
    				}
    				
    				
				}
    			
    		}
    		else
    		{
    			System.out.println("Edge_id=null");
    		}
    		System.out.println("Removing iNode: " + inode_id);
			ControllerEngine.gdb.removeINode(resource_id,inode_id);
			
			//remove resource_id if this is the last resource
			List<String> inodes = ControllerEngine.gdb.getresourceNodeList(resource_id,null);
			if(inodes == null)
			{
				ControllerEngine.gdb.removeResourceNode(resource_id);
			}
			
        	/*
        	if(edge_id != null)
        	{
        		if((ControllerEngine.gdb.setINodeParam(resource_id,inode_id,"status_code","10")) &&
						(ControllerEngine.gdb.setINodeParam(resource_id,inode_id,"status_desc","iNode Active.")))
				{
						//recorded plugin activations
        				System.out.println("SchedulerEngine : pollAddPlugin : Activated inode_id=" + inode_id);
				}
        	}
        	else
        	{
        		System.out.println("SchedulerEngine : pollAddPlugin : unable to verify iNode activation!");
        	}
        	*/
        }
	   catch(Exception v) 
	   {
            System.out.println(v);
       }
    }  

	public MsgEvent removePlugin(String region, String agent, String plugin)
	{
		MsgEvent me = new MsgEvent(MsgEventType.CONFIG,region,null,null,"remove plugin");
		me.setParam("src_region", region);
		me.setParam("src_agent", "external");
		me.setParam("dst_region", region);
		me.setParam("dst_agent", agent);
		me.setParam("controllercmd", "regioncmd");
		me.setParam("configtype", "pluginremove");
		me.setParam("plugin", plugin);
		return me;	
	}
	
}
