package network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import main.Mediator;
import main.TransferInfo;

import org.apache.log4j.Logger;

public class RspHandler {
	// Logger for this class 
	static Logger logger = Logger.getLogger(RspHandler.class);
	
	// The transfer state - 0 if waiting for ACK, 1 if receiving data
	private int state;
	private TransferInfo ti;
	private Mediator mediator;
	private Transfer transfer;
	
	RandomAccessFile raf = null;
	FileChannel fc = null;
	MappedByteBuffer mbb = null;
	
	public RspHandler(Transfer transfer, Mediator mediator, TransferInfo ti) {
		this.mediator = mediator;
		this.transfer = transfer;
		this.ti = ti;
		this.state = 0;
	}
	
	public synchronized boolean handleResponse(byte[] rsp) {
		if (state == 0) {
			return handleAckMessage(rsp);
		} else {
			return handleFileContents(rsp);
		}
	}
	
	private synchronized boolean handleAckMessage(byte[] rsp) {
		String message = new String(rsp);
		String[] contents = message.split(" ");
		if (contents[0].equals("ACK")) {
			ti.filesize = Integer.parseInt(contents[1]);
			state = 1;
			
			try {
				raf = new RandomAccessFile(mediator.homeDir + ti.filename, "rw");
				raf.setLength(ti.filesize);
				fc = raf.getChannel();
				mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size());
				mediator.notifyTransferFilesize(ti);
				this.transfer.send("ACK".getBytes());
			} catch (FileNotFoundException e) {
				logger.error("Unable to open file. Error was: " + e.toString());
			} catch (IOException e) {
				logger.error("Unable to map file. Error was: " + e.toString());
			} finally {
				try {
					if (fc != null)
						fc.close();
					if (raf != null)
						raf.close();
				} catch (IOException e) {
					logger.error("Unable to close file/channel. Error was: " + e.toString());
				}
			}
		} else {
			logger.warn("Buffer doesn't start with known command. Ignore.");
		}

		return true;
	}
	
	private synchronized boolean handleFileContents(byte[] rsp) {
			mbb.put(rsp);
			mediator.updateTransferValue(ti, mbb.position());
			if (mbb.position() == mbb.limit()) {
				try {
					if (fc != null)
						fc.close();
					if (raf != null)
						raf.close();
				} catch (IOException e) {
					logger.error("Unable to close file/channel. Error was: " + e.toString());
				}
				return false;
			}
			return true;
	}
}