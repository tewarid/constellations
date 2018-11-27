/*
 * Created on 31/08/2004
 */
package net.sf.recombo.constellations;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.recombo.common.Controller;
import net.sf.recombo.common.persistence.PersistentObjectNotFoundException;

/**
 * @author Devendra Tewari
 */
public class ConstellationController extends Controller {
    
    /**
     * List all SMS Messages ordered by arrivalDate. 
     * @return
     */
    public List findAllSMSMessages() {
        return hibernateUtil.find("from SMSMessage order by arrivalDate");
    }
    
    /**
     * Locate an SMS Message by id.
     * @param message
     */
    public SMSMessage findSMSMessage(String id) throws PersistentObjectNotFoundException {
        return (SMSMessage)hibernateUtil.load(SMSMessage.class, id);
    }
    
    /**
     * Locate SMS messages by message text ordered by arrivalDate.
     * @param message
     */
    public List findSMSMessageByText(String message) {
        return hibernateUtil.find("from SMSMessage where message like ? order by arrivalDate", message, Hibernate.STRING);
    }
    
    /**
     * Locate all SMSMessages with arrival date greater than or equal
     * to the date specified ordered by arrivalDate.
     * @param message
     */
    public List findAllSMSMessagesFromArrivalDate(Date arrivalDate) {
        return hibernateUtil.find("from SMSMessage where arrivalDate >= ? order by arrivalDate", arrivalDate, Hibernate.TIMESTAMP);
    }
    
    /**
     * Add a new SMSMessage. The id of the message
     * is generated automatically if the save
     * was successful.
     * @param message
     */
    public void addSMSMessage(SMSMessage message) {
		hibernateUtil.save(message);
    }
    
    /**
     * Deletes the SMSMessage.
     * @param message
     */
    public void deleteSMSMessage(SMSMessage message) {
		hibernateUtil.delete(message);
    }
    
    /**
     * Update the SMSMessage.
     * @param message
     */
    public void updateSMSMessage(SMSMessage message) {
		hibernateUtil.update(message);
    }
}
