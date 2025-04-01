package com.jash.protokit.simplifier;

import java.util.Objects;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.UnknownFieldSet;
import com.jash.protokit.simplifier.Internal.UtilAdapter;

/**
 * Utility class to simplify a proto message by<br>
 * 1. Removing default values if set<br>
 * 2. Removing unknown fields<br>
 * 2. Dropping fields as per the options<br>
 */
public class ProtoSimplifier {

	/**
	 * Simplify a message by removing default values, unknown fields, and fields as
	 * per the options.
	 * 
	 * @param message - The message to simplify.
	 * @param options - The options to simplify the message.
	 * @return The simplified message.
	 */
	public static <E extends Message> E simplifyMessage(E message, SimplifyOptions options) {
		if (options == null) {
			options = SimplifyOptions.DUMMY_OPTIONS;
		}
		E.Builder builder = message.toBuilder();
		if (!builder.getUnknownFields().asMap().isEmpty()) {
			builder.setUnknownFields(UnknownFieldSet.getDefaultInstance());
		}
		Object value = null;
		for (FieldDescriptor field : UtilAdapter.getAllFields(builder, null)) {
			if (options.canDropField(field.getFullName())) {
				builder.clearField(field);
				continue;
			}
			value = UtilAdapter.getValue(builder, field);
			if (value == null) {
				continue;
			}
			simplifyField(builder, field, value, options);
		}
		E simplifiedMsg = (E) builder.build();
		return simplifiedMsg;
	}

	private static void simplifyField(Builder builder, FieldDescriptor field, Object value,
			SimplifyOptions options) {
		if (field.isRepeated()) {
			simplifyRepeatedField(builder, field, options);
		} else {
			simplifySingleField(builder, field, value, options);
		}
	}

	private static void simplifyRepeatedField(Builder builder, FieldDescriptor field, SimplifyOptions options) {
		if (field.getJavaType() != JavaType.MESSAGE) {
			return;
		}
		Message msg = null;
		Message simplifiedMsg = null;
		for (int i = 0; i < builder.getRepeatedFieldCount(field); i++) {
			msg = (Message) builder.getRepeatedField(field, i);
			simplifiedMsg = simplifyMessage(msg, options);
			if (Objects.equals(simplifiedMsg, msg.getDefaultInstanceForType())) {
				builder.setRepeatedField(field, i, msg.getDefaultInstanceForType());
			} else {
				builder.setRepeatedField(field, i, simplifiedMsg);
			}
		}
	}

	private static void simplifySingleField(Builder builder, FieldDescriptor field, Object value,
			SimplifyOptions options) {
		if (field.getJavaType() == JavaType.MESSAGE) {
			Message msg = (Message) value;
			if (Objects.equals(msg, msg.getDefaultInstanceForType())) {
				builder.clearField(field);
			}
			Message simplifiedMsg = simplifyMessage(msg, options);
			if (Objects.equals(simplifiedMsg, msg.getDefaultInstanceForType())) {
				builder.clearField(field);
			} else {
				builder.setField(field, simplifiedMsg);
			}
		} else {
			if (Objects.equals(value, field.getDefaultValue())) {
				builder.clearField(field);
			}
		}
	}

}
