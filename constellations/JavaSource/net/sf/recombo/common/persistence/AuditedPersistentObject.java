 
package net.sf.recombo.common.persistence;

import java.util.Date;

/**
 * This class and it's sub-classes represent persistent objects with
 * audit information such as the date/hour the object was last updated. 
 */
public abstract class AuditedPersistentObject extends PersistentObject {
	protected Date dateLastUpdated = null;
	
	/**
	 * Recover the date/hour of last update. 
	 * @return Date/Hour
	 */
	public Date getDateLastUpdated() {
		return dateLastUpdated;
	}

	/**
	 * Set the date/hour of last update.
	 * @param dateLastUpdated Date/hour 
	 */
	public void setDateLastUpdated(Date dateLastUpdated) {
		this.dateLastUpdated = dateLastUpdated;
	}
}
