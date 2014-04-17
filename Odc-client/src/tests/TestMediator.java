package tests;

import static org.mockito.Mockito.*;
import gui.UIMediator;

import java.lang.reflect.InvocationTargetException;

import network.Network;
import network.NetworkWorker;

import org.apache.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import main.Mediator;
import main.TransferInfo;
import junit.framework.TestCase;

public class TestMediator extends TestCase {
	Mediator medMock;
	UIMediator uiMedMock;
	Logger mockLogger;

	String username = "chris";
	String homeDir = "chris/";

	public void setUp() {
		mockLogger = mock(Logger.class);
		Whitebox.setInternalState(Mediator.class, "logger", mockLogger);

		medMock = new Mediator(username, homeDir);
		uiMedMock = mock(UIMediator.class);
		medMock.registerUIMediator(uiMedMock);

		/* Create mocks for network and networkWorker and set them on mediator. */
		Network network = mock(Network.class);
		NetworkWorker networkWorker = mock(NetworkWorker.class);
		medMock.initMediator(networkWorker, network);
	}

	/* Assert the path to download a file from local user is build
	 * using the username's homeDir also.
	 */
	public void testBuildOutgoingTransferDir() throws InvocationTargetException, InterruptedException {
		final ArgumentCaptor<TransferInfo> arg = ArgumentCaptor.forClass(TransferInfo.class);
		medMock.newOutgoingTransfer("cineva", "path/to/file2");

		verify(uiMedMock, times(1)).newOutgoingTransfer(arg.capture());
		assertEquals(arg.getValue().filename, "file2");
		assertEquals(arg.getValue().path, homeDir + "path/to/");
	}

}
