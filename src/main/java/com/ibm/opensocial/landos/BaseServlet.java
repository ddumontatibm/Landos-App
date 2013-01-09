package com.ibm.opensocial.landos;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.wink.json4j.JSONWriter;

public class BaseServlet extends HttpServlet {
  private static final long serialVersionUID = -7232225273021470838L;
  private static final String CLAZZ = BaseServlet.class.getName();
  private static final Logger LOGGER = Logger.getLogger(CLAZZ);
  
  public static final String DATA_SOURCE = "com.ibm.opensocial.landos.servlets.datasource";
  
  private static DataSource dbSource = null;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    if (dbSource == null) {
      try {
        Context initCtx = new InitialContext();
        Context envCtx = (Context)initCtx.lookup("java:comp/env");
        dbSource = (DataSource)envCtx.lookup("jdbc/landos");
      } catch (Exception e) {
        LOGGER.logp(Level.SEVERE, CLAZZ, "init", e.getMessage(), e);
      }
    }
  }
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute(DATA_SOURCE, dbSource);
    super.service(req, resp);
  }

  protected DataSource getDataSource(HttpServletRequest req) {
    return (DataSource)req.getAttribute(DATA_SOURCE);
  }
  
  protected String getUser(HttpServletRequest req) {
    return req.getHeader("OPENSOCIAL-ID");
  }
  
  protected void close(Object... objects) {
    if (objects != null) {
      for (Object object : objects) {
        if (object != null) {
          if (object instanceof Closeable) {
            try { ((Closeable)object).close(); } catch (IOException ignore) { }
          } else if (object instanceof Statement) {
            try { ((Statement)object).close(); } catch (SQLException ignore) { }
          } else if (object instanceof Connection) {
            try { ((Connection)object).close(); } catch (SQLException ignore) { }
          } else if (object instanceof ResultSet) {
            try { ((ResultSet)object).close(); } catch (SQLException ignore) { }
          } else if (object instanceof JSONWriter) {
            try { ((JSONWriter)object).close(); } catch (Exception ignore) { }
          }
        }
      }
    }
  }
}

