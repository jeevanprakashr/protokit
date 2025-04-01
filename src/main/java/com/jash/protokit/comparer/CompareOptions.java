package com.jash.protokit.comparer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jash.protokit.comparer.Internal.UtilAdapter;

/**
 * A class to hold options for comparing two messages.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 * @see Builder
 */
public class CompareOptions {

	protected static final CompareOptions DUMMY_OPTIONS = new CompareOptions(Builder.newBuilder());

	private Map<Class<?>, String> messageVsKeyField;
	private Map<String, String> orderRepeatedMsgByField;
	private List<String> excludeFields;
	private List<String> redactionFields;
	private ProtoMapper protoMapper;

	private CompareOptions(Builder builder) {
		messageVsKeyField = builder.messageVsKeyField;
		orderRepeatedMsgByField = builder.orderRepeatedMsgByField;
		excludeFields = builder.excludeFields;
		redactionFields = builder.redactionFields;
		protoMapper = builder.protoMapper;
	}

	protected String getMessageKeyField(Class<?> messageClass) {
		return messageVsKeyField.get(messageClass);
	}

	protected boolean shouldOrderRepeatedMsg(String repeatedField) {
		return orderRepeatedMsgByField.containsKey(UtilAdapter.getFieldName(repeatedField));
	}

	protected String getOrderByFieldForRepeatedMsg(String repeatedField) {
		return orderRepeatedMsgByField.get(UtilAdapter.getFieldName(repeatedField));
	}

	protected boolean isFieldExcluded(String fieldFullName) {
		return excludeFields.contains(UtilAdapter.getFieldName(fieldFullName));
	}

	protected boolean isFieldRedacted(String fieldFullName) {
		return redactionFields.contains(UtilAdapter.getFieldName(fieldFullName));
	}

	protected boolean hasProtoMapper() {
		return protoMapper != null;
	}

	protected ProtoMapper getProtoMapper() {
		return protoMapper;
	}

	@Override
	public String toString() {
		return "CompareOptions [messageVsKeyField=" + messageVsKeyField + ", orderRepeatedMsgByField="
				+ orderRepeatedMsgByField + ", excludeFields=" + excludeFields + ", redactionFields=" + redactionFields
				+ ", protoMapper=" + protoMapper + "]";
	}

	/**
	 * Builder for {@link CompareOptions}.
	 */
	public static class Builder {

		private Map<Class<?>, String> messageVsKeyField;
		private Map<String, String> orderRepeatedMsgByField;
		private List<String> excludeFields;
		private List<String> redactionFields;
		private ProtoMapper protoMapper;

		private Builder() {
			messageVsKeyField = new HashMap<>();
			orderRepeatedMsgByField = new HashMap<>();
			excludeFields = new ArrayList<>();
			redactionFields = new ArrayList<>();
		}

		/**
		 * Create a new instance of {@link Builder}.
		 * 
		 * @return A new instance of {@link Builder}.
		 */
		public static Builder newBuilder() {
			return new Builder();
		}

		/**
		 * Set a field as key field for a message. This field will be used to identify a
		 * message in the comparison result.
		 * 
		 * @param messageClass - Class of the message.
		 * @param keyField     - The field to be used as key. Field format should be
		 *                     like "EncasingMessage.fieldName". E.g.:
		 *                     "SampleMessage.keyField"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setMessageKeyField(Class<?> messageClass, String keyField) {
			messageVsKeyField.put(messageClass, keyField);
			return this;
		}

		/**
		 * Set a field as key field for a message. This field will be used to identify a
		 * message in the comparison result.
		 * 
		 * @param messageVsKeyField - A map of message class vs key field. Field format
		 *                          should be like "EncasingMessage.fieldName". E.g.:
		 *                          "SampleMessage.keyField"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setAllMessageKeyFields(Map<Class<?>, String> messageVsKeyField) {
			this.messageVsKeyField.putAll(messageVsKeyField);
			return this;
		}

		/**
		 * Get the key field for a message.
		 * 
		 * @param messageClass
		 */
		public String getMessageKeyField(Class<?> messageClass) {
			return messageVsKeyField.get(messageClass);
		}

