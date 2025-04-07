package com.jash.protokit.merger;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.protobuf.Message;
import com.jash.protokit.LibraryManagement.Address;
import com.jash.protokit.LibraryManagement.Book;
import com.jash.protokit.LibraryManagement.BookStatus;
import com.jash.protokit.LibraryManagement.Member;
import com.jash.protokit.merger.MergeOptions.Resolver;

public class ProtoMergerTest {

	private Object[] getMergeMissingFieldsCase() {
		Object[] data = new Object[5];
		Book message1 = Book.newBuilder().setBookId(1).setStatus(BookStatus.BORROWED).build();
		Book message2 = Book.newBuilder().setName("Book name").setAuthor("Author name").setGenre("Genre name").build();
		Book expected = Book.newBuilder().setBookId(1).setName("Book name").setAuthor("Author name")
				.setGenre("Genre name").setStatus(BookStatus.BORROWED).build();
		data[0] = message1;
		data[1] = message2;
		data[2] = null;
		data[3] = expected;
		data[4] = expected;
		return data;
	}

	private Object[] getResolverCase1(Resolver resolver) {
		Object[] data = new Object[5];
		Member message1 = Member.newBuilder().setMemberId(1).setBooksOnHold(5).build();
		Member message2 = Member.newBuilder().setMemberId(1).setBooksOnHold(10).build();
		int expectedBooksOnHold = 10;
		switch (resolver) {
		case GREATER:
			expectedBooksOnHold = 10;
			break;
		case LESSER:
			expectedBooksOnHold = 5;
			break;
		case FIRST:
			expectedBooksOnHold = 5;
			break;
		case SECOND:
			expectedBooksOnHold = 10;
			break;
		case DEFAULT:
			expectedBooksOnHold = 0;
		default:
			break;
		}
		Member expected = Member.newBuilder().setMemberId(1).setBooksOnHold(expectedBooksOnHold).build();
		MergeOptions options = MergeOptions.Builder.newBuilder().setConflictResolver("Member.booksOnHold", resolver)
				.build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expected;
		data[4] = expected;
		return data;
	}

	private Object[] getResolverCase2(Resolver resolver) {
		Object[] data = new Object[5];
		Book book11 = Book.newBuilder().setBookId(1).setPrice(1000).build();
		Book book12 = Book.newBuilder().setBookId(1).setPrice(1500).build();
		Book book2 = Book.newBuilder().setBookId(2).setPrice(2000).build();
		Member message1 = Member.newBuilder().setMemberId(1).addBorrowHistory(book2).addBorrowHistory(book12).build();
		Member message2 = Member.newBuilder().setMemberId(1).addBorrowHistory(book11).build();
		int expectedPrice = 1000;
		switch (resolver) {
		case GREATER:
			expectedPrice = 1500;
			break;
		case LESSER:
			expectedPrice = 1000;
			break;
		case FIRST:
			expectedPrice = 1500;
			break;
		case SECOND:
			expectedPrice = 1000;
			break;
		case DEFAULT:
			expectedPrice = 0;
		default:
			break;
		}
		Book book1 = Book.newBuilder().setBookId(1).setPrice(expectedPrice).build();
		Member expected = Member.newBuilder().setMemberId(1).addBorrowHistory(book1).addBorrowHistory(book2).build();
		MergeOptions options = MergeOptions.Builder.newBuilder()
				.setMergeRepeatedByField("Member.borrowHistory", "Book.bookId")
				.setConflictResolver("Book.price", resolver).build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expected;
		data[4] = expected;
		return data;
	}

	private Object[] getExcludeFieldCase() {
		Object[] data = new Object[5];
		Address message1 = Address.newBuilder().setStreet("Street name").setState("State name")
				.setPostalCode("Postal code").build();
		Address message2 = Address.newBuilder().setCity("City name").setCountry("Country name").build();
		Address expected1 = Address.newBuilder().setStreet("Street name").setCity("City name").setState("State name")
				.setPostalCode("Postal code").build();
		Address expected2 = Address.newBuilder().setStreet("Street name").setCity("City name").setState("State name")
				.setPostalCode("Postal code").setCountry("Country name").build();
		MergeOptions options = MergeOptions.Builder.newBuilder().addExcludeField("Address.country").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expected1;
		data[4] = expected2;
		return data;
	}

