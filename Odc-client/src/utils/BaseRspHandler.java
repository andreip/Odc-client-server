package utils;

public class BaseRspHandler {
	private byte[] rsp = null;
	
	public synchronized boolean handleResponse(byte[] rsp) {
		this.rsp = rsp;
		this.notify();
		return true;
	}
	
	public synchronized void waitForResponse() {
		while(this.rsp == null) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}

		processResponse(this.rsp);
		this.rsp = null;
	}

	protected void processResponse(byte[] rsp) {
		System.out.println(new String(rsp));
	}
}
