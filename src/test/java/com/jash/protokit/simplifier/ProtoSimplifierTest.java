package com.jash.protokit.simplifier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.protobuf.Message;
import com.jash.protokit.LibraryManagement.Address;
import com.jash.protokit.LibraryManagement.AddressOld;
import com.jash.protokit.LibraryManagement.Book;
import com.jash.protokit.LibraryManagement.BookStatus;

public class ProtoSimplifierTest {

	private Object[] getDropFieldCase() {
		Object[] data = new Object[3];
		SimplifyOptions.Builder builder = SimplifyOptions.Builder.newBuilder();
		builder.addFieldToDrop("Address.postal_code");
		builder.addFieldToDrop("Address.country");
		SimplifyOptions options = builder.build();
		Address toBeSimplified = Address.newBuilder().setStreet("Street name").setCity("City name")
				.setState("State name").setPostalCode("Postal code").setCountry("Country name").build();
		Address expected = Address.newBuilder().setStreet("Street name").setCity("City name").setState("State name")
				.build();
		data[0] = toBeSimplified;
		data[1] = expected;
		data[2] = options;
		return data;
	}

	private Object[] getDropUnknownFieldCase() {
		Object[] data = new Object[3];
		Address newAddress = Address.newBuilder().setStreet("Street name").setCity("City name")
				.setState("State name").setPostalCode("Postal code").setCountry("Country name").build();
		AddressOld oldAddressFromNew = null;
		try {
			oldAddressFromNew = AddressOld.parseFrom(newAddress.toByteArray());
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse the message", e);
		}
		AddressOld expected = AddressOld.newBuilder().setStreet("Street name").setCity("City name")
				.setState("State name").setPostalCode("Postal code").build();
		data[0] = oldAddressFromNew;
		data[1] = expected;
		data[2] = null;
		return data;
	}

	private Object[] getDropDefaultFieldCase() {
		Object[] data = new Object[3];
		Book toBeSimplified = Book.newBuilder().setBookId(0).setName("Book name").setAuthor("Author name")
				.setGenre("Genre name").setStatus(BookStatus.AVAILABLE).build();
		Book expected = Book.newBuilder().setBookId(0).setName("Book name").setAuthor("Author name")
				.setGenre("Genre name").build();
		data[0] = toBeSimplified;
		data[1] = expected;
		data[2] = null;
		return data;
	}

	@DataProvider(name = "dataProvider")
	public Object[][] dataProvider() {
		Object[][] data = new Object[3][3];
		data[0] = getDropFieldCase();
		data[1] = getDropUnknownFieldCase();
		data[2] = getDropDefaultFieldCase();
		return data;
	}

	@Test(dataProvider = "dataProvider")
	public void testSimplifier(Message message, Message expected, SimplifyOptions options) {
		Message simplifiedMessage = ProtoSimplifier.simplifyMessage(message, options);
		Assert.assertEquals(simplifiedMessage, expected);
	}

}
