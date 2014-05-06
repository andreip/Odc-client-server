package tests;

import static org.mockito.Mockito.mock;

import java.net.InetAddress;

import junit.framework.TestCase;
import webservice.WebService;
import webservice.WebServiceWorker;

public class TestWebService extends TestCase {
	WebServiceWorker workerMock;
	WebService webService;
	InetAddress addr;
	int port;

	protected void setUp() throws Exception {
		super.setUp();
		workerMock = mock(WebServiceWorker.class);
		addr = InetAddress.getByName("localhost");
		port = 9090;
		webService = new WebService(addr, port, workerMock);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		webService.close();
	}

	public void testGetUsers() {
		assertEquals("USERS", webService.getUsers());
	}

	public void testAddUser() {
		webService.addUser("ana", "localhost", 9000);
		assertEquals("USERS ana localhost 9000", webService.getUsers());
		webService.addUser("ion", "localhost", 9001);
		assertEquals("USERS ion localhost 9001 ana localhost 9000", webService.getUsers());
	}

	public void testRemoveUser() {
		webService.addUser("ana", "localhost", 9000);
		assertEquals("USERS ana localhost 9000", webService.getUsers());
		webService.removeUser("ana");
		assertEquals("USERS", webService.getUsers());
	}
	
	public void testRemoveNonExistingUser() {
		webService.addUser("ana", "localhost", 9000);
		assertEquals("USERS ana localhost 9000", webService.getUsers());
		webService.removeUser("ion");
		assertEquals("USERS ana localhost 9000", webService.getUsers());
	}

	public void testGetUserFiles() {
		assertEquals(null, webService.getUserFiles("ana"));
	}

	public void testAddUserFiles() {
		byte[] expected = "test".getBytes();
		webService.addUserFiles("ana", expected);
		assertEquals(expected, webService.getUserFiles("ana"));
	}

	public void testRemoveUserFiles() {
		byte[] expected = "test".getBytes();
		webService.addUserFiles("ana", expected);
		assertEquals(expected, webService.getUserFiles("ana"));
		webService.removeUserFiles("ana");
		assertEquals(null, webService.getUserFiles("ana"));
	}
	
	public void testRemoveUserFilesForNonExistingUser() {
		byte[] expected = "test".getBytes();
		webService.addUserFiles("ana", expected);
		assertEquals(expected, webService.getUserFiles("ana"));
		webService.removeUserFiles("ion");
		assertEquals(null, webService.getUserFiles("ion"));
	}
}
