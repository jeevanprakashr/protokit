package com.jash.protokit.comparer;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.protobuf.Message;
import com.jash.protokit.LibraryManagement.Address;

public class ProtoComparerTest {

	private static String MESSAGE_START;
	private static String MESSAGE_END;
	private static String LIST_START;
	private static String LIST_END;
	private static String COLON_SPACE;

	private static char SIGN_CREATE;
	private static char SIGN_UPDATE;
	private static char SIGN_DELETE;
	private static char SIGN_EMPTY;

	@BeforeClass
	public void setConstants() {
		try {
			MESSAGE_START = (String) getPredefinedConstant("MESSAGE_START");
			MESSAGE_END = (String) getPredefinedConstant("MESSAGE_END");
			LIST_START = (String) getPredefinedConstant("LIST_START");
			LIST_END = (String) getPredefinedConstant("LIST_END");
			COLON_SPACE = (String) getPredefinedConstant("COLON_SPACE");
			SIGN_CREATE = (char) getPredefinedConstant("SIGN_CREATE");
			SIGN_UPDATE = (char) getPredefinedConstant("SIGN_UPDATE");
			SIGN_DELETE = (char) getPredefinedConstant("SIGN_DELETE");
			SIGN_EMPTY = (char) getPredefinedConstant("SIGN_EMPTY");
		} catch (Exception e) {
			throw new RuntimeException("Failed to set constants", e);
		}
	}

	private Object getPredefinedConstant(String variableName)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = ProtoComparer.class.getDeclaredField(variableName);
		field.setAccessible(true);
		return field.get(null);
	}

	private char[] getPrefix(char sign, int level) {
		try {
			Method method = ProtoComparer.class.getDeclaredMethod("getPrefix", char.class, int.class);
			method.setAccessible(true);
			return (char[]) method.invoke(null, sign, level);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get prefix", e);
		}
	}

	private Object[] getSimpleCompareCase() {
		Object[] data = new Object[4];
		Address message1 = Address.newBuilder().setStreet("Street name").setCity("City name 1").setState("State name")
				.setCountry("Country name").build();
		Address message2 = Address.newBuilder().setStreet("Street name").setCity("City name 2").setState("State name")
				.setPostalCode("Postal code").build();
		StringBuilder expectedSb = new StringBuilder();
		int level = 0;
		expectedSb.append(getPrefix(SIGN_UPDATE, level));
		expectedSb.append("Address").append(COLON_SPACE);
		expectedSb.append(MESSAGE_START);
		level++;
		expectedSb.append(getPrefix(SIGN_UPDATE, level)).append("city").append(COLON_SPACE).append("City name 1")
				.append(" => ").append("City name 2").append("\n");
		expectedSb.append(getPrefix(SIGN_CREATE, level)).append("postal_code").append(COLON_SPACE)
				.append("Postal code").append("\n");
		expectedSb.append(getPrefix(SIGN_DELETE, level)).append("country").append(COLON_SPACE).append("Country name")
				.append("\n");
		level--;
		expectedSb.append(getPrefix(SIGN_UPDATE, level));
		expectedSb.append(MESSAGE_END);
		data[0] = message1;
		data[1] = message2;
		data[2] = null;
		data[3] = expectedSb.toString();
		return data;
	}

	@DataProvider(name = "dataProvider")
	public Object[][] dataProvider() {
		List<Object[]> data = new ArrayList<>();
		data.add(getSimpleCompareCase());
		return data.toArray(new Object[data.size()][]);
	}

	@Test(dataProvider = "dataProvider")
	public void testComparer(Message message1, Message message2, CompareOptions options, String expectedReport) {
		String report = ProtoComparer.compare(message1, message2, options);
		assertEquals(report, expectedReport);
	}

}
