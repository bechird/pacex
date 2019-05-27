package com.epac.cap.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OrderBl {
	
	
	Long id;
	
    BonLivraison bonLivraison;
    
    int qty;
    
    @OneToOne(fetch = FetchType.EAGER)
	public BonLivraison getBonLivraison() {
		return bonLivraison;
	}
	public void setBonLivraison(BonLivraison bonLivraison) {
		this.bonLivraison = bonLivraison;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
    
    
}
