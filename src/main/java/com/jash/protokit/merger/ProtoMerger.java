package com.jash.protokit.merger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.jash.protokit.merger.Internal.UtilAdapter;
import com.jash.protokit.merger.MergeOptions.Resolver;

/**
 * Utility class to merge two protobuf message builders into each other inplace.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 */
public class ProtoMerger {

	/**
	 * Merge the alpha and beta messages into each other.
	 * 
	 * @param alpha - alpha message to merge into from beta message.
	 * @param beta  - beta message to merge into from alpha message.
	 * @return A {@link Result} object containing the merged messages.
	 */
	public static <T extends Message> Result<T> merge(T alpha, T beta, MergeOptions options) {
		Builder alphaBuilder = alpha.toBuilder();
		Builder betaBuilder = beta.toBuilder();
		merge(alphaBuilder, betaBuilder, options);
		@SuppressWarnings("unchecked")
		T first = (T) alphaBuilder.build();
		@SuppressWarnings("unchecked")
		T second = (T) betaBuilder.build();
		return new Result<T>(first, second);
	}

	/**
	 * Merge the alpha and beta builders into each other inplace.
	 * 
	 * @param alphaBuilder - alpha builder to merge into from beta builder.
	 * @param betaBuilder  - beta builder to merge into from alpha builder.
	 * 
	 * @throws IllegalArgumentException if the builders are of different types.
	 */
	public static void merge(Builder alphaBuilder, Builder betaBuilder, MergeOptions options) {
		if (Objects.equals(alphaBuilder.build(), betaBuilder.build())) {
			// No diff
			return;
		}
		if (alphaBuilder != null && betaBuilder != null && !alphaBuilder.getClass().equals(betaBuilder.getClass())) {
			throw new IllegalArgumentException(
					"Cannot merge different messages: " + alphaBuilder.getClass() + " - " + betaBuilder.getClass());
		}
		if (options == null) {
			options = MergeOptions.DUMMY_OPTIONS;
		}
		mergeBuilders(alphaBuilder, betaBuilder, options);
	}

	private static void mergeBuilders(Builder alphaBuilder, Builder betaBuilder, MergeOptions options) {
		Object alphaValue = null, betaValue = null;
		for (FieldDescriptor field : UtilAdapter.getAllFields(alphaBuilder, betaBuilder)) {
			alphaValue = UtilAdapter.getValue(alphaBuilder, field);
			betaValue = UtilAdapter.getValue(betaBuilder, field);
			if (Objects.equals(alphaValue, betaValue) || options.isFieldExcluded(field.getFullName())) {
                continue;
            }
			mergeField(field, alphaBuilder, betaBuilder, alphaValue, betaValue, options);
        }
	}

	private static void mergeField(FieldDescriptor field, Builder alphaBuilder, Builder betaBuilder, Object alphaValue,
			Object betaValue, MergeOptions options) {
		if (field.isRepeated()) {
			mergeRepeatedField(field, alphaBuilder, betaBuilder, alphaValue, betaValue, options);
		} else {
			mergeSingleField(field, alphaBuilder, betaBuilder, alphaValue, betaValue, options);
		}
	}

	private static void mergeRepeatedField(FieldDescriptor field, Builder alphaBuilder, Builder betaBuilder,
			Object alphaValue, Object betaValue, MergeOptions options) {
		List<Object> l1 = new ArrayList<>(alphaValue != null ? (List<Object>) alphaValue : Collections.emptyList());
		List<Object> l2 = new ArrayList<>(betaValue != null ? (List<Object>) betaValue : Collections.emptyList());
		if (l1.isEmpty()) {
			alphaBuilder.setField(field, betaValue);
		} else if (l2.isEmpty()) {
			betaBuilder.setField(field, alphaValue);
		} else {
			// mergeByField can be deliberately set to null if the repeated field is of
			// primitive type and hence mergeRepeated
			boolean mergeRepeated = options.shouldMergeRepeatedField(field.getFullName());
			String mergeByField = options.getMergeByFieldForRepeatedField(field.getFullName());
			if (!mergeRepeated) {
				return;
			}
			if (field.getJavaType() == JavaType.MESSAGE) {
				if (mergeByField == null) {
					throw new RuntimeException(
							"Have to provide a field name to merge the repeated field " + field.getFullName());
				}
				Function<Object, Object> mapper = msgObj -> {
					Message msg = (Message) msgObj;
					Builder builder = msg.toBuilder();
					Object val = null;
					for (FieldDescriptor f : UtilAdapter.getAllFields(builder, null)) {
						if (f.getFullName().endsWith(mergeByField)) {
							val = UtilAdapter.getValue(builder, f);
							break;
						}
					}
					return val;
				};
				List<Object> mappedl1 = l1.stream().map(mapper).collect(Collectors.toList());
				List<Object> mappedl2 = l2.stream().map(mapper).collect(Collectors.toList());
				List<Object> intersection = new ArrayList<>(mappedl1);
				intersection.retainAll(mappedl2);
				List<Object> templ1 = new ArrayList<>(l1);
				List<Object> templ2 = new ArrayList<>(l2);
				l1.clear();
				l2.clear();
				while (!intersection.isEmpty()) {
					Object obj = intersection.remove(0);
					int idx1 = mappedl1.indexOf(obj);
					int idx2 = mappedl2.indexOf(obj);
					if (idx1 == -1 || idx2 == -1) {
						continue;
					}
					l1.add(templ1.remove(idx1));
					l2.add(templ2.remove(idx2));
					mappedl1.remove(idx1);
					mappedl2.remove(idx2);
				}
				l1.addAll(templ1);
				l1.addAll(templ2);
				l2.addAll(templ1);
				l2.addAll(templ2);
				alphaBuilder.clearField(field);
				betaBuilder.clearField(field);
				for (int i = 0; i < l1.size(); i++) {
					Message msg1 = (Message) l1.get(i);
					Message msg2 = (Message) l2.get(i);
					Builder bbd1 = msg1.toBuilder();
					Builder bbd2 = msg2.toBuilder();
					merge(bbd1, bbd2, options);
					alphaBuilder.addRepeatedField(field, bbd1.build());
					betaBuilder.addRepeatedField(field, bbd2.build());
				}
			} else {
				List<Object> intersection = new ArrayList<>(l1);
				intersection.retainAll(l2);
				List<Object> l1WoIntersection = new ArrayList<>(l1);
				List<Object> l2WoIntersection = new ArrayList<>(l2);
				for (Object obj : intersection) {
					if (!l1WoIntersection.contains(obj) || !l2WoIntersection.contains(obj)) {
						continue;
					}
					l1WoIntersection.remove(obj);
					l2WoIntersection.remove(obj);
				}
				l1.addAll(l2WoIntersection);
				l2.addAll(l1WoIntersection);
				alphaBuilder.setField(field, l1);
				betaBuilder.setField(field, l2);
			}
		}
	}