		/**
		 * Set a field to order a repeated message by. Can be used if the order of
		 * elements in a repeated field doesn't matter and needs to be compared one to
		 * one. The value for the field should be unique among the repeated elements. If
		 * the repeated field is of primitive type, the order by field can be set as
		 * null. Field format should be like "EncasingMessage.fieldName". E.g.:
		 * "SampleMessage.keyField" Field format should be like
		 * "EncasingMessage.fieldName". E.g.: "SampleMessage.keyField"
		 * 
		 * @param repeatedField - The repeated field to be ordered.
		 * @param orderByField  - The field to order by.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setFieldToOrderRepeatedMsg(String repeatedField, String orderByField) {
			orderRepeatedMsgByField.put(repeatedField, orderByField);
			return this;
		}

		/**
		 * Set a field to order a repeated message by. Can be used if the order of
		 * elements in a repeated field doesn't matter and needs to be compared one to
		 * one. The value for the field should be unique among the repeated elements. If
		 * the repeated field is of primitive type, the order by field can be set as
		 * null. Field format should be like "EncasingMessage.fieldName". E.g.:
		 * "SampleMessage.keyField"
		 * 
		 * @param orderRepeatedMsgByField - The map of repeated field vs field to order.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setFieldsToOrderRepeatedMsg(Map<String, String> orderRepeatedMsgByField) {
			this.orderRepeatedMsgByField.putAll(orderRepeatedMsgByField);
			return this;
		}

		/**
		 * Get all fields to order repeated messages by.
		 */
		public Map<String, String> getOrderRepeatedMsgByField() {
			return orderRepeatedMsgByField;
		}

		/**
		 * Add a field to be excluded from comparison.
		 * 
		 * @param field - Field to be excluded. Field format should be like
		 *              "EncasingMessage.fieldName". E.g.: "SampleMessage.field"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder addExcludeField(String field) {
			excludeFields.add(field);
			return this;
		}

		/**
		 * Add all fields to be excluded from comparison.
		 * 
		 * @param fields - Fields to be excluded. Field format should be like
		 *               "EncasingMessage.fieldName". E.g.: "SampleMessage.keyField"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder allAllExcludeFields(List<String> fields) {
			excludeFields.addAll(fields);
			return this;
		}

		/**
		 * Get all fields to exclude from comparison.
		 */
		public List<String> getExcludeFields() {
			return excludeFields;
		}

		/**
		 * Add a field to be redacted from comparison. Can be used if the field value
		 * contains sensitive information and needs to be redacted in comparison report
		 * 
		 * @param field - Field to be redacted. Field format should be like
		 *              "EncasingMessage.fieldName". E.g.: "SampleMessage.field"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder addRedactionField(String field) {
			redactionFields.add(field);
			return this;
		}

		/**
		 * Add all fields to be redacted from comparison. Can be used if the field value
		 * contains sensitive information and needs to be redacted in comparison report
		 * 
		 * @param fields - Fields to be redacted. Field format should be like
		 *               "EncasingMessage.fieldName". E.g.: "SampleMessage.field"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder addAllRedactionFields(List<String> fields) {
			redactionFields.addAll(fields);
			return this;
		}

		/**
		 * Get all fields to redact from comparison.
		 */
		public List<String> getRedactionFields() {
			return redactionFields;
		}

		/**
		 * Set a {@link ProtoMapper} to map fields for comparison.
		 * 
		 * @param protoMapper - The {@link ProtoMapper} instance.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setProtoMapper(ProtoMapper protoMapper) {
			this.protoMapper = protoMapper;
			return this;
		}

		/**
		 * Get the {@link ProtoMapper} instance.
		 */
		public ProtoMapper getProtoMapper() {
			return protoMapper;
		}

		/**
		 * Build the {@link CompareOptions} instance.
		 * 
		 * @return The {@link CompareOptions} instance.
		 */
		public CompareOptions build() {
			return new CompareOptions(this);
		}

	}

}
