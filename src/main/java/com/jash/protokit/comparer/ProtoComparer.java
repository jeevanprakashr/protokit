package com.jash.protokit.comparer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;

/**
 * Utility class to compare two messages.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 */
public class ProtoComparer {

	private static final String MESSAGE_START = "{\n";
	private static final String MESSAGE_END = "}\n";
	private static final String LIST_START = "[\n";
	private static final String LIST_END = "]\n";
	private static final String COLON_SPACE = ": ";

	private static final int LEVEL_SPACE_COUNT = 2;

	private static final char SIGN_CREATE = '+';
	private static final char SIGN_UPDATE = ' ';
	private static final char SIGN_DELETE = '-';
	private static final char SIGN_EMPTY = ' ';

	/**
	 * Compare two messages and return the differences.
	 * 
	 * @param m1 - The first message.
	 * @param m2 - The second message.
	 * @return The differences between the two messages.
	 */
	public static String compare(Message m1, Message m2, CompareOptions options) {
		if (Objects.equals(m1, m2)) {
			// No diff
			return new String();
		}
		if (options == null) {
			options = CompareOptions.DUMMY_OPTIONS;
		}
		String protoBufName = null;
		if (m1 != null) {
			protoBufName = m1.getDescriptorForType().getName();
		} else if (m2 != null) {
			protoBufName = m2.getDescriptorForType().getName();
		}
		if (m1 != null && m2 != null && !m1.getClass().equals(m2.getClass())) {
			throw new IllegalArgumentException(
					"Cannot compare different messages: " + m1.getClass() + " - " + m2.getClass());
		}
		StringBuilder sb = new StringBuilder(512);
		compareMessageField(m1, m2, protoBufName, options, sb, 0);
		return sb.toString();
	}

	private static void compareMessageField(Message m1, Message m2, String messageName, CompareOptions options,
			StringBuilder sb, int indent) {
		Object v1 = null, v2 = null;
		char sign = getSign(m1, m2);
		StringBuilder diffSb = new StringBuilder();
		String keyFieldVal = "";
		String keyField = null;
		if (m1 != null) {
			keyField = options.getMessageKeyField(m1.getClass());
		} else if (m2 != null) {
			keyField = options.getMessageKeyField(m2.getClass());
		}
		List<FieldDescriptor> fields = getAllFields(m1, m2);
		for (FieldDescriptor field : fields) {
			// Skip if field is excluded
			if (options.isFieldExcluded(field.getFullName())) {
				continue;
			}
			// Skip if values are equal
			v1 = getValue(m1, field);
			v2 = getValue(m2, field);
			if (Objects.equals(v1, v2)) {
				if (keyField != null && field.getFullName().endsWith(keyField)) {
					keyFieldVal = v1.toString();
				}
				continue;
			}
			// use mapped values if provided
			if (options.hasProtoMapper() && options.getProtoMapper().shouldUseMappedValue()) {
				v1 = options.getProtoMapper().map(field.getFullName(), v1, true);
				v2 = options.getProtoMapper().map(field.getFullName(), v2, false);
				if (Objects.equals(v1, v2)) {
					continue;
				}
			}
			if (field.getJavaType() != JavaType.MESSAGE && (v1 == null || v2 == null)) {
				if ((v1 != null && Objects.equals(v1, field.getDefaultValue()))
						|| (v2 != null && Objects.equals(v2, field.getDefaultValue()))) {
					continue;
				}
			}
			// Write field
			compareField(field, v1, v2, options, diffSb, indent + 1);
		}
		if (!diffSb.toString().trim().isEmpty()) {
			// Write message start
			sb.append(getPrefix(sign, indent)).append(messageName);
			if (!keyFieldVal.isEmpty()) {
				sb.append(" (").append(keyFieldVal).append(")");
			}
			sb.append(COLON_SPACE).append(MESSAGE_START);
			sb.append(diffSb.toString());
			// Write message end
			sb.append(getPrefix(sign, indent)).append(MESSAGE_END);
		}
	}

	private static void compareField(FieldDescriptor field, Object v1, Object v2, CompareOptions options,
			StringBuilder sb, int indent) {
		if (field.isRepeated()) {
			compareRepeatedField(field, v1, v2, options, sb, indent);
		} else {
			compareSingleField(field, v1, v2, options, sb, indent);
		}
	}

