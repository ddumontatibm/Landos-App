package com.ibm.opensocial.landos;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class SubscribeServletTest extends EasyMock {
  private static final String TEST_USER = "com.ibm.opensocial.users:test";
  
  private SubscribeServlet servlet;
  private IMocksControl control;
  private Connection connection;
  private DataSource source;
  private Map<String, Object> attributes;
  private HttpServletRequest req;
  private StringWriter output;
  private HttpServletResponse resp;
  
  @Before
  public void before() throws Exception {
    servlet = new SubscribeServlet();
    control = createControl();
    attributes = Maps.newHashMap();
    output = new StringWriter();
    
    connection = TestControlUtils.mockConnection(control);
    source = TestControlUtils.mockDataSource(control, connection);
    
    resp = TestControlUtils.mockResponse(control, output);
  }
  
  @After
  public void after() {
    
  }
  
  @Test
  public void testDoGetUserSubscribedNoPath() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "");
    expectIsAdmin(req, "", false);
    connection.close(); expectLastCall().once();
    
    control.replay();
    servlet.doGet(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"\",\"subscribed\":false,\"admin\":false}", output.toString());
  }
  
  @Test
  public void testDoGetUserSubscribedRootPath() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/");
    expectIsAdmin(req, "", false);
    connection.close(); expectLastCall().once();
    
    control.replay();
    servlet.doGet(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"\",\"subscribed\":false,\"admin\":false}", output.toString());
  }
  
  @Test
  public void testDoGetUserSubscribed() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    expectIsAdmin(req, TEST_USER, true);
    PreparedStatement stmt = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(stmt, query, result, TEST_USER, true);
    connection.close(); expectLastCall().times(2);

    control.replay();
    servlet.doGet(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":true,\"admin\":true}", output.toString());
  }
  
  @Test
  public void testDoGetUserUnsubscribed() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    expectIsAdmin(req, TEST_USER, false);
    PreparedStatement stmt = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(stmt, query, result, TEST_USER, false);
    connection.close(); expectLastCall().times(2);

    control.replay();
    servlet.doGet(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":false,\"admin\":false}", output.toString());
  }
  
  @Test
  public void testDoNoPathSubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "");

    // Could not subscribe user.
    resp.setStatus(500); expectLastCall().once();
    control.replay();
    servlet.doPut(req, resp);
    control.verify();
   
    assertEquals("Verify servlet output", "{\"id\":\"\",\"subscribed\":false}", output.toString());
  }
  
  @Test
  public void testDoSubscribedUserSubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    PreparedStatement check = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(check, query, result, TEST_USER, true); // checks if subscribed first.
    connection.close(); expectLastCall().once();

    // All set, the user was already subscribed.
    resp.setStatus(200); expectLastCall().once();
    control.replay();
    servlet.doPut(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":true}", output.toString());
  }
  
  @Test
  public void testDoUnsubscribedUserSubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    PreparedStatement check = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(check, query, result, TEST_USER, false); // checks if subscribed first.
    
    // Insert the user, because it wasn't subscribed.
    PreparedStatement insert = control.createMock(PreparedStatement.class);
    expect(connection.prepareStatement(capture(query))).andReturn(insert).once();
    insert.setString(1, TEST_USER); expectLastCall().once();
    insert.close(); expectLastCall().once();
    expect(insert.executeUpdate()).andReturn(1).once();
    
    // Verify user subscription
    PreparedStatement checkAgain = control.createMock(PreparedStatement.class);
    ResultSet resultAgain = control.createMock(ResultSet.class);
    expectIsSubscribed(checkAgain, query, resultAgain, TEST_USER, true);
    
    connection.close(); expectLastCall().times(3);

    resp.setStatus(200); expectLastCall().once();
    control.replay();
    servlet.doPut(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":true}", output.toString());
  }
  
  @Test
  public void testDoNoPathUnsubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "");

    // Didn't have to unsubscribe user.
    resp.setStatus(200); expectLastCall().once();
    control.replay();
    servlet.doDelete(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"\",\"subscribed\":false}", output.toString());
  }
  
  @Test
  public void testDoUnsubscribedUserUnsubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    PreparedStatement check = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(check, query, result, TEST_USER, false); // checks if subscribed first.
    connection.close(); expectLastCall().once();
    
    // All set, the user wasn't subscribed.
    resp.setStatus(200); expectLastCall().once();
    control.replay();
    servlet.doDelete(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":false}", output.toString());
  }
  
  @Test
  public void testDoSubscribedUserUnsubscribe() throws Exception {
    req = TestControlUtils.mockRequest(control, attributes, source, "/" + TEST_USER);
    PreparedStatement check = control.createMock(PreparedStatement.class);
    Capture<String> query = new Capture<String>(); 
    ResultSet result = control.createMock(ResultSet.class);
    
    expectIsSubscribed(check, query, result, TEST_USER, true); // checks if subscribed first.
    
    // Delete the user, because it was subscribed.
    PreparedStatement insert = control.createMock(PreparedStatement.class);
    expect(connection.prepareStatement(capture(query))).andReturn(insert).once();
    insert.setString(1, TEST_USER); expectLastCall().once();
    insert.close(); expectLastCall().once();
    expect(insert.executeUpdate()).andReturn(1).once();
    
    // Verify user subscription
    PreparedStatement checkAgain = control.createMock(PreparedStatement.class);
    ResultSet resultAgain = control.createMock(ResultSet.class);
    expectIsSubscribed(checkAgain, query, resultAgain, TEST_USER, false);
    
    connection.close(); expectLastCall().times(3);

    resp.setStatus(200); expectLastCall().once();
    control.replay();
    servlet.doDelete(req, resp);
    control.verify();
    
    assertEquals("Verify servlet output", "{\"id\":\"" + TEST_USER + "\",\"subscribed\":false}", output.toString());
  }
  
  private void expectIsSubscribed(PreparedStatement stmt, Capture<String> query, ResultSet result, String user, boolean isUserSubscribed) throws Exception {
    expect(connection.prepareStatement(capture(query))).andReturn(stmt).once();
    stmt.setString(1, user); expectLastCall().once();
    stmt.close(); expectLastCall().times(0, 1);
    
    expect(stmt.executeQuery()).andReturn(result).once();
    result.close(); expectLastCall().times(0, 1);
    
    expect(result.first()).andReturn(true).once();
    expect(result.getInt(1)).andReturn(isUserSubscribed ? 1 : 0).times(0, 1);
  }
  
  private void expectIsAdmin(HttpServletRequest req, String user, boolean isAdmin) throws Exception {
    PreparedStatement authstmt = control.createMock(PreparedStatement.class);
    ResultSet authres = control.createMock(ResultSet.class);
    expect(connection.prepareStatement("SELECT * FROM `subscribed` WHERE `user`=? AND `admin`=1")).andReturn(authstmt).once();
    authstmt.setString(1, user); expectLastCall().once();
    expect(authstmt.executeQuery()).andReturn(authres).once();
    expect(authres.first()).andReturn(isAdmin).once();
    authres.close(); expectLastCall().once();
    authstmt.close(); expectLastCall().once();
  }
}

