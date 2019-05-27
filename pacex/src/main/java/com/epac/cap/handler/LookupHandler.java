package com.epac.cap.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.PNLTemplateLine;
import com.epac.cap.model.Preference;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.LookupSearchBean;

import java.util.List;

/**
 * Lookup handler class
 * 
 */
@Service
public class LookupHandler {
	
  @Autowired
  private LookupDAO lookupDAO;
  
  private static Logger logger = Logger.getLogger(LookupHandler.class);

  /**
   * create lookup item
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public void create(LookupItem item) throws PersistenceException {
    try {
      getLookupDAO().create(item);
    } catch (Exception e) {
      item.setId(null);
      logger.error("Error occurred creating a Lookup Item : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * create PNL template line
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public void createTmpLine(PNLTemplateLine item) throws PersistenceException {
    try {
    	if (item.getFontItalic() ==null)
    		item.setFontItalic(false);
    	if (item.getFontBold() ==null)
    		item.setFontBold(false);
        getLookupDAO().createTmpLine(item);
    } catch (Exception e) {
      item.setId(null);
      logger.error("Error occurred creating a PNL Template Line: " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * update lookup item
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public void update(LookupItem item) throws PersistenceException {
    try {
      getLookupDAO().update(item);
    } catch (Exception e) {
    	logger.error("Error occurred updating a Lookup Item : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * update template line
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public void updateTmpLine(PNLTemplateLine item) throws PersistenceException {
    try {
    	if (item.getFontItalic() ==null)
    		item.setFontItalic(false);
    	if (item.getFontBold() ==null)
    		item.setFontBold(false);
        getLookupDAO().updateTmpLine(item);
    } catch (Exception e) {
    	logger.error("Error occurred updating a template Line Item : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }

  /**
   * delete lookup item
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public <L extends LookupItem> boolean delete(String id, Class<L> itemClass) throws PersistenceException {
    try {
      return getLookupDAO().delete(id, itemClass);
    } catch (Exception e) {
    	logger.error("Error occurred deleting a Lookup Item : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * delete Template Line
   */
  @Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
  public boolean deleteTmpLine(Integer tmpLineId) throws PersistenceException {
    try {
      return getLookupDAO().deleteTmpLine(tmpLineId);
    } catch (Exception e) {
    	logger.error("Error occurred deleting a template line : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }

  /**
   * read lookup item
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public <L extends LookupItem> L read(String id, Class<L> itemClass) throws PersistenceException {
    try {
      return getLookupDAO().read(id, itemClass);
    } catch (Exception e) {
    	logger.error("Error occurred reading a Lookup Item : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * read PNL Template Line
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public PNLTemplateLine readTmpLine(Integer tmpLineId) throws PersistenceException {
    try {
      return getLookupDAO().readTmpLine(tmpLineId);
    } catch (Exception e) {
    	logger.error("Error occurred reading a template line: " + e.getMessage());
      throw new PersistenceException(e);
    }
  }

  /**
   * clear cache
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public void clearCache () throws PersistenceException {
    try {
       getLookupDAO().clearCacheInternal();
    } catch (Exception e) {
    	logger.error("Error occurred clearing cache : " + e.getMessage());
      throw new PersistenceException(e);
    }
  }
  
  /**
   * read all lookup items
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public <L extends LookupItem> List<L> readAll(Class<L> itemClass) throws PersistenceException {
    return this.readAll(null, null, itemClass);
  }
  
  /**
   * read all (List<String> idList, Class<L> itemClass)
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public <L extends LookupItem> List<L> readAll(List<String> idList, Class<L> itemClass) throws PersistenceException {
	  return this.readAll(idList, null, itemClass);
  }
  
  /**
   * read all (LookupSearchBean lsb, Class<L> itemClass)
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public <L extends LookupItem> List<L> readAll(LookupSearchBean lsb, Class<L> itemClass) throws PersistenceException {
	  return this.readAll(null, lsb, itemClass);
  }

  /**
   * read all (List<String> idList, LookupSearchBean lsb, Class<L> itemClass)
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public <L extends LookupItem> List<L> readAll(List<String> idList, LookupSearchBean lsb, Class<L> itemClass) throws PersistenceException {
	  try {
		  return getLookupDAO().readAll(idList, lsb, itemClass);
	  } catch (Exception e) {
		  logger.error("Error occurred reading Lookup Items : " + e.getMessage());
	      throw new PersistenceException(e);
	  }
  }
  
  /**
   * read all Preference subjects
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public List<String> readPrefSubjects(String groupId, String clientId) throws PersistenceException {
	  try {
		  return getLookupDAO().readPrefSubjects(groupId, clientId);
	  } catch (Exception e) {
		  logger.error("Error occurred reading Lookup Items : " + e.getMessage());
	      throw new PersistenceException(e);
	  }
  }
  
  /**
   * read all Preference groups
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public List<String> readPrefGroups(String clientId) throws PersistenceException {
	  try {
		  return getLookupDAO().readPrefGroups(clientId);
	  } catch (Exception e) {
		  logger.error("Error occurred reading Lookup Items : " + e.getMessage());
	      throw new PersistenceException(e);
	  }
  }
  
  /**
   * read the grouping of the preference based on the subject
   */
  @Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
  public String getGroupBySubject(String prefSubject) throws PersistenceException {
	  try {
		  String result = "";
		  LookupSearchBean lsb = new LookupSearchBean();
		  lsb.setPrefSubject(prefSubject);
		  List<Preference> listOfPrefs = readAll(lsb, Preference.class);
		  for(Preference p : listOfPrefs){
			  if(p.getGroupingValue() != null && !p.getGroupingValue().isEmpty()){
				  result = p.getGroupingValue();
				  break;
			  }
		  }
		  return result;
	  } catch (Exception e) {
		  logger.error("Error occurred reading group : " + e.getMessage());
	      throw new PersistenceException(e);
	  }
  }
  
	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}
	
	/**
	 * @param lookupDAO the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

}
