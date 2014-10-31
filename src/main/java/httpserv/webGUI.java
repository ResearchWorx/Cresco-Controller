package httpserv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class webGUI {
	
	
	@GET
	@Path("{subResources:.*}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getfile(@PathParam("subResources") String subResources) 
	{
		if(subResources.length() == 0)
		{
			subResources = "/index.html";
		}
		else
		{
			subResources = "/" + subResources;
		}
		InputStream in = null;
		try{
			in = getClass().getResourceAsStream(subResources);
			if(in == null) {
				
				in = getClass().getResourceAsStream("/404.html");
				return Response.status(Response.Status.NOT_FOUND).entity(in).build();
		    }
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
			in = getClass().getResourceAsStream("/500.html");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(in).build();
	    
		}
		
		if(subResources.endsWith(".html"))
		{
			return Response.ok(in, MediaType.TEXT_HTML_TYPE).build();	
		}
		else if(subResources.endsWith(".js"))
		{
			return Response.ok(in, "text/javascript").build();
		}
		else if(subResources.endsWith(".css") || subResources.endsWith(".less"))
		{
			return Response.ok(in, "text/css").build();
		}
		else if(subResources.endsWith(".svg"))
		{
			return Response.ok(in, "image/svg+xml").build();
		}
		else if(subResources.endsWith(".woff"))
		{
			return Response.ok(in, "application/font-woff").build();
		}
		else
		{
			return Response.ok(in, MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Disposition", "attachment; filename=\"" + "somefile" + "\"" ) //optional
					.build();
		}
	}
	
	/*
	@GET
	@Path("{subResources:.*}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getfile(@PathParam("subResources") String subResources) 
	{
		System.out.println("SubResources:" + subResources);
		if(subResources.length() == 0)
		{
			subResources = "index.html";
		}
		File file = null;
		try{
		String filename = "webroot/" + subResources;
		System.out.println(filename);
		String path = httpServerEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if(path.startsWith("file:"))
		{
			path = path.substring(5);
		}
			path = URLDecoder.decode(path, "UTF-8");
			System.out.println(path);
			
		path = new File(path).getParentFile().getPath() + File.separator + filename;
		System.out.println(path);
		// /Users/cody/Documents/Mesh/Work/Development/Cresco/cresco-agent-controller-plugin/target//index.html
		file = new File(path);
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		
		if(subResources.endsWith(".html"))
		{
			return Response.ok(file, MediaType.TEXT_HTML_TYPE).build();	
		}
		else if(subResources.endsWith(".js"))
		{
			return Response.ok(file, "text/javascript").build();
		}
		else if(subResources.endsWith(".css") || subResources.endsWith(".less"))
		{
			return Response.ok(file, "text/css").build();
		}
		else if(subResources.endsWith(".svg"))
		{
			return Response.ok(file, "image/svg+xml").build();
		}
		else if(subResources.endsWith(".woff"))
		{
			return Response.ok(file, "application/font-woff").build();
		}
		else
		{
			return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
					.build();
		}
	}
	
	@GET
	@Path("/js/{subResources:.*}")
	@Produces("text/javascript")
	
	public String getjs(@PathParam("subResources") String subResources) throws FileNotFoundException, UnsupportedEncodingException
	{
	  //return subResources;
	  return getTextFile("js/" + subResources);
	}
	
	
	@GET
	@Path("/css/{subResources:.*}")
	@Produces("text/css")
	public String getcss(@PathParam("subResources") String subResources) throws FileNotFoundException, UnsupportedEncodingException
	{
	  //return subResources;
	  return getTextFile("css/" + subResources);
	}
	
	@GET
	@Path("/images/{subResources:.*}")
	@Produces("image/png")
	public StreamingOutput getpng(@PathParam("subResources") String subResources) throws FileNotFoundException, UnsupportedEncodingException
	
	//public StreamingOutput getThumbNail(final String filePath) 
	{
		final String filePath = "images/" + subResources;
		System.out.println("Fetching Image:" + filePath);
		try{
	    return new StreamingOutput() {
	      @Override
	      public void write(OutputStream out) throws IOException, WebApplicationException {
	        //... read your stream and write into os
	    	  System.out.println("INside issue:" + filePath);
	    	  InputStream in = getClass().getResourceAsStream(filePath);
	    	  
	    	  if(in != null)
	    	  {
	    		  System.out.println("Inputsream is no null");
	    	  }
	    	  else
	    	  {
	    		  System.out.println("Inputsream is null");
	    	  }
	    	  
	    	  IOUtils.copy(in,out);
	    	  //in.close();
	    	  //out.close();
	      }
	    };
		}
		catch(Exception ex)
		{
			System.out.println("could not fetch image" + ex.toString());
			return null;
		}
		
	  }
	
String getTextFile(String filePath)
{
	String fileContents = null;
	try{
		InputStream in = getClass().getResourceAsStream(filePath);
		
		if(in != null)
  	  {
  		  System.out.println("Inputsream is no null");
  	  }
  	  else
  	  {
  		  System.out.println("Inputsream is null");
  	  }
		
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		fileContents = org.apache.commons.io.IOUtils.toString(input);
	}
	catch(Exception ex)
	{
		fileContents = "getFile Error:" + ex.toString();
	}
	return fileContents;
}

String getFile(String filename) throws FileNotFoundException, UnsupportedEncodingException
{
	String path = httpServerEngine.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	if(path.startsWith("file:"))
	{
		path = path.substring(5);
	}
	path = URLDecoder.decode(path, "UTF-8");
	//BufferedImage img = ImageIO.read(new File((new File(path).getParentFile().getPath()) +  File.separator + "folder" + File.separator + "yourfile.jpg"));
	
	InputStream in = new FileInputStream((new File(path).getParentFile().getPath()) + File.separator + filename);
	BufferedReader input = new BufferedReader(new InputStreamReader(in));
	 
	String sCurrentLine;
	
	StringBuilder liveflowscript = new StringBuilder();
	
	try {
		while ((sCurrentLine = input.readLine()) != null) {
			liveflowscript.append(sCurrentLine + "\n");
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return liveflowscript.toString();
	
}

	*/
 
}

