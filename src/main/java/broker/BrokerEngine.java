package broker;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.security.JaasAuthenticationPlugin;

public class BrokerEngine {

	public BrokerEngine()
	{
		startInternalBroker();
		startExternalBroker();
		
	}
	
	private void startExternalBroker()
	{
		try
		{
			
			BrokerService broker = new BrokerService();
			broker.setBrokerName("external");
			broker.setUseShutdownHook(true);
			broker.setUseJmx(false);
			//Add plugin
			//broker.setPlugins(new BrokerPlugin[]{new JaasAuthenticationPlugin()});
			//Add a network connection
			//NetworkConnector connector = broker.addNetworkConnector("static://"+"tcp://somehost:32005");
			//connector.setDuplex(true);
			broker.addConnector("tcp://0.0.0.0:32005");
			broker.start();
			
			/*
			BrokerService broker = new BrokerService();
			broker.addConnector("tcp://localhost:32005");
			broker.start();
			*/
		}
		catch(Exception ex)
		{
			System.out.println("BrokerEngine Error: " + ex.toString());
		}
	}
	
	private void startInternalBroker()
	{
		try
		{
			
			BrokerService broker = new BrokerService();
			broker.setBrokerName("internal");
			broker.setUseShutdownHook(true);
			broker.setUseJmx(false);
			//Add plugin
			//broker.setPlugins(new BrokerPlugin[]{new JaasAuthenticationPlugin()});
			//Add a network connection
			//NetworkConnector connector = broker.addNetworkConnector("static://"+"tcp://somehost:32005");
			//connector.setDuplex(true);
			broker.addConnector("vm://localhost");
			broker.start();
			
			/*
			BrokerService broker = new BrokerService();
			broker.addConnector("tcp://localhost:32005");
			broker.start();
			*/
		}
		catch(Exception ex)
		{
			System.out.println("BrokerEngine Error: " + ex.toString());
		}
	}
	
}
