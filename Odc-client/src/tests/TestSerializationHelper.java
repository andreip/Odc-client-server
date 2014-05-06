package tests;

import java.io.IOException;

import junit.framework.TestCase;
import utils.SerializationHelper;

public class TestSerializationHelper extends TestCase {

	public void testSerializeSame() throws IOException, ClassNotFoundException {
		String stringToSerialize = "I am a string.";
		byte[] result = SerializationHelper.serialize(stringToSerialize);
		String stringResult = (String)SerializationHelper.deserialize(result);
		assertEquals(stringToSerialize, stringResult);
	}
}
