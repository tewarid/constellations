/*
 * Created on 26/08/2004
 */
package net.sf.recombo.constellations;

import java.util.Date;
import java.util.List;

import net.sf.recombo.common.ControllerFactory;


/**
 * @author Devendra Tewari
 */
public class ServiceFacade {
    private static ConstellationController constellationController;
    
    static {
        constellationController = (ConstellationController)ControllerFactory.getController(ConstellationController.class);
    }
    
    public List findAllSMSMessages() {
        return constellationController.findAllSMSMessages();
    }
    
    public List findAllSMSMessagesFromArrivalDate(Date arrivalDate) {
        return constellationController.findAllSMSMessagesFromArrivalDate(arrivalDate);
    }
    
    public Date getNewDate() {
        return new Date();
    }
}
