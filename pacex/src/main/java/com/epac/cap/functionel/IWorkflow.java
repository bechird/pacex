package com.epac.cap.functionel;

public interface IWorkflow
{
	public boolean handler(Object parameter);

	public boolean TryFire();

	public void subscribe();

	public void unsubscribe();

}