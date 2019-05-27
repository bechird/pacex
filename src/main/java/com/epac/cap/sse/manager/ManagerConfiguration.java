package com.epac.cap.sse.manager;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ManagerConfiguration {
     
	private PoolingHttpClientConnectionManager cm;
	private CloseableHttpClient httpClient = null;
	
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(100);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }
    
    
	@Bean
	public CloseableHttpClient sseHttpClient() {
		
		if(httpClient == null) {
			SSLConnectionSocketFactory socketFactory = null;
			
			try {
				
				SSLContext sslcontext = SSLContextBuilder
				        .create()
				        .loadTrustMaterial(new TrustSelfSignedStrategy()) 
				        .build();
				 
				 socketFactory = new SSLConnectionSocketFactory(sslcontext);
				 
			} catch (Exception e) {}			
		
			
			if(socketFactory != null) {
				Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
																		 .register("http", PlainConnectionSocketFactory.getSocketFactory())
																		 .register("https", socketFactory)
																		 .build();
				cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			}else {
				cm = new PoolingHttpClientConnectionManager();
			}
			
			cm.setMaxTotal(200);
			cm.setDefaultMaxPerRoute(100);
			SocketConfig sc = SocketConfig.custom()
				    .setSoTimeout(60000)
				    .build();

			cm.setDefaultSocketConfig(sc);
				
			httpClient = HttpClients.custom().setConnectionManager(cm).setConnectionManagerShared(true).build();			
		}
		
		return httpClient;
		
	}
	
	
}