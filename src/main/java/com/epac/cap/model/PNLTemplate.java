package com.epac.cap.model;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortComparator;

import com.epac.cap.handler.PNLTemplateLinesPerOrderingComparator;

/**
 * PNL Template: a class representing a PNL template in the system
 */
@Entity
@Table(name = "PNL_Template")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Template_Id", unique = true, nullable = false, length = 100), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 300), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 300), name = "description")})
public class PNLTemplate extends LookupItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -368465232219774789L;
	
	private SortedSet<PNLTemplateLine> templateLines = new TreeSet<PNLTemplateLine>(new PNLTemplateLinesPerOrderingComparator());
	
	/**
	 * 
	 */
	public PNLTemplate() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public PNLTemplate(String id, String name) {
		super(id, name);
	}
	
	public PNLTemplate(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the templateLines
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "templateId", cascade = CascadeType.ALL, orphanRemoval = true)
	@SortComparator(PNLTemplateLinesPerOrderingComparator.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public SortedSet<PNLTemplateLine> getTemplateLines() {
		return templateLines;
	}

	/**
	 * @param templateLines the templateLines to set
	 */
	public void setTemplateLines(SortedSet<PNLTemplateLine> templateLines) {
		this.templateLines = templateLines;
	}
	
}
