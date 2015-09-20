package scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.ControllerEngine;
import shared.MsgEvent;


public class SchedulerEngine implements Runnable {

		
	public void run() 
	{
		try
		{
			ControllerEngine.SchedulerActive = true;
			while (ControllerEngine.SchedulerActive) 
			{
				try
				{
					MsgEvent ce = ControllerEngine.resourceScheduleQueue.poll();
					if(ce != null)
					{
						System.out.println("me offered");
						//check the pipeline node
						if(ce.getParam("globalcmd").equals("addplugin"))
						{
							
						}
						else if(ce.getParam("globalcmd").equals("removeplugin"))
						{
							System.out.println("Removing iNode: " + ce.getParam("inode_id"));
							ControllerEngine.gdb.removeINode(ce.getParam("resource_id"),ce.getParam("inode_id"));
						}
					}
					else
					{
						Thread.sleep(1000);
					}
				}
				catch(Exception ex)
				{
					System.out.println("SchedulerEngine Error: " + ex.toString());
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println("SchedulerEngine Error: " + ex.toString());
		}
	}
	
	
	private MsgEvent preCheck(MsgEvent ce)
	{
		//pre-schedule check
		String configparams = ce.getParam("configparams");
		String[] cparams = configparams.split(",");
		Map<String,String> cm = new HashMap<String,String>();
		for(String param : cparams)
		{
			String[] paramkv = param.split("=");
			cm.put(paramkv[0], paramkv[1]);
		}
		
		//check if we have the plugin
		List<String> pluginMap = getPluginInventory();
		String requestedPlugin = cm.get("pluginname") + "=" + cm.get("pluginversion");
		if(pluginMap.contains(requestedPlugin))
		{
			//plugin exist on controller
			//below here should be done by provisioning system
			//find least loaded agent
			String pluginPath = ControllerEngine.gdb.getLowAgent();
			if(pluginPath != null)
			{
				System.out.println(pluginPath);
				ce.setMsgBody("Plugin Assigned");
				ce.setParam("pluginstatus","scheduled");
				
			}
			else
			{
				ce.setMsgBody("No agent avalable for resource assignment!");
				ce.setParam("pluginstatus","failed");			
			}
		}
		else
		{
			ce.setMsgBody("Matching plugin could not be found!");
			ce.setParam("pluginstatus","failed");
		}
		return ce;
	}
	
	public static List<String> getPluginInventory()
	{
		List<String> pluginList = new ArrayList<String>();
		
		try
		{
		File jarLocation = new File(ControllerEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		String parentDirName = jarLocation.getParent(); // to get the parent dir name
		
		File folder = new File(parentDirName + "/plugins");
		if(folder.exists())
		{
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) 
		    {
		      if (listOfFiles[i].isFile()) 
		      {
		        //System.out.println("Found Plugin: " + listOfFiles[i].getName());
		        //<pluginName>=<pluginVersion>,
		        String pluginPath = listOfFiles[i].getAbsolutePath();
		        pluginList.add(ControllerEngine.commandExec.getPluginName(pluginPath) + "=" + ControllerEngine.commandExec.getPluginVersion(pluginPath));
		        //pluginList = pluginList + getPluginName(pluginPath) + "=" + getPluginVersion(pluginPath) + ",";
		        //pluginList = pluginList + listOfFiles[i].getName() + ",";
		      } 
		      
		    }
		    if(pluginList.size() > 0)
		    {
		    	return pluginList;
		    }
		}
		
		
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		return null; 
		
	}
	
	
		
}
