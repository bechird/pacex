package com.epac.cap.handler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.PropertyResolverUtils;
import org.springframework.stereotype.Service;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.utils.LogUtils;

/**
 * 
 *
 * @author Nabil
 *
 */
@Service
public class SshConnectionsManager {


	// 192.168.30.160
	// admin
	// Fr5-@dmin

	private static Logger logger = Logger.getLogger(SshConnectionsManager.class);

	private String nasAddress;
	private String nasLogin;
	private String nasPwd;
	private SshClient client = SshClient.setUpDefaultClient();

	
	private BlockingQueue<ClientSession> sessionsQueue;

	public SshConnectionsManager() {

		client.start();
		PropertyResolverUtils.updateProperty(client, ClientFactoryManager.HEARTBEAT_INTERVAL, TimeUnit.MINUTES.toMillis(30));
		PropertyResolverUtils.updateProperty(client, ClientFactoryManager.IDLE_TIMEOUT, TimeUnit.MINUTES.toMillis(30));
		
		//set sessions queue
		sessionsQueue = new ArrayBlockingQueue<ClientSession>(32);
		//initialize queue withn six parallel sessions
		for(int i=0; i<3;i++) {
			try {
				ClientSession session = checkSession(null);
				sessionsQueue.put(session);
			} catch (Exception e) {}
		}
		
	}
	
	public BlockingQueue<ClientSession> getSessionsQueue(){
		return sessionsQueue;
	}
	
	
	public String replacePath(String propertyName, String path) {
		
		Map<String, String> replacementMap = new HashMap<String, String>();
		
		String propertyValue = System.getProperty(propertyName);
		if(propertyValue == null)return path;
		
		Pattern pattern = Pattern.compile("(\\{.*?\\})");
		Matcher matcher = pattern.matcher(propertyValue);
		while (matcher.find()) {
		    String pair = matcher.group(1);
		    pair = pair.trim().replace("{", "").replace("}", "");
		    //split
		    String[] parts = pair.split(",");
		    if(parts.length < 2)return path;
		    replacementMap.put(parts[0].trim(), parts[1].trim());
		}
		
		for(String key : replacementMap.keySet()) {
			String value = replacementMap.get(key);
			path = path.replace(key, value);
		}
		
		return path;
	}	


	public ClientSession checkSession(ClientSession session) {
		// get a ssh session
		boolean sessionConnected = false;
		for (int i = 0; i < 2; i++) {

			try {
				session = getSession(session);
				sessionConnected = true;
				LogUtils.debug("SSh connection successful");
				break;
			} catch (Exception e) {
				LogUtils.debug("checkSession, SSh Session can't be created, exception :");
				LogUtils.debug(e.getMessage());
				LogUtils.debug("-----");
			}

		}

		if (!sessionConnected) {
			LogUtils.debug("SSh Session can't be created after 2 atempts,  exiting");
		}
		
		return session;
	}

	private ClientSession getSession(ClientSession session) throws Exception{

		if (session==null || session.isClosed()) {

			this.nasAddress = System.getProperty(ConfigurationConstants.NasAddress);
			this.nasLogin = System.getProperty(ConfigurationConstants.NasLogin);
			this.nasPwd = System.getProperty(ConfigurationConstants.NasPwd);
			
			logger.info("Session is closed, reconnecting to " + nasAddress + " with credentials " + nasLogin + "/" + nasPwd);
			
			try {
				ConnectFuture connectFuture = client.connect(nasLogin, nasAddress, 22);
				logger.info("create connectFuture success");
				connectFuture.await(3, TimeUnit.SECONDS);
				logger.info("create session ...");

				session = connectFuture.getSession();				

				session.addPasswordIdentity(nasPwd);
				logger.info("verify credencial  session ...");
				session.auth().verify(3, TimeUnit.SECONDS);
				logger.info("create session success.");
			} catch (IOException e) {
				throw e;
			}
	        
		}		
		
		return session;

	}
	
	


}
