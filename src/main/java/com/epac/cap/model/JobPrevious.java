package com.epac.cap.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class used to gather the data needed for a job to find previous job's data; used on the binder to display the status 
 * of the cover and text parts; or on other stations to display data about the job from previous station
 *
 */
public class JobPrevious {
	
	private String machineId;
	private Integer currentJobQtyNeeded;
	private Boolean bindingStage;
	
	private String bookPrevStation;
	private String coverPrevStation;
	private String textPrevStation;
	
	private String bookStatus;
	private String coverStatus;
	private String textStatus;
	
	private Set<String> bookLocation = new HashSet<String>();
	private Set<String> coverLocation = new HashSet<String>();
	private Set<String> textLocation = new HashSet<String>();
	
	private List<LoadTag> bookLoadtags = new ArrayList<LoadTag>();
	private List<LoadTag> coverLoadtags = new ArrayList<LoadTag>();
	private List<LoadTag> textLoadtags = new ArrayList<LoadTag>();
	
	private float bookQuantityReceived;
	private float coverQuantityReceived;
	private float textQuantityReceived;
	
	private String loadTags;
	private String cartNumbers;
	
	/**
	 * @return the machineId
	 */
	public String getMachineId() {
		return machineId;
	}
	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	
	/**
	 * @return the bindingStage
	 */
	public Boolean getBindingStage() {
		return bindingStage;
	}
	/**
	 * @param bindingStage the bindingStage to set
	 */
	public void setBindingStage(Boolean bindingStage) {
		this.bindingStage = bindingStage;
	}
	/**
	 * @return the coverPrevStation
	 */
	public String getCoverPrevStation() {
		return coverPrevStation;
	}
	/**
	 * @param coverPrevStation the coverPrevStation to set
	 */
	public void setCoverPrevStation(String coverPrevStation) {
		this.coverPrevStation = coverPrevStation;
	}
	/**
	 * @return the textPrevStation
	 */
	public String getTextPrevStation() {
		return textPrevStation;
	}
	/**
	 * @param textPrevStation the textPrevStation to set
	 */
	public void setTextPrevStation(String textPrevStation) {
		this.textPrevStation = textPrevStation;
	}
	/**
	 * @return the coverStatus
	 */
	public String getCoverStatus() {
		return coverStatus;
	}
	/**
	 * @param coverStatus the coverStatus to set
	 */
	public void setCoverStatus(String coverStatus) {
		this.coverStatus = coverStatus;
	}
	/**
	 * @return the textStatus
	 */
	public String getTextStatus() {
		return textStatus;
	}
	/**
	 * @param textStatus the textStatus to set
	 */
	public void setTextStatus(String textStatus) {
		this.textStatus = textStatus;
	}
	/**
	 * @return the coverLocation
	 */
	public Set<String> getCoverLocation() {
		return coverLocation;
	}
	/**
	 * @param coverLocation the coverLocation to set
	 */
	public void setCoverLocation(Set<String> coverLocation) {
		this.coverLocation = coverLocation;
	}
	/**
	 * @return the textLocation
	 */
	public Set<String> getTextLocation() {
		return textLocation;
	}
	/**
	 * @param textLocation the textLocation to set
	 */
	public void setTextLocation(Set<String> textLocation) {
		this.textLocation = textLocation;
	}
	/**
	 * @return the coverLoadtags
	 */
	public List<LoadTag> getCoverLoadtags() {
		return coverLoadtags;
	}
	/**
	 * @param coverLoadtags the coverLoadtags to set
	 */
	public void setCoverLoadtags(List<LoadTag> coverLoadtags) {
		this.coverLoadtags = coverLoadtags;
	}
	/**
	 * @return the textLoadtags
	 */
	public List<LoadTag> getTextLoadtags() {
		return textLoadtags;
	}
	/**
	 * @param textLoadtags the textLoadtags to set
	 */
	public void setTextLoadtags(List<LoadTag> textLoadtags) {
		this.textLoadtags = textLoadtags;
	}
	/**
	 * @return the coverQuantityReceived
	 */
	public float getCoverQuantityReceived() {
		return coverQuantityReceived;
	}
	/**
	 * @param coverQuantityReceived the coverQuantityReceived to set
	 */
	public void setCoverQuantityReceived(float coverQuantityReceived) {
		this.coverQuantityReceived = coverQuantityReceived;
	}
	/**
	 * @return the textQuantityReceived
	 */
	public float getTextQuantityReceived() {
		return textQuantityReceived;
	}
	/**
	 * @param textQuantityReceived the textQuantityReceived to set
	 */
	public void setTextQuantityReceived(float textQuantityReceived) {
		this.textQuantityReceived = textQuantityReceived;
	}
	/**
	 * @return the bookPrevStation
	 */
	public String getBookPrevStation() {
		return bookPrevStation;
	}
	/**
	 * @param bookPrevStation the bookPrevStation to set
	 */
	public void setBookPrevStation(String bookPrevStation) {
		this.bookPrevStation = bookPrevStation;
	}
	/**
	 * @return the bookStatus
	 */
	public String getBookStatus() {
		return bookStatus;
	}
	/**
	 * @param bookStatus the bookStatus to set
	 */
	public void setBookStatus(String bookStatus) {
		this.bookStatus = bookStatus;
	}
	/**
	 * @return the bookLocation
	 */
	public Set<String> getBookLocation() {
		return bookLocation;
	}
	/**
	 * @param bookLocation the bookLocation to set
	 */
	public void setBookLocation(Set<String> bookLocation) {
		this.bookLocation = bookLocation;
	}
	/**
	 * @return the bookLoadtags
	 */
	public List<LoadTag> getBookLoadtags() {
		return bookLoadtags;
	}
	/**
	 * @param bookLoadtags the bookLoadtags to set
	 */
	public void setBookLoadtags(List<LoadTag> bookLoadtags) {
		this.bookLoadtags = bookLoadtags;
	}
	/**
	 * @return the bookQuantityReceived
	 */
	public float getBookQuantityReceived() {
		return bookQuantityReceived;
	}
	/**
	 * @param bookQuantityReceived the bookQuantityReceived to set
	 */
	public void setBookQuantityReceived(float bookQuantityReceived) {
		this.bookQuantityReceived = bookQuantityReceived;
	}
	/**
	 * @return the currentJobQtyNeeded
	 */
	public Integer getCurrentJobQtyNeeded() {
		return currentJobQtyNeeded;
	}
	/**
	 * @param currentJobQtyNeeded the currentJobQtyNeeded to set
	 */
	public void setCurrentJobQtyNeeded(Integer currentJobQtyNeeded) {
		this.currentJobQtyNeeded = currentJobQtyNeeded;
	}
	