	private static void mergeSingleField(FieldDescriptor field, Builder alphaBuilder, Builder betaBuilder,
			Object alphaValue, Object betaValue, MergeOptions options) {
		Resolver cmp = options.getResolverForField(field.getFullName());
		if (field.getJavaType() == JavaType.MESSAGE) {
			if (alphaValue != null && betaValue != null) {
				Message m1 = (Message) alphaValue;
				Message m2 = (Message) betaValue;
				Builder bb1 = m1.toBuilder();
				Builder bb2 = m2.toBuilder();
				mergeBuilders(bb1, bb2, options);
				alphaBuilder.setField(field, bb1.build());
				betaBuilder.setField(field, bb2.build());
			} else {
				if (cmp != null) {
					if (cmp == Resolver.GREATER || cmp == Resolver.LESSER) {
						throw new UnsupportedOperationException(
								cmp + " resolver is not applicable for a message field. Field: " + field.getFullName());
					} else if (cmp == Resolver.FIRST) {
						if (alphaValue == null) {
							betaBuilder.clearField(field);
						} else {
							betaBuilder.setField(field, alphaValue);
						}
					} else if (cmp == Resolver.SECOND) {
						if (betaValue == null) {
							alphaBuilder.clearField(field);
						} else {
							alphaBuilder.setField(field, betaValue);
						}
					} else {
						alphaBuilder.clearField(field);
						betaBuilder.clearField(field);
					}
				} else {
					if (alphaValue == null) {
						alphaBuilder.setField(field, betaValue);
					} else {
						betaBuilder.setField(field, alphaValue);
					}
				}
			}
		} else {
			if (alphaValue != null && betaValue != null) {
				if (cmp == null) {
					return;
				}
				switch (field.getJavaType()) {
				case DOUBLE:
				case FLOAT:
				case INT:
				case LONG:
				case BOOLEAN:
				case STRING:
				case ENUM:
					if (cmp == Resolver.GREATER || cmp == Resolver.LESSER) {
						int compareRes = 0;
						if (alphaValue instanceof Comparable && betaValue instanceof Comparable) {
							compareRes = ((Comparable) alphaValue).compareTo((Comparable) betaValue);
						} else {
							compareRes = alphaValue.toString().compareTo(betaValue.toString());
						}
						Object smallerObj = alphaValue;
						Object largerObj = betaValue;
						if (compareRes > 0) {
							smallerObj = betaValue;
							largerObj = alphaValue;
						}
						if (cmp == Resolver.GREATER) {
							alphaBuilder.setField(field, largerObj);
							betaBuilder.setField(field, largerObj);
						} else {
							alphaBuilder.setField(field, smallerObj);
							betaBuilder.setField(field, smallerObj);
						}
					} else if (cmp == Resolver.FIRST) {
						betaBuilder.setField(field, alphaValue);
					} else if (cmp == Resolver.SECOND) {
						alphaBuilder.setField(field, betaValue);
					} else {
						alphaBuilder.clearField(field);
						betaBuilder.clearField(field);
					}
					break;
				default:
					return;
				}
			} else {
				if (cmp != null) {
					switch (field.getJavaType()) {
					case DOUBLE:
					case FLOAT:
					case INT:
					case LONG:
					case BOOLEAN:
					case STRING:
					case ENUM:
						if (cmp == Resolver.GREATER || cmp == Resolver.LESSER) {
							throw new UnsupportedOperationException(cmp + " resolver is not applicable for "
									+ field.getFullName() + " when one of the objects is null");
						} else if (cmp == Resolver.FIRST) {
							if (alphaValue == null) {
								betaBuilder.clearField(field);
							} else {
								betaBuilder.setField(field, alphaValue);
							}
						} else if (cmp == Resolver.SECOND) {
							if (betaValue == null) {
								alphaBuilder.clearField(field);
							} else {
								alphaBuilder.setField(field, betaValue);
							}
						} else {
							alphaBuilder.clearField(field);
							betaBuilder.clearField(field);
						}
						break;
					default:
						return;
					}
				} else {
					if (alphaValue == null) {
						alphaBuilder.setField(field, betaValue);
					} else {
						betaBuilder.setField(field, alphaValue);
					}
				}
			}
		}
	}

}
