package com.jash.protokit.comparer;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.protobuf.Message;
import com.jash.protokit.LibraryManagement.Address;
import com.jash.protokit.LibraryManagement.Book;
import com.jash.protokit.LibraryManagement.Member;

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

	private Map<String, String> expectedReportMap = new HashMap<>();

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

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File("src/test/resources/cases/merger.xml"));
			doc.getDocumentElement().normalize();
			NodeList cases = doc.getElementsByTagName("case");
			for (int i = 0; i < cases.getLength(); i++) {
				Element caseElement = (Element) cases.item(i);
				String caseName = caseElement.getAttribute("name");
				Element expectedEle = (Element) caseElement.getElementsByTagName("expected").item(0);
				NodeList expectedLines = expectedEle.getElementsByTagName("line");
				StringBuilder expectedSb = new StringBuilder();
				String line = null;
				for (int j = 0; j < expectedLines.getLength(); j++) {
					Element lineElement = (Element) expectedLines.item(j);
					line = lineElement.getTextContent();
					expectedSb.append(line).append("\n");
				}
				expectedReportMap.put(caseName, expectedSb.toString());
			}
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
		String caseName = "simpleCompareCase";
		Object[] data = new Object[4];
		Address message1 = Address.newBuilder().setStreet("Street name").setCity("City name 1").setState("State name")
				.setCountry("Country name").build();
		Address message2 = Address.newBuilder().setStreet("Street name").setCity("City name 2").setState("State name")
				.setPostalCode("Postal code").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = null;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	private Object[] getKeyFieldCase() {
		String caseName = "keyFieldCase";
		Object[] data = new Object[4];
		Book book1 = Book.newBuilder().setBookId(1).setGenre("Genre name 1").build();
		Book book2 = Book.newBuilder().setBookId(1).setGenre("Genre name 2").build();
		Member message1 = Member.newBuilder().addBorrowHistory(book1).build();
		Member message2 = Member.newBuilder().addBorrowHistory(book2).build();
		CompareOptions options = CompareOptions.Builder.newBuilder().setMessageKeyField(Book.class, "Book.bookId")
				.build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	@DataProvider(name = "dataProvider")
	public Object[][] dataProvider() {
		List<Object[]> data = new ArrayList<>();
		data.add(getSimpleCompareCase());
		data.add(getKeyFieldCase());
		return data.toArray(new Object[data.size()][]);
	}

	@Test(dataProvider = "dataProvider")
	public void testComparer(Message message1, Message message2, CompareOptions options, String expectedReport) {
		String report = ProtoComparer.compare(message1, message2, options);
		assertEquals(report, expectedReport);
	}

}
