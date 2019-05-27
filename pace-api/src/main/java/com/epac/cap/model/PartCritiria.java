package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * PartCritiria class representing a critiria for the part bean
 */
@Entity
@Table(name = "part_Critiria")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class PartCritiria implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 775728345009062344L;
	private PartCritiriaId id;
	//private Critiria critiria;
	//private Part part;

	/**
	 * Default constructor
	 */
	public PartCritiria() {
		if(this.id == null){
			this.id = new PartCritiriaId();
		}
	}
	
	public PartCritiria(String partNum, String critiriaId) {
		this.id = new PartCritiriaId(partNum, critiriaId);
	}

	/**
	 * Constructor which sets the identity property
	 
	public PartCritiria(Critiria critiria, Part part) {
		this(null,critiria, part);
	}*/

	/**
	 * Constructor which sets the identity property
	 
	public PartCritiria(PartCritiriaId id, Critiria critiria, Part part) {
		this.id = id;
		if(this.id == null){
			this.id = new PartCritiriaId();
		}	
		this.critiria = critiria;
		if(this.critiria != null && this.critiria.getId() != null){
			this.id.setCritiriaId(this.critiria.getId());
		}
		this.part = part;
		if(this.part != null && this.part.getPartNum() != null){
			this.id.setPartNum(this.part.getPartNum());
		}
	}*/
	
	/**
	 * Accessor methods for id
	 *
	 * @return id  
	 */
	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "partNum", column = @Column(name = "Part_Num", nullable = false, length = 25)),
			@AttributeOverride(name = "critiriaId", column = @Column(name = "Critiria_Id", nullable = false, length = 25)) })
	public PartCritiriaId getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(PartCritiriaId id) {
		this.id = id;
	}

	/**
	 * Accessor methods for critiria
	 *
	 * @return critiria
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Critiria_Id", nullable = false, insertable = false, updatable = false)
	public Critiria getCritiria() {
		return this.critiria;
	}*/

	/**
	 * @param critiria the critiria to set
	 
	public void setCritiria(Critiria critiria) {
		this.critiria = critiria;
	}*/

	/**
	 * Accessor methods for part
	 *
	 * @return part  
	 

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Part_Num", nullable = false, insertable = false, updatable = false)
	public Part getPart() {
		return this.part;
	}*/

	/**
	 * @param part the part to set
	 
	public void setPart(Part part) {
		this.part = part;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof PartCritiria)) {
			return false;
		}
		PartCritiria other = (PartCritiria) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
