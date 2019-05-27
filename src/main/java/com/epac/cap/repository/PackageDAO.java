package com.epac.cap.repository;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.epac.cap.model.Package;


@Repository
public class PackageDAO extends BaseEntityPersister{

	public void create(Package bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	public void update(Package bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	public Package read(long packageId) {
		return getEntityManager().find(Package.class, packageId);
	}
	
	public Package fetchPackageByPcbId(Long packageBookId){
		String sql = "SELECT * FROM `Package` WHERE packageId in(select `Package_packageId` FROM `Package_PackageBook` where `pcbs_packagePartId` = "+packageBookId+")";
		Query query = getEntityManager().createNativeQuery(sql,Package.class);
		return (Package)query.getResultList().get(0);
		
	}
}
