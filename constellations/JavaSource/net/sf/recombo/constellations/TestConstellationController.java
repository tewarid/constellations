/*
 * Created on 01/09/2004
 */
package net.sf.recombo.constellations;

import java.util.Date;
import java.util.List;

import net.sf.recombo.common.ControllerFactory;
import net.sf.recombo.common.persistence.PersistentObjectNotFoundException;

import junit.framework.TestCase;

/**
 * @author Devendra Tewari
 */
public class TestConstellationController extends TestCase {
    private ConstellationController starController; 
    
    /**
     * Test SMS Message related functions.
     */
    public void testSMSMessage() throws PersistentObjectNotFoundException {
        SMSMessage message = new SMSMessage();
        message.setMessage("Test");
        Date date = new Date();
        message.setArrivalDate(date);
        message.setSender("91450680");
        
        starController.addSMSMessage(message);
        
        message.setMessage("Test Update");
        starController.updateSMSMessage(message);
        
        List list = starController.findAllSMSMessages();
        assertTrue(!list.isEmpty());
        
        list = starController.findAllSMSMessagesFromArrivalDate(date);
        assertTrue(!list.isEmpty());

        list = starController.findSMSMessageByText("Test Update");
        assertTrue(!list.isEmpty());

        starController.findSMSMessage(message.getId()); // generates exception if not found
        
        starController.deleteSMSMessage(message);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        starController = (ConstellationController)ControllerFactory.getController(ConstellationController.class);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
    }

    /**
     * Constructor.
     * @param name
     */
    public TestConstellationController(String name) {
        super(name);
    }
}
