package tests;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.Selector;

import main.Mediator;
import network.Network;
import network.NetworkWorker;
import network.Transfer;

import org.apache.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public class TestNetwork extends TestCase {
	Logger mockLogger;
	Mediator medMock;
	NetworkWorker workerMock;
	Network network;
	InetAddress addr;
	int port;
	Selector selector;

	public void setUp() throws IOException {
		mockLogger = mock(Logger.class);
		Whitebox.setInternalState(Network.class, "logger", mockLogger);

		medMock = mock(Mediator.class);
		workerMock = mock(NetworkWorker.class);
		addr = InetAddress.getByName("localhost");
		port = 9090;
		network = new Network(medMock, workerMock, addr, port);
		selector = mock(Selector.class);
		network.setSelector(selector);
	}

	/* Transfer.initTransfer() does trigger an error that should
	 * be caught and reported to Mediator.notifyNetworkError.
	 */
	public void testStartTransferOException() throws IOException {
		Transfer t = mock(Transfer.class);
		doThrow(new IOException()).when(t).initTransfer();

		network.startTransfer(null, t);
		final ArgumentCaptor<String> err_msg = ArgumentCaptor.forClass(String.class);
		verify(medMock, times(1)).notifyNetworkError(err_msg.capture());
	}

}
