package com.epac.cap.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.PackageBookHandler;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.model.PalletteBooksId;
import com.epac.cap.utils.AoDataParser;
import com.epac.cap.model.Pallette.PalletteStatus;


@Repository
public class PalletteDao extends BaseEntityPersister{

   @Autowired
   PackageBookHandler packageBookHandler ;

	public void create(Pallette bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	public void update(Pallette bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	public void update(PalletteBook bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	public void create(PalletteBook bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	public boolean delete(Pallette bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}
	public boolean deletePalletteBook(PalletteBook bean) {		
		if (bean != null){
			getEntityManager().remove(read(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	public Pallette read(Long palletteId) {
		return getEntityManager().find(Pallette.class, palletteId);
	}
	public PalletteBook read(PalletteBooksId id) {
		return getEntityManager().find(PalletteBook.class, id);
	}
	@SuppressWarnings("unchecked")
	public List<Pallette> readAll(PalletteSearchBean searchBean) {
		Criteria criteria = createCriteria(Pallette.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getId() != null){
				criteria.add(Restrictions.eq("id", searchBean.getId()));
			}
			if(searchBean.getCustomer()!= null){
				criteria.add(Restrictions.eq("customer", searchBean.getCustomer()));
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));
			}

			if(searchBean.getStartDate() != null){
				criteria.add(Restrictions.eq("startDate", searchBean.getStartDate()));
			}
			if(searchBean.getEndDate() != null){
				criteria.add(Restrictions.eq("endDate", searchBean.getEndDate()));
			}
			if(searchBean.getPalletteName() != null && !searchBean.getPalletteName().isEmpty()){
				criteria.add(Restrictions.eq("palletteName", searchBean.getPalletteName()));		
			}
			if(searchBean.getStatusPallette() != null){
				criteria.add(Restrictions.eq("statusPallette", searchBean.getStatusPallette()));		
			}
			if(searchBean.getId() != null){
				criteria.add(Restrictions.eq("id", searchBean.getId()));
			}

		}else{
			searchBean = new PalletteSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Pallette> l=criteria.list();
		return l;
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void updateQtyOfPcb(long packageBookId,long palletteId,int qty){
		

		Pallette palette = read(palletteId);
		if(palette == null)return;
		boolean contain = false;
		for(PalletteBook pb: palette.getBooks()){
			if(pb.getPackageBook().getPackagePartId() == packageBookId){
				contain = true;
				int oldQty = pb.getQuantity();
				int newQty = oldQty + qty;
				pb.setQuantity(newQty);
				Integer delivered = pb.getPackageBook().getDelivered();
				if(delivered == null)delivered = 0;
				delivered += qty;
				pb.getPackageBook().setDelivered(delivered);
				try {
					packageBookHandler.update(pb.getPackageBook());
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				update(pb);
				
			}
		}
		
		
		if(!contain){
			PackageBook pBook = packageBookHandler.read(packageBookId);
			PalletteBook palletteBook = new PalletteBook();
			palletteBook.setPackageBook(pBook);
			PalletteBooksId id = new PalletteBooksId();
			id.setPalletteId(palletteId);
			id.setPackagePartId(pBook.getPackagePartId());
			palletteBook.setId(id);
			palletteBook.setCreatedDate(new Date());
			palletteBook.setLastUpdateDate(new Date());
			palletteBook.setCreatorId("Sabrine");
			palletteBook.setLastUpdateId("Sabrine");
			palletteBook.setQuantity(qty);
			create(palletteBook);
			PalletteBook pb = read(id);
			Integer delivered = pb.getPackageBook().getDelivered();
			if(delivered == null)delivered = 0;
			delivered += qty;
			pb.getPackageBook().setDelivered(delivered);
			try {
				packageBookHandler.update(pb.getPackageBook());
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			palette.getBooks().add(pb);
			
		}
		update(palette);
		
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void editQtyOfPcb(long packageBookId,long palletteId,int qty){
		

		Pallette palette = read(palletteId);
		if(palette == null)return;
		for(PalletteBook pb: palette.getBooks()){
			if(pb.getPackageBook().getPackagePartId() == packageBookId){
				int oldQty = pb.getQuantity();
				int diffQty = qty - oldQty  ;
				pb.setQuantity(qty);
				Integer delivered = pb.getPackageBook().getDelivered();
				if(delivered == null)delivered = 0;
				delivered += diffQty;
				pb.getPackageBook().setDelivered(delivered);
				try {
					packageBookHandler.update(pb.getPackageBook());
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				update(pb);
				
			}
		}
	}
	 public List<Integer> fetchMaxCountOf(){
		 String sql ="SELECT MAX(count) FROM Pallette";

			Query query = getEntityManager().createNativeQuery(sql);
			return (List<Integer>)query.getResultList();
	 }
	 @SuppressWarnings("unchecked")
		public List<Pallette> readAllPalletteComplete(PalletteSearchBean searchBean) {
			Criteria criteria = createCriteria(Pallette.class);
			if(searchBean != null){
				super.addAuditableCriteria(criteria, searchBean);
				if(searchBean.getId() != null){
					criteria.add(Restrictions.eq("id", searchBean.getId()));
				}
			
				if(searchBean.getStatusPallette() != null){
					criteria.add(Restrictions.eq("statusPallette", searchBean.getStatusPallette()));		
				}
				if(searchBean.getBlNumber() != null){
					criteria.add(Restrictions.eq("blNumber", searchBean.getBlNumber()));
				}
				if(searchBean.getDestination() != null){
					criteria.add(Restrictions.ilike("destination", searchBean.getDestination(), MatchMode.ANYWHERE));
				}				
				if(searchBean.getDelivredDate() != null){
					criteria.add(Restrictions.eq("delivredDate", searchBean.getDelivredDate()));
				}

			}else{
				searchBean = new PalletteSearchBean();
			}
			if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
				criteria.setMaxResults(searchBean.getMaxResults());
			}
			
			if(searchBean.getResultOffset() != null){
				criteria.setFirstResult(searchBean.getResultOffset());
			}
			
			addOrderBy(searchBean.getOrderByList(),criteria);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
			List<Pallette> l=criteria.list();
			return l;
		}
	 public List<PalletteBook> fetchPalletteBokkByPcbId(Long pcbId){
		 String sql ="SELECT * FROM PalletteBook where packagePartId = "+ pcbId;

			Query query = getEntityManager().createNativeQuery(sql,PalletteBook.class);
			return (List<PalletteBook>)query.getResultList();
	 }

	public List<Pallette> fetchPalletteWithoutBL() {
		 String sql ="SELECT * FROM Pallette where blNumber is null and status = 'COMPLETE'";
			Query query = getEntityManager().createNativeQuery(sql,Pallette.class);
		return (List<Pallette>)query.getResultList();
	}
	
	public List<Pallette> fetchPalletteToShipToday() {
		Criteria criteria = createCriteria(Pallette.class);
	
		Calendar startDateTime = new GregorianCalendar();
		startDateTime.set(Calendar.HOUR_OF_DAY, 0);
		startDateTime.set(Calendar.MINUTE, 0);
		startDateTime.set(Calendar.SECOND, 0);
		startDateTime.set(Calendar.MILLISECOND, 0);

		
		Calendar endDateTime = new GregorianCalendar();
		endDateTime.set(Calendar.HOUR_OF_DAY, 23);
		endDateTime.set(Calendar.MINUTE, 59);
		endDateTime.set(Calendar.SECOND, 59);
		endDateTime.set(Calendar.MILLISECOND, 0);
		
		criteria.add(Restrictions.eq("statusPallette", PalletteStatus.COMPLETE));
		criteria.add(Restrictions.isNull("blNumber"));
		super.addDateRangeCriteria(criteria,"delivredDate", startDateTime.getTime(), endDateTime.getTime());
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);  
		
		List<Pallette> l=criteria.list();
		return l;
		
	}
	
	/** 
	 * count
	 */	 
	public List<Pallette> fullSearch(String query, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Pallette.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		Criterion c = AoDataParser.fullSearchCriterionBuilder("id", "eq", "long", query);
		if(c != null)criterions.add(c);
		
		
		c = AoDataParser.fullSearchCriterionBuilder("blNumber", "eq", "integer", query);
		if(c != null)criterions.add(c);

		c = AoDataParser.fullSearchCriterionBuilder("destination", "ilike", "string", query);
		if(c != null)criterions.add(c);
		
		c = AoDataParser.fullSearchCriterionBuilder("delivredDate", "eq", "date", query);
		if(c != null)criterions.add(c);
		
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Pallette> l=criteria.list();
		return l;
		
	}
	
}