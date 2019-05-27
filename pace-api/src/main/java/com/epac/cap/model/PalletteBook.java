package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "PalletteBook")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class PalletteBook extends AuditableBean implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private PalletteBooksId id;
	
	private PackageBook packageBook;
	
	private int quantity;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "palletteId", column = @Column(name = "pallette_Id", nullable = false)),
			@AttributeOverride(name = "packagePartId", column = @Column(name = "packagePartId", nullable = false, length = 25)) })
	public PalletteBooksId getId() {
		return id;
	}

	
	public void setId(PalletteBooksId id) {
		this.id = id;
	}


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "packagePartId", nullable = false, insertable = false, updatable = false)
	public PackageBook getPackageBook() {
		return packageBook;
	}

	public void setPackageBook(PackageBook packageBook) {
		this.packageBook = packageBook;
	}

	@Column(name = "quantity")
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	
 
}
