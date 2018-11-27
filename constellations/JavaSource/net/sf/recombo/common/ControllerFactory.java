
package net.sf.recombo.common;

import net.sf.recombo.common.persistence.HibernateUtil;

/**
 * Factory for constructing control objects.
 * @author Devendra Tewari
 */
public final class ControllerFactory {
    private static HibernateUtil hibernateUtil;
    
    static {
        try {
			hibernateUtil = HibernateUtil.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static Controller getController(Class controllerClass) {
	    Controller controller = (Controller)hibernateUtil.extendClass(controllerClass);
		controller.hibernateUtil = hibernateUtil;
		return controller;
	}
}
