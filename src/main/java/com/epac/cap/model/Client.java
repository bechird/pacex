package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Client class representing a pacex Client (ex; EPAC, INTERFORUM...)
 */
@Entity
@Table(name = "client")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Client_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 155), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 255), name = "description")})
public class Client extends LookupItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6922643688900180605L;
	
	/**
	 * Default constructor
	 */
	public Client() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Client(String id, String name) {
		super(id, name);
	}

}
