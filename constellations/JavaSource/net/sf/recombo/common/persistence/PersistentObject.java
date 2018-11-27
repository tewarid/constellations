
package net.sf.recombo.common.persistence;

import java.io.Serializable;

/**
 * Super-class of all basic / entity classes. 
 */
public abstract class PersistentObject implements Serializable {
	// flag indicating that object is persistent / saved
	private boolean _saved = false; 

	/**
	 * This method is called when the object is saved to persistent
	 * storage.
	 */
	public void onSave() {
		_saved = true;
	}

	/**
	 * This method is called when the object is loaded from persistent
	 * storage.
	 */
	public void onLoad() {
		_saved = true;
	}

	/**
	 * This method is used to discover the saved status of the object.
	 * 
	 * @return True if the object is exists in persistent storage
	 */
	public boolean isSaved() {
		return _saved;
	}
}