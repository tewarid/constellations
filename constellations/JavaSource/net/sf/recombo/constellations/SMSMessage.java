/*
 * Created on 31/08/2004
 */
package net.sf.recombo.constellations;

import java.util.Date;

import net.sf.recombo.common.persistence.PersistentObject;


/**
 * @author Devendra Tewari
 */
public class SMSMessage extends PersistentObject {
    private String id;
    private Date arrivalDate;
    private String message;
    private String sender;
    
    public Date getArrivalDate() {
        return arrivalDate;
    }
    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
