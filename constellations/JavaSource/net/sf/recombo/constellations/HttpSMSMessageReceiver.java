/*
 * Created on 02/09/2004
 */
package net.sf.recombo.constellations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.recombo.common.ControllerFactory;


/**
 * @author Devendra Tewari
 */
public class HttpSMSMessageReceiver extends HttpServlet {
    
    private static final int DUPLICATE_MESSAGE_IGNORE_INTERVAL = 5; // seconds 
        
    private static ConstellationController constellationController;
    
    public HttpSMSMessageReceiver() {
        constellationController = (ConstellationController)ControllerFactory.getController(ConstellationController.class);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }
    
    private void process(HttpServletRequest req, HttpServletResponse resp) 
    		throws ServletException, IOException {
        resp.setContentType("text/plain");
        
        boolean ignoreMessage = false;
        SMSMessage smsMessage;
        List list;
        Calendar currentDate = Calendar.getInstance();
        Calendar lowerLimit;
        Calendar messageArrivalDate;
        String message = req.getParameter("message");
        String sender = req.getParameter("sender");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        
        // Query messages to see if our message is a duplicate
        // message.
        list = constellationController.findSMSMessageByText(message);
        if (!list.isEmpty()) {
            // There is a message with the same text.
            // Verify if it was received within the last
            // DUPLICATE_MESSAGE_IGNORE_INTERVAL seconds.
            smsMessage = (SMSMessage)list.get(list.size() - 1); // last message

            lowerLimit = Calendar.getInstance();
            lowerLimit.set(currentDate.get(Calendar.YEAR), currentDate
                    .get(Calendar.MONTH), currentDate
                    .get(Calendar.DAY_OF_MONTH), currentDate
                    .get(Calendar.HOUR_OF_DAY), currentDate
                    .get(Calendar.MINUTE), currentDate.get(Calendar.SECOND)
                    - DUPLICATE_MESSAGE_IGNORE_INTERVAL);
            
            messageArrivalDate = Calendar.getInstance();
            messageArrivalDate.setTime(smsMessage.getArrivalDate());
            
            if (messageArrivalDate.after(lowerLimit)) ignoreMessage = true;
        }
        
        if (ignoreMessage) {
            out.write("IGNORED [" + sender + ": " + message + "]");
        } else {
            // Save message.
            smsMessage = new SMSMessage();
            smsMessage.setMessage(message);
            smsMessage.setArrivalDate(currentDate.getTime());
            smsMessage.setSender(sender);
            
            constellationController.addSMSMessage(smsMessage);
            
            out.write("OK [" + sender + ": " + message + "]");
        }
        
        out.flush();
    }
}
