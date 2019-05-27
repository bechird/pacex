package com.epac.cap.repository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.mycila.event.api.topic.Topics;

/**
 * Interacts with Order data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class OrderDAO extends BaseEntityPersister{

	/** 
	 * creates the bean
	 */	 
	@Transactional
	public void create(Order bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * creates the orderPart bean
	 */	 
	public void createOrderPart(OrderPart bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	/** 
	 * updates the orderPart bean
	 */	 
	public void updateOrderPart(OrderPart bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}	
	
	/** 
	 * updates the bean
	 */	 
	public void update(Order bean) {
		Order originalBean = read(bean.getOrderId());
		if(originalBean != null && !originalBean.getStatus().equals(bean.getStatus())){
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/update/productionOrder"), bean);
		}
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Order bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getOrderId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	public boolean deleteOrderPart(OrderPart bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(readOrderPart(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}
	
	/** 
	 * reads the order part bean
	 */	 
	public OrderPart readOrderPart(Long orderPartId) {
		return getEntityManager().find(OrderPart.class, orderPartId);
	}
	
	/** 
	 * reads the bean
	 */	 
	public Order read(Integer orderId) {
		return getEntityManager().find(Order.class, orderId);
	}
	public Order readByOrderNum(String orderNum) {
		OrderSearchBean orderSearchBean = new OrderSearchBean();
		orderSearchBean.setOrderNumExact(orderNum);
		List<Order> orders = readAll(orderSearchBean);
		if(orders!= null && orders.size() >0) return orders.get(0); 
		return null;
	}
	
	/** 
	 * reads how many erroneous erros are present
	 */	 
	public int getErrorStatusCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from order_T where status = 'ERROR'";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Order> readAll(OrderSearchBean searchBean) {
		Criteria criteria = createCriteria(Order.class);
		
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getOrderId() != null){
				criteria.add(Restrictions.eq("orderId", searchBean.getOrderId()));
			}	
			if(searchBean.getOrderIdDiff() != null){
				criteria.add(Restrictions.ne("orderId", searchBean.getOrderIdDiff()));
			}
			if(!StringUtils.isBlank(searchBean.getOrderNum())){							
				criteria.add(Restrictions.ilike("orderNum", searchBean.getOrderNum(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getOrderNumExact())){							
				criteria.add(Restrictions.eq("orderNum", searchBean.getOrderNumExact()));		
			}
			if(!StringUtils.isBlank(searchBean.getStatus())){
				criteria.add(Restrictions.eq("status", searchBean.getStatus()));		
			}
			super.addDateRangeCriteria(criteria,"dueDate",searchBean.getDueDateFrom(),searchBean.getDueDateTo());
			
			if(!StringUtils.isBlank(searchBean.getPriorityLevel())){
				criteria.add(Restrictions.eq("priority", searchBean.getPriorityLevel()));
			}	
			if(!StringUtils.isBlank(searchBean.getNotes())){							
				criteria.add(Restrictions.ilike("notes", searchBean.getNotes(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getSource())){							
				criteria.add(Restrictions.ilike("source", searchBean.getSource(), MatchMode.ANYWHERE));		
			}
			
			criteria.createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN);
			if(searchBean.getCustomerId() != null){
				criteria.add(Restrictions.eq("c.customerId", searchBean.getCustomerId()));
			}
			if(searchBean.getPartNumbers() != null && !searchBean.getPartNumbers().isEmpty()){
				criteria.createAlias("orderParts", "orderParts", CriteriaSpecification.LEFT_JOIN);
				criteria.add(Restrictions.in("orderParts.part.partNum", searchBean.getPartNumbers()));
			}			
			if(!StringUtils.isBlank(searchBean.getEmail())){					
				criteria.add(Restrictions.ilike("c.email", searchBean.getEmail(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getSearchOrderIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Order_Id LIKE '"+searchBean.getSearchOrderIdPart()+"%' "));
			}
			if(!StringUtils.isBlank(searchBean.getFullName())){
				Criterion c1 = Restrictions.ilike("c.firstName", searchBean.getFullName(), MatchMode.ANYWHERE);
				Criterion c2 = Restrictions.ilike("c.lastName", searchBean.getFullName(), MatchMode.ANYWHERE);
				criteria.add( Restrictions.or(c1, c2) );
			}
			if(searchBean.getReceivedDateExact() != null){				
				super.addDateRangeCriteria(
						criteria,"recievedDate", 
						atStartOfDay(searchBean.getReceivedDateExact()),
						atEndOfDay(searchBean.getReceivedDateExact())
						);
			}
			if(searchBean.getDueDateExact() != null){
				super.addDateRangeCriteria(
						criteria,"dueDate", 
						atStartOfDay(searchBean.getDueDateExact()),
						atEndOfDay(searchBean.getDueDateExact())
						);
			}			
			if(!StringUtils.isBlank(searchBean.getClientId())){
				criteria.add(Restrictions.eq("clientId", searchBean.getClientId()));
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new OrderSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		
		//for pagination
		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		if(searchBean.isListing()){
			criteria.setFetchMode("customer",  FetchMode.SELECT);			
			criteria.setFetchMode("orderParts", FetchMode.SELECT);
			criteria.setFetchMode("orderPackages", FetchMode.SELECT);			
		}
		
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");

		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Order> l=criteria.list();
		return l;
	}
	
	public List<Order> searchIsbnAndQuantity(String isbn, String quantity, OrderSearchBean searchBean){
		Criteria criteria = createCriteria(Order.class);	
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Order> result=criteria.list();
		
		
		if(!StringUtils.isBlank(searchBean.getSearchIsbn())){
			List<Order> filtered = new ArrayList<Order>();
			
			for(Order order : result) {
				
				try {
					if(order.getOrderPart().getPart().getIsbn().startsWith(searchBean.getSearchIsbn())){
						filtered.add(order);
					}
				} catch (Exception e) {}
				
			}
			
			result.clear();
			result.addAll(filtered);
		}
		
		if(!StringUtils.isBlank(searchBean.getSearchQuantity())){
			List<Order> filtered = new ArrayList<Order>();
			
			
			for(Order order : result) {				
				
				try {
					String q = String.valueOf( order.getOrderPart().getQuantity() );
					if(q.startsWith(searchBean.getSearchQuantity())){
						filtered.add(order);
					}
				} catch (Exception e) {}
				
				
			}
			
			result.clear();
			result.addAll(filtered);
		}
		
		int end = searchBean.getResultOffset()+searchBean.getMaxResults();
		return result.subList(searchBean.getResultOffset(), end > result.size() ? result.size() : end );
	}
	
	public List<Order> fetchOrderFinishing(){
		String sql = "select * from order_T o where o.Status in('ONPROD','COMPLETE') and o.Order_Id in(SELECT j.order_Id FROM `job` j WHERE j.Status in('RUNNING','COMPLETE','COMPLETE_PARTIAL') and j.Station_Id = 'BINDER' || j.Station_Id = 'SHIPPING')";
		Query query = getEntityManager().createNativeQuery(sql,com.epac.cap.model.Order.class);
		return (List<Order> )query.getResultList();
	}
	
	
	
	public List<Order> fetchOrderFinishingOfInterforum(String interSiren){
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String d = format.format(cal.getTime());
		String sql = "select * from order_T o where o.company_id = '"+interSiren
				    + "' and  o.completeDate = '"+ d  + "' ";
		System.out.println(sql);
				Query query = getEntityManager().createNativeQuery(sql,com.epac.cap.model.Order.class);
		return (List<Order> )query.getResultList();
	}
	public List<Order> fetchOrderInPROD(){
		String sql = "select * from order_T o where o.Status = 'ONPROD' and o.Order_Id in ( select orderId from OrderPackages where packageId in "+
				"(Select Package_packageId from Package_Package where Packages_packageId in "+
							" (Select Package_packageId from Package_PackageBook where Pcbs_packagePartId in (select packagePartId from PalletteBook  ))))";
					;
		Query query = getEntityManager().createNativeQuery(sql,com.epac.cap.model.Order.class);
		return (List<Order> )query.getResultList();
	}
	
	public List<Order> fetchOrderInPRODPaginate(Integer pageLength, Integer offset, OrderBy orderBy, String searchClause){
		String sql = "select * from order_T o where o.Status = 'ONPROD' and o.Order_Id in ( select orderId from OrderPackages where packageId in "+
				"(Select Package_packageId from Package_Package where Packages_packageId in "+
							" (Select Package_packageId from Package_PackageBook where Pcbs_packagePartId in (select packagePartId from PalletteBook  ))))";
		
		if(searchClause != null) {
			sql = sql  + searchClause;
		}
		
		sql = sql  + " order by " + orderBy.getName() + " " + orderBy.getDirection();
				
		if(offset != null) {
			sql = sql  + " limit " + String.valueOf(offset) + "," + String.valueOf(pageLength);
		}
				
			
		Query query = getEntityManager().createNativeQuery(sql,com.epac.cap.model.Order.class);
		return (List<Order> )query.getResultList();
	}
	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from order_T";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	/** 
	 * count
	 */	 
	public List<Order> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Order.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.eq("orderId", Integer.valueOf(word));
			criterions.add(c1);
		} catch (NumberFormatException e) {}
		
		try {
			Criterion c2 = Restrictions.ilike("orderNum", word, MatchMode.ANYWHERE);
			criterions.add(c2);
		} catch (NumberFormatException e) {}
		
		
		/*try {
			Criterion c3 = Restrictions.ilike("customer.email", word, MatchMode.START);
			criterions.add(c3);
		} catch (NumberFormatException e) {}

		
		try {
			Criterion c4 = Restrictions.ilike("customer.fullName", word, MatchMode.START);
			criterions.add(c4);
		} catch (NumberFormatException e) {}
		
		
		try {
			Criterion c5 = Restrictions.ilike("orderPart.part.isbn", word, MatchMode.START);
			criterions.add(c5);
		} catch (NumberFormatException e) {}*/
		

		
		try {
			Criterion c6 = Restrictions.ilike("source", word, MatchMode.ANYWHERE);
			criterions.add(c6);
		} catch (Exception e) {}

		/*try {
			Criterion c7 = Restrictions.eq("orderPart.quantity", Integer.valueOf(word));
			criterions.add(c7);
		} catch (Exception e) {}*/		

		try {
			Criterion c8 = Restrictions.ilike("priority", word, MatchMode.ANYWHERE);
			criterions.add(c8);
		} catch (Exception e) {}

		try {
			Criterion c9 = Restrictions.ilike("status", word, MatchMode.ANYWHERE);
			criterions.add(c9);
		} catch (Exception e) {}
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Order> l=criteria.list();
		return l;
		
	}
	
	
	public Date atStartOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
	    return localDateTimeToDate(startOfDay);
	}

	public Date atEndOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
	    return localDateTimeToDate(endOfDay);
	}

	private LocalDateTime dateToLocalDateTime(Date date) {
	    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private Date localDateTimeToDate(LocalDateTime localDateTime) {
	    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	
}