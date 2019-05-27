package com.epac.cap.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.Response;

/**
 * A Mock implementation of the Java Web Service ImpositionResponse. It does nothing and it's only purpose is to stub out web
 * service responses.
 * 
 * @author smithjac
 * 
 */
public class MockWebServiceResponse<T> implements Response<T> {

  /**
   * @see javax.xml.ws.Response#getContext()
   */
  @Override
  public Map<String, Object> getContext() {
    return new HashMap<String, Object>();
  }

  /**
   * @see java.util.concurrent.Future#cancel(boolean)
   */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.util.concurrent.Future#get()
   */
  @Override
  public T get() throws InterruptedException, ExecutionException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.util.concurrent.Future#isCancelled()
   */
  @Override
  public boolean isCancelled() {
    return false;
  }

  /**
   * @see java.util.concurrent.Future#isDone()
   */
  @Override
  public boolean isDone() {
    return true;
  }

}
