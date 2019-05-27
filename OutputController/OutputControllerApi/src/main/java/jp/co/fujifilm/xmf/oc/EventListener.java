package jp.co.fujifilm.xmf.oc;

import jp.co.fujifilm.xmf.oc.model.Event;

public interface EventListener {
	public void handleEvent(Event event);
}
