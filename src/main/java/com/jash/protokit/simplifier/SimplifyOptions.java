package com.jash.protokit.simplifier;

import java.util.ArrayList;
import java.util.List;

import com.jash.protokit.simplifier.Internal.UtilAdapter;

/**
 * A class to hold options for simplifying a message.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 * @see Builder
 */
public class SimplifyOptions {

	protected static final SimplifyOptions DUMMY_OPTIONS = new SimplifyOptions(Builder.newBuilder());

	private List<String> dropFields;

	private SimplifyOptions(Builder builder) {
		dropFields = builder.dropFields;
	}

	protected boolean canDropField(String fieldFullName) {
		return dropFields.contains(UtilAdapter.getFieldName(fieldFullName));
	}

	@Override
	public String toString() {
		return "SimplifyOptions [dropFields=" + dropFields + "]";
	}

	/**
	 * Builder for {@link SimplifyOptions}.
	 */
	public static class Builder {

		private List<String> dropFields;

		private Builder() {
			dropFields = new ArrayList<>();
		}

		public static Builder newBuilder() {
			return new Builder();
		}

		/**
		 * Add a field to drop from the message.
		 * 
		 * @param field - The field to be dropped. Field format should be like
		 *              "EncasingMessage.fieldName". E.g.: "SampleMessage.field"
		 * @return The current instance of {@link Builder}.
		 */
		public Builder addFieldToDrop(String field) {
			dropFields.add(field);
			return this;
		}

		/**
		 * Get the list of fields to drop.
		 */
		public List<String> getDropFields() {
			return dropFields;
		}

		/**
		 * Build the {@link SimplifyOptions} instance.
		 * 
		 * @return The {@link SimplifyOptions} instance.
		 */
		public SimplifyOptions build() {
			return new SimplifyOptions(this);
		}

	}

}
