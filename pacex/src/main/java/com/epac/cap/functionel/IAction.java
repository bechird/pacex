package com.epac.cap.functionel;

public interface IAction<E> {
	
	public boolean handle(E parameter);

	public boolean TryFire(String partNb);

	public void subscribe();

	public void unsubscribe();

}