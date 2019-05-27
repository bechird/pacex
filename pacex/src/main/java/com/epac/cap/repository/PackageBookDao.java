package com.epac.cap.repository;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.epac.cap.model.PackageBook;
import com.epac.cap.model.Pallette;


@Repository
public class PackageBookDao extends BaseEntityPersister{
	
	public void create(PackageBook bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	public void update(PackageBook bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	public PackageBook read(long packagePartId) {
		return getEntityManager().find(PackageBook.class, packagePartId);
	}


	public Integer fectchOrder(long packageBookId){
		String sql = "select orderId from OrderPackages where packageId in "+
	"(Select Package_packageId from Package_Package where Packages_packageId = "+
				" (Select Package_packageId from Package_PackageBook where Pcbs_packagePartId= "+packageBookId+"))";
		Integer result = null;
		Query query = getEntityManager().createNativeQuery(sql);
		try{
		 result = (Integer)query.getSingleResult();
		}catch (NoResultException nre){
			//Ignore this because as per your logic this is ok!
		}
		return result;
	}
	public List<Pallette> fetchPalletteByPcb(long packageBookId){
		String sql = "select * from Pallette where id in "+
	"(Select pallette_Id from PalletteBook where packagePartId = "+packageBookId+")";
		
		Query query = getEntityManager().createNativeQuery(sql,Pallette.class);
		List<Pallette> result = (List<Pallette>)query.getResultList();
		return result;
	}

	
}
