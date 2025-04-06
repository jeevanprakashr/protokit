package com.jash.protokit.comparer;

import static org.testng.Assert.assertEquals;

import java.io.File;
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
import com.jash.protokit.LibraryManagement.BookStatus;
import com.jash.protokit.LibraryManagement.Member;

public class ProtoComparerTest {

	private final Map<String, String> expectedReportMap = new HashMap<>();

	@BeforeClass
	public void getExpectedReports() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File("src/test/resources/cases/comparer.xml"));
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

	private Object[] getCompareRepeatedPrimCase1() {
		String caseName = "compareRepeatedPrimCase1";
		Object[] data = new Object[4];
		Member message1 = Member.newBuilder().addPhoneNumbers("1234567890").addPhoneNumbers("0987654321").build();
		Member message2 = Member.newBuilder().addPhoneNumbers("0987654321").addPhoneNumbers("9876543210").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = null;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	private Object[] getCompareRepeatedPrimCase2() {
		String caseName = "compareRepeatedPrimCase2";
		Object[] data = new Object[4];
		Member message1 = Member.newBuilder().addPhoneNumbers("1234567890").addPhoneNumbers("0987654321").build();
		Member message2 = Member.newBuilder().addPhoneNumbers("0987654321").addPhoneNumbers("9876543210").build();
		CompareOptions options = CompareOptions.Builder.newBuilder()
				.setFieldToOrderRepeatedMsg("Member.phoneNumbers", null).build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	private Object[] getCompareRepeatedMsgCase() {
		String caseName = "compareRepeatedMsgCase";
		Object[] data = new Object[4];
		Book book11 = Book.newBuilder().setBookId(1).setName("Book name 1").setAuthor("Author name 1")
				.setStatus(BookStatus.BORROWED).build();
		Book book12 = Book.newBuilder().setBookId(1).setGenre("Genre name 1").setAuthor("Author name 2").build();
		Book book2 = Book.newBuilder().setBookId(2).setName("Book name 2").setAuthor("Author name 2")
				.setStatus(BookStatus.BORROWED).build();
		Member message1 = Member.newBuilder().addBorrowHistory(book2).addBorrowHistory(book11).build();
		Member message2 = Member.newBuilder().addBorrowHistory(book12).build();
		CompareOptions options = CompareOptions.Builder.newBuilder()
				.setFieldToOrderRepeatedMsg("Member.borrowHistory", "Book.bookId").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	private Object[] getComparedRepeatedMsgWithKeyFieldCase() {
		String caseName = "compareRepeatedMsgWithKeyFieldCase";
		Object[] data = new Object[4];
		Book book11 = Book.newBuilder().setBookId(1).setName("Book name 1").setAuthor("Author name 1")
				.setStatus(BookStatus.BORROWED).build();
		Book book12 = Book.newBuilder().setBookId(1).setGenre("Genre name 1").setAuthor("Author name 2").build();
		Book book2 = Book.newBuilder().setBookId(2).setName("Book name 2").setAuthor("Author name 2")
				.setStatus(BookStatus.BORROWED).build();
		Member message1 = Member.newBuilder().addBorrowHistory(book2).addBorrowHistory(book11).build();
		Member message2 = Member.newBuilder().addBorrowHistory(book12).build();
		CompareOptions options = CompareOptions.Builder.newBuilder().setMessageKeyField(Book.class, "Book.bookId")
				.setFieldToOrderRepeatedMsg("Member.borrowHistory", "Book.bookId").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expectedReportMap.get(caseName);
		return data;
	}

	public Object[] getExcludeFieldCase() {
		String caseName = "excludeFieldCase";
		Object[] data = new Object[4];
		Book message1 = Book.newBuilder().setBookId(1).setName("Book name 1").setAuthor("Author name 1")
				.setStatus(BookStatus.BORROWED).build();
		Book message2 = Book.newBuilder().setBookId(1).setGenre("Genre name 1").setAuthor("Author name 2").build();
		CompareOptions options = CompareOptions.Builder.newBuilder().addExcludeField("Book.status").build();
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
		data.add(getCompareRepeatedPrimCase1());
		data.add(getCompareRepeatedPrimCase2());
		data.add(getCompareRepeatedMsgCase());
		data.add(getComparedRepeatedMsgWithKeyFieldCase());
		data.add(getExcludeFieldCase());
		return data.toArray(new Object[data.size()][]);
	}

	@Test(dataProvider = "dataProvider")
	public void testComparer(Message message1, Message message2, CompareOptions options, String expectedReport) {
		String report = ProtoComparer.compare(message1, message2, options);
		assertEquals(report, expectedReport);
	}

}
