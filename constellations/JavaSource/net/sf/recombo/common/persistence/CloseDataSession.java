
package net.sf.recombo.common.persistence;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;



/**
 * Closes hibernate session after the web request has been served. This class
 * guarantees that the session will be closed only once even though the filter 
 * can be called many times within the same request during forwards and includes
 * (as in Oracle 9.0.3 iAS)
 * 
 * @author Devendra Tewari
 */
public class CloseDataSession implements Filter {
	
	/**
	 * Counter to trace the number of times the filter has been 
	 * called within the same request
	 */
	private static ThreadLocal stackCounter = new ThreadLocal(); 
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		try {
			increment();
			chain.doFilter(req, resp);
		} finally {					
			// In some application servers such as Oracle 9.0.3 iAS the 
			// filter may also be called after a "forward" or "include".
			// The hibernate session is closed only when the response 
			// is being returned to the user.
			if (decrement() <= 0) {
				HibernateUtil.getInstance().closeSession();
			}			
		}
	}
		
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	}

	/**
	 * Increment call stack trace counter.
	 * @return Current value of the counter
	 */
	private int increment() {
		
		int count = 0;
		if (stackCounter.get() == null) {
			stackCounter.set(new Integer(++count));
		} else {
			count = ((Integer) stackCounter.get()).intValue();
			stackCounter.set(new Integer(++count));
		}
		return count;

	}

	/**
	 * Decrement call stack trace counter.
	 * @return Current value of the counter
	 */
	private int decrement() {
		
		int count = 0;
		if (stackCounter.get() == null) {
			stackCounter.set(new Integer(count));
		} else {
			count = ((Integer) stackCounter.get()).intValue();
			stackCounter.set(new Integer(--count));
		}
		return count;
		
	}
	
}
