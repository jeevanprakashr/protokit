package com.jash.protokit.merger;

import com.google.protobuf.Message;

/**
 * A class to hold the merged messages after a merge operation.
 * 
 * @author Jeevan Prakash (jeevanprakash1998@gmail.com)
 */
public class Result<T extends Message> {

	private T alpha;
	private T beta;

	protected Result(T alpha, T beta) {
		this.alpha = alpha;
		this.beta = beta;
	}

	/**
	 * Get the first message.
	 * 
	 * @return The first message.
	 */
	public T getFirst() {
		return alpha;
	}

	/**
	 * Get the second message.
	 * 
	 * @return The second message.
	 */
	public T getSecond() {
		return beta;
	}

	@Override
	public String toString() {
		return "Result [alpha=" + alpha + ", beta=" + beta + "]";
	}

}
