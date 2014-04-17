package tests;

import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import gui.UIMediator;
import network.Network;
import network.NetworkWorker;

import org.apache.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import main.Mediator;
import main.TransferInfo;
import junit.framework.TestCase;

public class TestMediator extends TestCase {
	Mediator med;
	UIMediator uiMedMock;
	Logger mockLogger;

	String username = "chris";
	String homeDir = "chris/";

	public void setUp() {
		mockLogger = mock(Logger.class);
		Whitebox.setInternalState(Mediator.class, "logger", mockLogger);

		med = new Mediator(username, homeDir);
		uiMedMock = mock(UIMediator.class);
		med.registerUIMediator(uiMedMock);

		/* Create mocks for network and networkWorker and set them on mediator. */
		Network network = mock(Network.class);
		NetworkWorker networkWorker = mock(NetworkWorker.class);
		med.initMediator(networkWorker, network);
	}

	/* Assert the path to download a file from local user is build
	 * using the username's homeDir also.
	 */
	public void testBuildOutgoingTransferDir() throws InvocationTargetException, InterruptedException {
		final ArgumentCaptor<TransferInfo> arg = ArgumentCaptor.forClass(TransferInfo.class);
		med.newOutgoingTransfer("cineva", "path/to/file2");

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				verify(uiMedMock, times(1)).newOutgoingTransfer(arg.capture());
				assertEquals(arg.getValue().filename, "file2");
				assertEquals(arg.getValue().path, homeDir + "path/to/");
			}
		});
	}

	/* Assert Mediator calls UIMediator for rendering concerned logic. */
	public void testNotifyUIMediatorOnTransferFilesize() throws InvocationTargetException, InterruptedException {
		final TransferInfo ti = new TransferInfo();
		ti.id = 1;
		ti.filesize = 10;

		med.notifyTransferFilesize(ti);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				verify(uiMedMock, times(1)).updateTransferFilesize(ti.id, ti.filesize);
			}
		});
	}

	/* Assert Mediator calls UIMediator for rendering concerned logic. */
	public void testUpdateUIMediatorOnTransferValue() throws InvocationTargetException, InterruptedException {
		final TransferInfo ti = new TransferInfo();
		ti.id = 1;
		final int progress = 50;

		med.updateTransferValue(ti, progress);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				verify(uiMedMock, times(1)).updateTransferValue(ti.id, progress);
			}
		});
	}
}
