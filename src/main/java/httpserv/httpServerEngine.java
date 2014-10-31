/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package httpserv;


import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class httpServerEngine implements Runnable{

	
	public static String ipAddress;
	
	public httpServerEngine() throws IOException, InterruptedException
	{
	
	}
	public void run()
	{
		System.out.println("Graphs UP...");
        try {
			thed();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/*
    	FPSUpdater c = new FPSUpdater("FPS_TOTAL","128.163.2.80");
    	Thread t2 = new Thread(c);
    	t2.start();
    	*/
    	
    	
        try {
			HttpServer httpServer = startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /*
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...",
                BASE_URI, BASE_URI));
        */
        //System.in.read();
        //System.out.println("Building GraphDB Cache...");
        //gs.buildCache();
        //System.out.println("GraphDB Cache Complete...");
        
        while(true)
        {
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        //httpServer.stop();
	}
    private static int getPort(int defaultPort) {
        String port = System.getProperty("jersey.test.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;        
    } 
    
    private static URI getBaseURI() {
    	
    	InetAddress address = null;
    	try {
    		address = InetAddress.getLocalHost();
    	    ipAddress = address.getHostAddress();
    	} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
		   e.printStackTrace();
    	
		}  
    	
    	return UriBuilder.fromUri("http://" + ipAddress + "/").port(getPort(32000)).build();
        
    }

    public static final URI BASE_URI = getBaseURI();

       
    private void thed() throws IOException
    {
    	ClassLoader classloader = this.getClass().getClassLoader(); 
    	   Enumeration<URL> urls = classloader.getResources("httpserv");
    	   while(urls.hasMoreElements()){
    		   URL param = (URL) urls.nextElement();
    		   System.out.println(param);
    		   }
    }
    
    
    protected static HttpServer startServer() throws IOException {
        System.out.println("Starting grizzly...");
        
        //URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        URI baseUri = getBaseURI();
        ResourceConfig config = new ResourceConfig(webREST.class);
        //config.register(webGUI.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        //return GrizzlyServerFactory.createHttpServer(BASE_URI);
        //return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
        return server;
    }
    
    //public static void main(String[] args) throws IOException, InterruptedException {
    	
    
    //}
}
