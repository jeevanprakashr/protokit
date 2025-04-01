package com.jash.protokit.comparer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jash.protokit.comparer.Internal.UtilAdapter;

/**
 * Use if a field value needs to be mapped to another value for comparison only
 * if a diff is found.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 * @see Builder
 */
public class ProtoMapper {

	private Map<String, MapperFunction> fieldVsMapper = new LinkedHashMap<>();
	private boolean useMappedValue;

	private ProtoMapper(Builder builder) {
		this.fieldVsMapper = builder.fieldVsMapper;
		this.useMappedValue = builder.useMappedValue;
	}

	protected Object map(String field, Object value, boolean firstObj) {
		MapperFunction mapper = fieldVsMapper.get(UtilAdapter.getFieldName(field));
		if (mapper == null) {
			return value;
		}
		return mapper.map(value, firstObj);
	}

	protected boolean shouldUseMappedValue() {
		return useMappedValue;
	}

	@Override
	public String toString() {
		return "ProtoMapper [fieldVsMapper fields=" + fieldVsMapper.keySet() + ", useMappedValue=" + useMappedValue
				+ "]";
	}

	/**
	 * Callback to map a value to another value.
	 */
	@FunctionalInterface
	public static interface MapperFunction {

		/**
		 * Map the value to another value.
		 * 
		 * @param value    - The value to map.
		 * @param firstObj - If the value is from the first object while comparing.
		 * @return The mapped value.
		 */
		Object map(Object value, boolean firstObj);

	}

	/**
	 * Builder for {@link ProtoMapper}.
	 */
	public static class Builder {

		private Map<String, MapperFunction> fieldVsMapper = new LinkedHashMap<>();
		private boolean useMappedValue;

		private Builder() {
			fieldVsMapper = new HashMap<>();
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
		 * If set, the mapped value will be used for comparison else the mapped value
		 * will be printed along side the actual value in the comparison result
		 * 
		 * @param useMappedValue
		 * @return The current instance of {@link Builder}.
		 */
		public Builder setUseMappedValue(boolean useMappedValue) {
			this.useMappedValue = useMappedValue;
			return this;
		}

		/**
		 * Set a mapper for a field.
		 * 
		 * @param field  - The field for which the mapper is added. Field format should
		 *               be like "EncasingMessage.fieldName". E.g.:
		 *               "SampleMessage.primField"
		 * @param mapper - The mapper function to map the value.
		 * @return The current instance of {@link Builder}.
		 * @see MapperFunction
		 */
		public Builder setMapper(String field, MapperFunction mapper) {
			fieldVsMapper.put(field, mapper);
			return this;
		}

		/**
		 * Set all mappers at once.
		 * 
		 * @param fieldVsMapper - The map of field vs mapper. Field format should be
		 *                      like "EncasingMessage.fieldName". E.g.:
		 *                      "SampleMessage.primField"
		 * @return The current instance of {@link Builder}.
		 * @see MapperFunction
		 */
		public Builder setAllMappers(Map<String, MapperFunction> fieldVsMapper) {
			this.fieldVsMapper.putAll(fieldVsMapper);
			return this;
		}

		/**
		 * Should use the mapped value for comparison or just print it along the side.
		 * 
		 */
		public boolean shouldUseMappedValue() {
			return useMappedValue;
		}

		/**
		 * Get the map of field vs mapper.
		 */
		public Map<String, MapperFunction> getFieldVsMapper() {
			return fieldVsMapper;
		}

		/**
		 * Build the {@link ProtoMapper} instance.
		 * 
		 * @return The {@link ProtoMapper} instance.
		 */
		public ProtoMapper build() {
			return new ProtoMapper(this);
		}

	}


}
