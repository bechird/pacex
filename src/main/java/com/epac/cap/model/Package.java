package com.epac.cap.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(value = Include.NON_NULL)
@Table(name = "Package" )
public class Package extends AuditableBean implements java.io.Serializable{

	private static final long serialVersionUID = 1L;


	private Long packageId; 

	private String typePackage; 

	private int quantity;

	private int count = 1;

	private Double weight;

	private String destination;

	private String label;

	private Set<PackageBook> pcbs;


	private Set<Package> packages;


	public Package(){
		//setTypePackage(PackageType.PALLET.getName());
	}

	//	public Package(PackageType type) {
	//		this.typePackage = type.getName(); 
	//	}

	//	@Column(name = "type")
	//	@Enumerated(EnumType.STRING)
	//	public PackageType getTypePackgae() {
	//		return typePackage;
	//	}

	public enum PackageType {
		SHRINK_WRAPPED("SHRINK_WRAPPED"),
		TOAD("TOAD"),
		PALLET("PALLET");

		private String name;
		private PackageType(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}


	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Set<Package> getPackages() {
		return packages;
	}

	@Column(name = "quantity")
	public int getQuantity() {

		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	@OneToMany(cascade = CascadeType.ALL ,fetch = FetchType.EAGER)
	public Set<PackageBook> getPcbs() {
		return pcbs;
	}

	@Column(name="destination")
	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	@Column(name="label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO)
	@Column(name = "packageId")
	public Long getPackageId() {
		return packageId;
	}

	public void setPackageId(long id) {
		this.packageId = id;
	}

	/**
	 * @return the typePackage
	 */
	@Column(name = "type", length = 55)
	public String getTypePackage() {
		return typePackage;
	}

	/**
	 * @param typePackage the typePackage to set
	 */
	public void setTypePackage(String typePackage) {
		this.typePackage = typePackage;
	}

	@Column(name="count")
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Column(name="weight")
	public Double getWeight() {

		return weight;
	}

	public void setPackages(Set<Package> packages) {
		this.packages = packages;
	}

	public void setPcbs(Set<PackageBook> pcbs) {
		this.pcbs = pcbs;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}	
		if (!(obj instanceof Package)) {
			return false;
		}
		Package that = (Package)obj;
		if(that.packageId != null && this.packageId != null && !that.packageId.equals(this.packageId)) return false;
			
			if(this.quantity != that.getQuantity())return false;
			if(this.count != that.getCount())return false;
			if(this.weight != that.getWeight())return false;
			if(this.pcbs.size() != that.getPcbs().size())return false;
			else{
				int i = 0;
				for(PackageBook pcb : this.pcbs){
					PackageBook ortherPcb = that.getPcbs().iterator().next();
                   if(pcb.getDepthQty() != ortherPcb.getDepthQty())return false;
                   if(pcb.getHeightQty() != ortherPcb.getHeightQty())return false;
                   if(pcb.getWidthQty() != ortherPcb.getWidthQty())return false;

                   i++;
				}
			}


		

       return true;
	}



}

