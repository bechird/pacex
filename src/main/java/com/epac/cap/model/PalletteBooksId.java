package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PalletteBooksId implements java.io.Serializable {
	
	private static final long serialVersionUID = 1641233114861969581L;
	private Long palletteId;
	private Long packagePartId;
	
	@Column(name = "pallette_Id")
	public Long getPalletteId() {
		return palletteId;
	}
	public void setPalletteId(Long palletteId) {
		this.palletteId = palletteId;
	}
	@Column(name = "packagePartId")
	public Long getPackagePartId() {
		return packagePartId;
	}
	public void setPackagePartId(Long packageBookId) {
		this.packagePartId = packageBookId;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof PalletteBooksId))
			return false;
		PalletteBooksId castOther = (PalletteBooksId) other;

		return (this.getPalletteId() == castOther.getPalletteId())
				&& ((this.getPackagePartId() == castOther.getPackagePartId()) || (this.getPackagePartId()!= null
						&& castOther.getPackagePartId() != null && this.getPackagePartId().equals(castOther.getPackagePartId())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = (int) (37 * result + this.getPalletteId());
		result = 37 * result + (getPackagePartId() == null ? 0 : this.getPackagePartId().hashCode());
		return result;
	}
	
}
