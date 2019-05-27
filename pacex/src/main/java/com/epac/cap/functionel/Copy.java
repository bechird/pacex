package com.epac.cap.functionel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.epac.cap.config.NDispatcher;
import com.epac.cap.handler.WFSProgressHandler;
import com.epac.cap.model.WFSAction;
import com.epac.cap.utils.LogUtils;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

@Component
public class Copy extends WFSAction implements IAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ExecutorService executor;
	//private static String partNb;
	

	@Override
	public boolean handle(Object parameter) {
		   synchronized(this){//synchronized block  
			   List<String> parameters = (List<String>) parameter;
				String source = parameters.get(0);
				String destination = parameters.get(1);
				String partNb = parameters.get(2);
				try {
					FileUtils.copyFile(new File(source), new File(destination));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TryFire(partNb);
				return true;
			 }//end of the method  
		
	}

	@Override
	public boolean TryFire(String partNb) {
		NDispatcher.getDispatcher().publish(Topics.topic("cap/events/copy/done"), partNb);
		return true;
	}

	@Override
	public void subscribe() {
		TopicMatcher matcher = Topics.only("cap/events/copy");
		NDispatcher.getDispatcher().subscribe(matcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws IOException  {
				LogUtils.debug("Received: " + event.toString() + " calling the copy method");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							handle(event.getSource());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				executor.execute(task);
			}
		});
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub
		
	}
	
	public Copy() {
		executor = Executors.newFixedThreadPool(10);
		subscribe();
	}

}
