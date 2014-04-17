package tests;

import main.Mediator;
import models.UserListModel;

import org.apache.log4j.Logger;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;

import gui.UIMediator;
import gui.UserList;
import junit.framework.TestCase;

public class TestUIMediator extends TestCase {
	UIMediator uimed;
	Mediator medMock;

	public void setUp() {
		Logger mockLogger = mock(Logger.class);
		Whitebox.setInternalState(UIMediator.class, "logger", mockLogger);

		medMock = mock(Mediator.class);
		uimed = UIMediator.getInstance();
		uimed.registerMediator(medMock);
	}

	public void testUIMediatorSingleton() {
		assertEquals(uimed, UIMediator.getInstance());
	}

	public void testAddUserToModel() {
		UserListModel userListModel = new UserListModel(uimed);
		assertEquals(0, userListModel.size());
		uimed.addUser("A");
		assertEquals(1, userListModel.size());

	}

	/* Test that uiMediator does not know about usernames, and
	 * in order to get them, it has to check with Mediator.
	 */
	public void testGetUsername() {
		verify(medMock, times(0)).getUsername();
		uimed.getUsername();
		verify(medMock, times(1)).getUsername();
	}
}