	/**
	 * @return the loadTags
	 */
	public String getLoadTags() {
		if(textLoadtags != null && !textLoadtags.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(LoadTag lt : textLoadtags){
				sb.append(lt.getLoadTagId()).append("<br/>");
			}
			sb.delete(sb.lastIndexOf("<br/>"), sb.length());
			loadTags = sb.toString();
		}else if(coverLoadtags != null && !coverLoadtags.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(LoadTag lt : coverLoadtags){
				sb.append(lt.getLoadTagId()).append("<br/>");
			}
			sb.delete(sb.lastIndexOf("<br/>"), sb.length());
			loadTags = sb.toString();
		}else if(bookLoadtags != null && !bookLoadtags.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(LoadTag lt : bookLoadtags){
				sb.append(lt.getLoadTagId()).append("<br/>");
			}
			sb.delete(sb.lastIndexOf("<br/>"), sb.length());
			loadTags = sb.toString();
		}
		return loadTags;
	}
	/**
	 * @param loadTags the loadTags to set
	 */
	public void setLoadTags(String loadTags) {
		this.loadTags = loadTags;
	}
	
	/**
	 * @return the cartNumbers
	 */
	public String getCartNumbers() {
		if(textLocation != null && !textLocation.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(String lt : textLocation){
				sb.append(lt).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			cartNumbers = sb.toString();
		}else if(coverLocation != null && !coverLocation.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(String lt : coverLocation){
				sb.append(lt).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			cartNumbers = sb.toString();
		}else if(bookLocation != null && !bookLocation.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for(String lt : bookLocation){
				sb.append(lt).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			cartNumbers = sb.toString();
		}
		return cartNumbers;
	}
	/**
	 * @param cartNumbers the cartNumbers to set
	 */
	public void setCartNumbers(String cartNumbers) {
		this.cartNumbers = cartNumbers;
	}

	
}
