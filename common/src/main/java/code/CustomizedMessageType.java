package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * The interface Customize message type interface.
 */
@Slf4j
@Getter
@Setter
@ToString(callSuper = true)
public abstract class CustomizedMessageType implements Serializable {// TODO: Define message types that inherit from CustomizedMessageType
	/**
	 * count
	 */
	private static int count = 0;
	/**
	 * sequenceId
	 */
	private int sequenceId;
	/**
	 * messageType
	 */
	private int messageType;

	/**
	 * CustomizedMessageType
	 *
	 * @param messageType messageType
	 */
	public CustomizedMessageType(MessageType messageType) {
		count++;
		this.sequenceId = count;
		this.messageType = messageType.ordinal();
	}
}