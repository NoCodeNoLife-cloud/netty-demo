package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Custom length field based frame decoder.
 * Inherited from the LengthFieldBasedFrameDecoder class.
 */
public class CustomizedLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {
	/**
	 * Default constructor.
	 * Call the parameterized constructor with default parameters.
	 */
	public CustomizedLengthFieldBasedFrameDecoder() {
		this(1024, 12, 4, 0, 0);
	}

	/**
	 * Constructor with parameters.
	 *
	 * @param maxFrameLength      The maximum frame length received.
	 * @param lengthFieldOffset   Length field offset.
	 * @param lengthFieldLength   The length of the length field.
	 * @param lengthAdjustment    length adjustment value.
	 * @param initialBytesToStrip The number of bytes to skip.                            Call the parametric constructor of the parent class with the given parameters.
	 */
	public CustomizedLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}
}