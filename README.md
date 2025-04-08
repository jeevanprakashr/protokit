# Protokit
A utility library to compare, merge and simplify Google Protocol Buffers. Please refer [this](src/test/resources/library.proto) proto file for following samples.

## Proto Comparer
Compare two protobuffer messages and get a beautified comparison report.
```java
Address message1 = Address.newBuilder().setStreet("Street name").setCity("City name 1")
        .setState("State name").setCountry("Country name").build();
Address message2 = Address.newBuilder().setStreet("Street name").setCity("City name 2")
        .setState("State name").setPostalCode("Postal code").build();

String compareReport = ProtoComparer.compare(message1, message2);
System.out.println(compareReport);
```
<b>Output:</b>
```
  Address: {
    city: City name 1 => City name 2
+   postal_code: Postal code
-   country: Country name
  }
```
<br>

In case of a more complex protobuffers, ```CompareOptions``` can be used to compare.
```java
Book book11 = Book.newBuilder().setBookId(1).setName("Book name 1").setAuthor("Author name 1")
				.setStatus(BookStatus.BORROWED).build();
Book book12 = Book.newBuilder().setBookId(1).setGenre("Genre name 1").setAuthor("Author name 2").build();
Book book2 = Book.newBuilder().setBookId(2).setName("Book name 2").setAuthor("Author name 2")
				.setStatus(BookStatus.BORROWED).build();
Member message1 = Member.newBuilder().addBorrowHistory(book2).addBorrowHistory(book11).build();
Member message2 = Member.newBuilder().addBorrowHistory(book12).build();

CompareOptions options = CompareOptions.Builder.newBuilder().setMessageKeyField(Book.class, "Book.bookId")
				.setFieldToOrderRepeatedMsg("Member.borrowHistory", "Book.bookId").build();

String compareReport = ProtoComparer.compare(message1, message2, options);
System.out.println(compareReport);
```
<b>Output:</b>
```
  Member: {
    borrowHistory: [
      borrowHistory (1): {
-       name: Book name 1
        author: Author name 1 => Author name 2
+       genre: Genre name 1
-       status: BORROWED
      }
-     borrowHistory: {
-       bookId: 2
-       name: Book name 2
-       author: Author name 2
-       status: BORROWED
-     }
    ]
  }
```
## Proto Merger
Merge two protobuffer messages into each other to make them equal.
```java
Book message1 = Book.newBuilder().setBookId(1).setStatus(BookStatus.BORROWED).build();
Book message2 = Book.newBuilder().setName("Book name").setAuthor("Author name")
          .setGenre("Genre name").build();

Result<Book> result = ProtoMerger.merge(message1, message2);
Book res1 = result.getFirst();
Book res2 = result.getSecond();
System.out.println(res1.equals(res2));
```
<b>Output:</b>
```
true
```
<br>

In case of a more complex protobuffers or conflicts while merging, ```MergeOptions``` can be used to resolve the merging.
```java
Book book11 = Book.newBuilder().setBookId(1).setPrice(1000).build();
Book book12 = Book.newBuilder().setBookId(1).setPrice(1500).build();
Book book2 = Book.newBuilder().setBookId(2).setPrice(2000).build();
Member message1 = Member.newBuilder().setMemberId(1).addBorrowHistory(book2).addBorrowHistory(book12).build();
Member message2 = Member.newBuilder().setMemberId(1).addBorrowHistory(book11).build();
MergeOptions options = MergeOptions.Builder.newBuilder()
				.setMergeRepeatedByField("Member.borrowHistory", "Book.bookId")
				.setConflictResolver("Book.price", Resolver.GREATER).build();

Result<Member> result = ProtoMerger.merge(message1, message2, options);
System.out.println(result.getFirst().equals(result.getSecond()));
```

<b>Output:</b>
```
true
```
## Proto Simplifier
Simplify a protobuffer message by dropping default values (in proto2), unknown fields and unnecessary fields.
```java
try {
	Address newAddress = Address.newBuilder().setStreet("Street name").setCity("City name")
			.setState("State name").setPostalCode("Postal code").setCountry("Country name").build();
	AddressOld oldAddressFromNew = AddressOld.parseFrom(newAddress.toByteArray());
	AddressOld expected = AddressOld.newBuilder().setStreet("Street name").setCity("City name")
			.setState("State name").setPostalCode("Postal code").build();
	AddressOld simplified = ProtoSimplifier.simplifyMessage(oldAddressFromNew);
	System.out.println(simplified.toString().equals(expected.toString()));
} catch (Exception e) {
	throw new IllegalArgumentException("Failed to parse the message", e);
}
```
<b>Output:</b>
```
true
```
<br>

In case if an actual field needs to be dropped from a message,  ```SimplifyOptions``` can be used.
```java
Address toBeSimplified = Address.newBuilder().setStreet("Street name").setCity("City name")
				.setState("State name").setPostalCode("Postal code").setCountry("Country name").build();
Address expected = Address.newBuilder().setStreet("Street name").setCity("City name").setState("State name")
				.build();
SimplifyOptions.Builder builder = SimplifyOptions.Builder.newBuilder();
builder.addFieldToDrop("Address.postal_code");
builder.addFieldToDrop("Address.country");
SimplifyOptions options = builder.build();

Address simplified = ProtoSimplifier.simplifyMessage(toBeSimplified, options);
System.out.println(simplified.toString().equals(expected.toString()));
```
<b>Output:</b>
```
true
```
