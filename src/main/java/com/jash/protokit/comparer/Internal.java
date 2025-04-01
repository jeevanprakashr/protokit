package com.jash.protokit.comparer;

import java.util.List;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;
import com.jash.protokit.internal.Util;

/**
 * This class is for internal use only and subject to change. It is not meant to
 * be used by clients. Refrain from using this class.
 */
public class Internal {

	protected static class UtilAdapter extends Util {

		protected static String getFieldName(String fieldFullName) {
			return Util.getFieldName(fieldFullName);
		}

		protected static List<FieldDescriptor> getAllFields(Builder alphaBuilder, Builder betaBuilder) {
			return Util.getAllFields(alphaBuilder, betaBuilder);
		}

		protected static Object getValue(Builder builder, FieldDescriptor field) {
			return Util.getValue(builder, field);
		}

	}

}
