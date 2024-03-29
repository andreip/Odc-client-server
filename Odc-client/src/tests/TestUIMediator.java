package tests;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import main.Mediator;
import main.TransferInfo;
import models.TransfersTableModel;
import models.UserListModel;

import org.apache.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;
import gui.UIMediator;
import gui.UserList;
import junit.framework.TestCase;

public class TestUIMediator extends TestCase {
	UIMediator uimed;
	Mediator medMock;
	Logger mockLogger;

	public void setUp() {
		mockLogger = mock(Logger.class);
		Whitebox.setInternalState(UIMediator.class, "logger", mockLogger);

		medMock = mock(Mediator.class);
		uimed = UIMediator.getInstance();
		uimed.registerMediator(medMock);
		uimed.registerTransfersTableModel(mock(TransfersTableModel.class));
	}

	public void testUIMediatorSingleton() {
		assertEquals(uimed, UIMediator.getInstance());
	}

	public void testAddUserToModel() throws InvocationTargetException, InterruptedException {
		final UserListModel userListModel = new UserListModel(uimed);
		assertEquals(0, userListModel.size());
		uimed.userOn("A");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				assertEquals(1, userListModel.size());
			};
		});
	}

	public void testRemoveUserFromModel() throws InvocationTargetException, InterruptedException {
		final UserListModel userListModel = new UserListModel(uimed);
		uimed.userOn("A");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				assertEquals(1, userListModel.size());
			};
		});
		uimed.userOff("NoOne");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				assertEquals(1, userListModel.size());
			};
		});
		uimed.userOff("A");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				assertEquals(0, userListModel.size());
			};
		});
	}

	/* Test that uiMediator does not know about usernames, and
	 * in order to get them, it has to check with Mediator.
	 */
	public void testGetUsername() {
		verify(medMock, times(0)).getUsername();
		uimed.getUsername();
		verify(medMock, times(1)).getUsername();
	}

	/* Test that a new row is added w/ someone wants to download a file. */
	public void testOutgoingTransferIsDisplayed() {
		TransfersTableModel transferMock = mock(TransfersTableModel.class);
		uimed.registerTransfersTableModel(transferMock);
		uimed.newOutgoingTransfer(null);
		verify(transferMock, times(0)).addRow((TransferInfo) null);
		TransferInfo ti = new TransferInfo();
		uimed.newOutgoingTransfer(ti);
		verify(transferMock, times(1)).addRow(ti);
	}

	public void testUpdateTransferCallsTableModel() {
		TransfersTableModel transferMock = mock(TransfersTableModel.class);
		uimed.registerTransfersTableModel(transferMock);
		uimed.updateTransferValue(0, 0);
		verify(transferMock, times(1)).updateTransferValue(0, 0);
		uimed.updateTransferFilesize(0, 10);
		verify(transferMock, times(1)).updateTransferFilesize(0, 10);
	}

	public void testPathParsingIncomingTransfer() throws InvocationTargetException, InterruptedException {
		UserList userList = mock(UserList.class);
		uimed.registerUserList(userList);
		when(userList.getSelectedUser()).thenReturn("user");

		final ArgumentCaptor<TransferInfo> arg = ArgumentCaptor.forClass(TransferInfo.class);
		uimed.newIncomingTransfer("chris/path/to/file");

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				verify(medMock, times(1)).newIncomingTransfer(arg.capture());
				assertEquals(arg.getValue().filename, "file");
				assertEquals(arg.getValue().path, "path/to/");
			};
		});
	}
}