	private static void compareRepeatedField(FieldDescriptor field, Object v1, Object v2, CompareOptions options,
			StringBuilder sb, int indent) {
		List<?> l1 = new ArrayList<>(v1 != null ? (List<?>) v1 : Collections.emptyList());
		List<?> l2 = new ArrayList<>(v2 != null ? (List<?>) v2 : Collections.emptyList());
		if (options.shouldOrderRepeatedMsg(field.getFullName())) {
			List<?> intersection = new ArrayList<>(l1);
			intersection.retainAll(l2);
			for (Object val : intersection) {
				if (!l1.contains(val) || !l2.contains(val)) {
					continue;
				}
				l1.remove(val);
				l2.remove(val);
			}
			if (field.getJavaType() == JavaType.MESSAGE) {
				String orderByField = options.getOrderByFieldForRepeatedMsg(field.getFullName());
				if (orderByField == null) {
					throw new IllegalArgumentException(
							"Order by field not found for repeated message field: " + field.getFullName());
				}
				Function<Object, Object> mapper = message -> {
					Message msg = (Message) message;
					Object val = null;
					for (FieldDescriptor f : getAllFields(msg, null)) {
						if (f.getFullName().endsWith(orderByField)) {
							val = getValue(msg, f);
							break;
						}
					}
					return val;
				};
				List<?> mappedl1 = l1.stream().map(mapper).collect(Collectors.toList());
				List<?> mappedl2 = l2.stream().map(mapper).collect(Collectors.toList());
				int size1 = new HashSet<>(mappedl1).size();
				int size2 = new HashSet<>(mappedl2).size();
				if (l1.size() == size1 && l2.size() == size2) {
					// mapped values need to be unique
					intersection = new ArrayList<>(mappedl1);
					intersection.retainAll(mappedl2);
					Object[] orderedl1 = new Object[l1.size()];
					Object[] orderedl2 = new Object[l2.size()];
					int j = intersection.size();
					for (int i = 0; i < l1.size(); i++) {
						Object val = l1.get(i);
						Object mappedVal = mappedl1.get(i);
						int index = intersection.indexOf(mappedVal);
						if (index >= 0) {
							orderedl1[index] = val;
						} else {
							orderedl1[j++] = val;
						}
					}
					j = intersection.size();
					for (int i = 0; i < l2.size(); i++) {
						Object val = l2.get(i);
						Object mappedVal = mappedl2.get(i);
						int index = intersection.indexOf(mappedVal);
						if (index >= 0) {
							orderedl2[index] = val;
						} else {
							orderedl2[j++] = val;
						}
					}
					l1 = Arrays.asList(orderedl1);
					l2 = Arrays.asList(orderedl2);
				} else {
					throw new IllegalArgumentException("Mapped values are not unique for repeated message field: "
							+ field.getFullName() + " - " + orderByField);
				}
			}
		}
		if (Objects.equals(l1, l2)) {
			return;
		}
		Object value1, value2;
		int n = Math.max(l1.size(), l2.size());
		char sign = getSign(v1, v2);

		StringBuilder diffSb = new StringBuilder();

		// Iterate and write each field
		for (int i = 0; i < n; i++) {
			value1 = i < l1.size() ? l1.get(i) : null;
			value2 = i < l2.size() ? l2.get(i) : null;
			// Skip if values are equal
			if (Objects.equals(value1, value2)) {
				continue;
			}
			compareSingleField(field, value1, value2, options, diffSb, indent + 1);
		}

		if (!diffSb.toString().trim().isEmpty()) {
			// Write repeated field start
			sb.append(getPrefix(sign, indent)).append(field.getName());
			sb.append(COLON_SPACE).append(LIST_START);
			sb.append(diffSb);
			// Write repeated field end
			sb.append(getPrefix(sign, indent)).append(LIST_END);
		}
	}

	private static void compareSingleField(FieldDescriptor field, Object v1, Object v2, CompareOptions options,
			StringBuilder sb, int indent) {
		if (field.getJavaType() == JavaType.MESSAGE) {
			compareMessageField((Message) v1, (Message) v2, field.getName(), options, sb, indent);
		} else {
			comparePrimitiveField(field, v1, v2, options, sb, indent);
		}
	}

	private static void comparePrimitiveField(FieldDescriptor field, Object v1, Object v2, CompareOptions options,
			StringBuilder sb, int indent) {
		char sign = getSign(v1, v2);
		sb.append(getPrefix(sign, indent));
		sb.append(field.getName());
		sb.append(COLON_SPACE);
		if (sign == SIGN_UPDATE) {
			sb.append(valueToString(field, v1, options, true));
			sb.append(" => ");
			sb.append(valueToString(field, v2, options, false));
		} else if (sign == SIGN_CREATE) {
			sb.append(valueToString(field, v2, options, false));
		} else if (sign == SIGN_DELETE) {
			sb.append(valueToString(field, v1, options, true));
		}
		sb.append('\n');
	}

	private static String valueToString(FieldDescriptor field, Object value, CompareOptions options, boolean firstObj) {
		if (options.isFieldRedacted(field.getFullName())) {
			return "****";
		}
		String returnVal = String.valueOf(value);
		if (field.getJavaType() == JavaType.ENUM) {
			returnVal = ((Descriptors.EnumValueDescriptor) value).getName();
		}
		if (options.hasProtoMapper() && !options.getProtoMapper().shouldUseMappedValue()) {
			Object mappedValue = options.getProtoMapper().map(field.getFullName(), value, firstObj);
			if (Objects.equals(mappedValue, value)) {
				return returnVal;
			} else {
				return returnVal + " (" + String.valueOf(mappedValue) + ")";
			}
		}
		return returnVal;
	}

	private static char[] getPrefix(char sign, int indent) {
		char[] indentBuf = new char[(indent * LEVEL_SPACE_COUNT) + 2];
		Arrays.fill(indentBuf, SIGN_EMPTY);
		indentBuf[0] = sign;
		return indentBuf;
	}

	private static List<FieldDescriptor> getAllFields(Message m1, Message m2) {
		if (m1 != null) {
			return m1.getDescriptorForType().getFields();
		} else if (m2 != null) {
			return m2.getDescriptorForType().getFields();
		}
		return Collections.emptyList();
	}

	private static Object getValue(Message message, FieldDescriptor field) {
		if (message != null && (field.isRepeated() || message.hasField(field))) {
			return message.getField(field);
		}
		return null;
	}

	private static char getSign(Object v1, Object v2) {
		if (v1 != null && v2 != null) {
			return SIGN_UPDATE;
		} else if (v1 == null) {
			return SIGN_CREATE;
		} else if (v2 == null) {
			return SIGN_DELETE;
		}
		// Should not get here
		return SIGN_EMPTY;
	}

}
