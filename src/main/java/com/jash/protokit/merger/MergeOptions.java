package com.jash.protokit.merger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.jash.protokit.merger.Internal.UtilAdapter;

/**
 * A class to hold options for merging two messages.
 * 
 * @see Builder
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 */
public class MergeOptions {

	protected static final MergeOptions DUMMY_OPTIONS = new MergeOptions(Builder.newBuilder());

	private Map<String, Resolver> conflictResolver;
	private Map<String, String> mergeRepeatedByField;
	private List<String> excludeFields;

	private MergeOptions(Builder builder) {
		conflictResolver = builder.conflictResolver;
		mergeRepeatedByField = builder.mergeRepeatedByField;
		excludeFields = builder.excludeFields;
	}

	protected Resolver getResolverForField(String fieldFullName) {
		return conflictResolver.get(UtilAdapter.getFieldName(fieldFullName));
	}

	protected boolean shouldMergeRepeatedField(String fieldFullName) {
		return mergeRepeatedByField.containsKey(UtilAdapter.getFieldName(fieldFullName));
	}

	protected String getMergeByFieldForRepeatedField(String fieldFullName) {
		return mergeRepeatedByField.get(UtilAdapter.getFieldName(fieldFullName));
	}

	protected boolean isFieldExcluded(String fieldFullName) {
		return excludeFields.contains(UtilAdapter.getFieldName(fieldFullName));
	}

	@Override
	public String toString() {
		return "MergeOptions [conflictResolver=" + conflictResolver + ", mergeRepeatedByField="
				+ mergeRepeatedByField + ", excludeFields=" + excludeFields + "]";
	}

	/**
	 * Builder for {@link MergeOptions}.
	 */
	public static class Builder {

		private Map<String, Resolver> conflictResolver;
		private Map<String, String> mergeRepeatedByField;
		private List<String> excludeFields;

		private Builder() {
			conflictResolver = new HashMap<>();
			mergeRepeatedByField = new HashMap<>();
			excludeFields = new ArrayList<>();
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
		 * Add a resolver for a field in case of conflict. Resolver will be used in
		 * cases of (objA, null), (null, objB) and (objA, objB) where objA != objB.
		 * Certain resolvers may not be applicable for some situations even if they are
		 * among these three cases like comparator type resolver when one of the objects
		 * is null.
		 * 
		 * @param field    - Conflicting field. Field format should be like
		 *                 "EncasingMessage.fieldName". E.g.: "SampleMessage.primField"
		 * @param resolver - Resolver type to use in case of conflict.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setConflictResolver(String field, Resolver resolver) {
			conflictResolver.put(field, resolver);
			return this;
		}

		/**
		 * Add resolvers for multiple fields in case of conflict. Resolver will be used
		 * in cases of (objA, null), (null, objB) and (objA, objB) where objA != objB.
		 * Certain resolvers may not be applicable for some situations even if they are
		 * among these three cases like comparator type resolver when one of the objects
		 * is null or of type {@link Message}.
		 * 
		 * @param fieldVsResolver - Map of field vs resolver. Field format should be
		 *                        like "EncasingMessage.fieldName". E.g.:
		 *                        "SampleMessage.primField"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setAllConflictResolvers(Map<String, Resolver> fieldVsResolver) {
			conflictResolver.putAll(fieldVsResolver);
			return this;
		}

		/**
		 * Can be used if the order of elements in a repeated field doesn't matter and
		 * needs to be merged. If a repeated field is not given here, it will not be
		 * merged. Field format should be like "EncasingMessage.fieldName". E.g.:
		 * "SampleMessage.repeatedField"
		 * 
		 * @param field   - Repeated field to be merged.
		 * @param byField - Field to be used for merging if the repeated field is of
		 *                type {@link Message}. If it is not, then can be null.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setMergeRepeatedByField(String field, String byField) {
			mergeRepeatedByField.put(field, byField);
			return this;
		}

		/**
		 * Can be used if the order of elements in a repeated field doesn't matter and
		 * needs to be merged. If a repeated field is not given here, it will not be
		 * merged. Field format should be like "EncasingMessage.fieldName". E.g.:
		 * "SampleMessage.repeatedField"
		 * 
		 * @param fieldVsByField - Map of repeated field vs field to be used for merging
		 *                       if the repeated field is of type {@link Message} else
		 *                       can be null.
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setAllMergeRepeatedByFields(Map<String, String> fieldVsByField) {
			mergeRepeatedByField.putAll(fieldVsByField);
			return this;
		}

		/**
		 * Add a field to be excluded from merging.
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
		 * Add multiple fields to be excluded from merging.
		 * 
		 * @param fields - Fields to be excluded. Field format should be like
		 *               "EncasingMessage.fieldName". E.g.: "SampleMessage.field"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder addAllExcludeFields(Collection<String> fields) {
			excludeFields.addAll(fields);
			return this;
		}

		/**
		 * Get field vs resolvers map for merge.
		 */
		public Map<String, Resolver> getConflictResolver() {
			return conflictResolver;
		}

		/**
		 * Get field vs merge by field map for merge.
		 */
		public Map<String, String> getMergeRepeatedByField() {
			return mergeRepeatedByField;
		}

		/**
		 * Get list of excluded fields for merge.
		 */
		public List<String> getExcludeFields() {
			return excludeFields;
		}

		/**
		 * Build the {@link MergeOptions} instance.
		 * 
		 * @return The {@link MergeOptions} instance.
		 */
		public MergeOptions build() {
			return new MergeOptions(this);
		}

	}

	public enum Resolver {

		GREATER, LESSER, FIRST, SECOND, DEFAULT;

	}

}
