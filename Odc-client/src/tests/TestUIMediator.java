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

	public void setUp() {
		Logger mockLogger = mock(Logger.class);
		Whitebox.setInternalState(UIMediator.class, "logger", mockLogger);

		uimed = UIMediator.getInstance();
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
}