	private Object[] getPrimitiveRepeatedCase() {
		Object[] data = new Object[5];
		List<String> phoneNumbers1 = Arrays.asList("1234567890", "0987654321");
		List<String> phoneNumbers2 = Arrays.asList("0987654321", "9876543210", "8765432109");
		List<String> expectedPhoneNumbers1 = Arrays.asList("1234567890", "0987654321", "9876543210", "8765432109");
		List<String> expectedPhoneNumbers2 = Arrays.asList("0987654321", "9876543210", "8765432109", "1234567890");
		Member message1 = Member.newBuilder().addAllPhoneNumbers(phoneNumbers1).build();
		Member message2 = Member.newBuilder().addAllPhoneNumbers(phoneNumbers2).build();
		Member expected1 = Member.newBuilder().addAllPhoneNumbers(expectedPhoneNumbers1).build();
		Member expected2 = Member.newBuilder().addAllPhoneNumbers(expectedPhoneNumbers2).build();
		MergeOptions options = MergeOptions.Builder.newBuilder()
				.setMergeRepeatedByField("Member.phoneNumbers", null).build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expected1;
		data[4] = expected2;
		return data;
	}

	private Object[] getMergeRepeatedByFieldCase() {
		Object[] data = new Object[5];
		Book book1 = Book.newBuilder().setBookId(1).setName("Book name").setAuthor("Author name").build();
		Book book2 = Book.newBuilder().setBookId(1).setGenre("Genre name").setStatus(BookStatus.BORROWED).build();
		Book book3 = Book.newBuilder().setBookId(2).setName("Book name 2").setAuthor("Author name 2")
				.setGenre("Genre name 2").setStatus(BookStatus.AVAILABLE).build();
		Book book4 = Book.newBuilder().setBookId(3).setName("Book name 3").setAuthor("Author name 3")
				.setGenre("Genre name 3").setStatus(BookStatus.BORROWED).build();
		Book book5 = Book.newBuilder().setBookId(1).setName("Book name").setAuthor("Author name").setGenre("Genre name")
				.setStatus(BookStatus.BORROWED).build();
		Member message1 = Member.newBuilder().addBorrowHistory(book3).addBorrowHistory(book1).build();
		Member message2 = Member.newBuilder().addBorrowHistory(book2).addBorrowHistory(book4).build();
		Member expected = Member.newBuilder().addBorrowHistory(book5).addBorrowHistory(book3).addBorrowHistory(book4)
				.build();
		MergeOptions options = MergeOptions.Builder.newBuilder()
				.setMergeRepeatedByField("Member.borrowHistory", "Book.bookId").build();
		data[0] = message1;
		data[1] = message2;
		data[2] = options;
		data[3] = expected;
		data[4] = expected;
		return data;
	}

	@DataProvider(name = "dataProvider")
	public Object[][] dataProvider() {
		List<Object[]> data = new ArrayList<>();
		data.add(getMergeMissingFieldsCase());
		for (Resolver resolver : Resolver.values()) {
			data.add(getResolverCase1(resolver));
			data.add(getResolverCase2(resolver));
		}
		data.add(getExcludeFieldCase());
		data.add(getPrimitiveRepeatedCase());
		data.add(getMergeRepeatedByFieldCase());
		return data.toArray(new Object[data.size()][]);
	}

	@Test(dataProvider = "dataProvider")
	public void testMerger(Message message1, Message message2, MergeOptions options, Message expected1,
			Message expected2) {
		Result<Message> result = ProtoMerger.merge(message1, message2, options);
		assertEquals(result.getFirst(), expected1);
		assertEquals(result.getSecond(), expected2);
	}

}
