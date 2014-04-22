package webservice_client;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import main.Mediator;
import utils.BaseRspHandler;

public class WebServiceRspHandler extends BaseRspHandler {
	static Logger logger = Logger.getLogger(WebServiceClient.class);
	private Mediator mediator;

	public WebServiceRspHandler(Mediator mediator) {
		this.mediator = mediator;
	}

	@Override
	protected void processResponse(byte[] rsp) {
		String resp = new String(rsp);
		String[] strs = resp.split(" ");
		logger.debug("Received " + resp);
		if (strs[0].equals("ACK")) {
			logger.debug("Successfully sent command");
		} else if (strs[0].equals("USERS")) {
			processUsersList(rsp);
		}
	}

	protected void processUsersList(byte[] rsp) {
		String users_str = new String(rsp);
		String[] lst = users_str.split(" ");
		Map<String, Entry<String, Integer>> users = new HashMap<>();
		for (int i = 1; i < lst.length; i += 3) {
			assert(lst.length > i + 2);
			users.put(lst[i], new AbstractMap.SimpleEntry<String, Integer>(
				lst[i+1], Integer.parseInt(lst[i+2])));
		}
		mediator.updateUsersList(users);
	}
}
