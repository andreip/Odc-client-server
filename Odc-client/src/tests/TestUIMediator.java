package tests;

import gui.UIMediator;
import junit.framework.TestCase;

public class TestUIMediator extends TestCase {
	UIMediator uimed;

	public void setUp() {
		uimed = UIMediator.getInstance();
	}

	public void testUIMediatorSingleton() {
		assertEquals(uimed, UIMediator.getInstance());
	}
}
