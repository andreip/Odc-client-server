package tests;

import static org.mockito.Mockito.*;
import junit.framework.TestCase;

import gui.UIMediator;
import models.TransfersTableModel;

public class TestTransfersTableModel extends TestCase {
	TransfersTableModel ttm;
	UIMediator uiMedMock;

	public void setUp() {
		uiMedMock = mock(UIMediator.class);
		ttm = new TransfersTableModel(uiMedMock);
	}
	
	public void testGetTransferStateForValue() {
		assertEquals(TransfersTableModel.transferingState,
			ttm.getTransferStateForValue(10, 11));
		assertEquals(TransfersTableModel.completedState,
			ttm.getTransferStateForValue(11, 11));
		assertEquals(TransfersTableModel.completedState,
			ttm.getTransferStateForValue(12, 11));
	}

}
