package com.epac.cap.functionel;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.epac.cap.config.NDispatcher;
import com.epac.cap.model.WFSAction;
import com.epac.cap.utils.LogUtils;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

public class Move extends WFSAction implements IAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean handle(Object parameter) {
		List<String> parameters = (List<String>) parameter;
		File newFile = new File(parameters.get(0));
		File oldFile = new File(parameters.get(1));
		if (oldFile.exists()){
			try {
				FileUtils.moveFile(oldFile, newFile);
				TryFire(null);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		} else return false;
	}

	@Override
	public boolean TryFire(String partNb) {
		NDispatcher.getDispatcher().publish(Topics.topic("cap/events/move/done"), true);
		return true;
	}

	@Override
	public void subscribe() {
		TopicMatcher matcher = Topics.only("cap/events/move");//, "cap/events/part/done");// Topics.only("cap/events/download").or(Topics.topics("cap/events/part/done"));//.or(Topics.topics("app/events/swing/fields/**"));
		NDispatcher.getDispatcher().subscribe(matcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the move method");
				handle(event.getSource());
			}
		});
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub
		
	}
	
	public Move() {
		subscribe();
	}

}
