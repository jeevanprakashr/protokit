package com.jash.protokit.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;

/**
 * This class is for internal use only and subject to change. It is not meant to
 * be used by clients. Refrain from using this class.
 */
public class Util {

	/**
	 * Get the last two parts of the field's full name, i.e., the encasing message
	 * class and field name. Have seen cases where the full name is the entire
	 * hierarchy/address of the field like "EncasingMessage.NestedMessage.fieldName"
	 * instead of just "NestedMessage.fieldName".
	 * 
	 * @param fieldFullName
	 */
	protected static String getFieldName(String fieldFullName) {
		if (fieldFullName == null) {
			return "";
		}
		String[] parts = fieldFullName.split("\\.");
		String[] lastTwoParts = Arrays.copyOfRange(parts, parts.length - 2, parts.length);
		return String.join(".", lastTwoParts);
	}

	protected static List<FieldDescriptor> getAllFields(Builder alphaBuilder, Builder betaBuilder) {
		if (alphaBuilder != null) {
			return alphaBuilder.getDescriptorForType().getFields();
		} else if (betaBuilder != null) {
			return betaBuilder.getDescriptorForType().getFields();
		}
		return Collections.emptyList();
	}

	protected static Object getValue(Builder builder, FieldDescriptor field) {
		if (builder != null && (field.isRepeated() || builder.hasField(field))) {
			return builder.getField(field);
		}
		return null;
	}

}
