package com.ibm.opensocial.landos;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wink.json4j.JSONWriter;

public class RunServlet extends BaseServlet {
	private static final String CLAZZ = RunServlet.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLAZZ);
	
	/**
	 * Gets information about a particular run given an ID.
	 * @throws IOException 
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Set headers
		res.setHeader("CACHE-CONTROL", "no-cache");
		res.setContentType("application/json");
		
		// Parse arguments
		final Pattern p = Pattern.compile("\\/(\\d+)\\/?$");
		Matcher m = p.matcher(req.getRequestURI());
		m.find();
		int id = Integer.parseInt(m.group(1));
		
		// Create JSON writer
		JSONWriter writer = new JSONWriter(res.getWriter()).object();
		
		// Write
		try {
			writer.key("id").value(id).endObject();
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLAZZ, "init", e.getMessage());
		} finally {
			writer.close();
		}
	}

	/**
	 * Creates a new run given a start date, an end date, and (optionally)
	 * whether the run is a test.
	 * @throws IOException 
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Set headers
		res.setHeader("CACHE-CONTROL", "no-cache");
		res.setContentType("application/json");
		
		// Parse arguments
		final Pattern p = Pattern.compile("\\/(\\d+)\\/(\\d+)\\/?(?:(\\d)\\/?)?$");
		Matcher m = p.matcher(req.getRequestURI());
		m.find();
		String start = m.group(1);
		String end = m.group(2);
		boolean test = Boolean.parseBoolean(m.group(3));
		
		// Create JSON Writer
		JSONWriter writer = new JSONWriter(res.getWriter()).object();
		
		// Write
		try {
			writer.key("start").value(start)
				.key("end").value(end)
				.key("test").value(test)
				.endObject();
		} catch (Exception e) {
	        LOGGER.logp(Level.SEVERE, CLAZZ, "init", e.getMessage(), e);
	    } finally {
	    	writer.close();
	    }
	}
	
	/**
	 * Deletes a particular run given an ID.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) {
		
	}
}