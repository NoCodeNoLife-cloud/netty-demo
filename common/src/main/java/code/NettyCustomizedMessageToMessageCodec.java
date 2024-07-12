package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.HashMap;
import java.util.List;

/**
 * The type Message codec.
 * In order to solve the sticky package problem, it is necessary to add: LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0)
 */
@ChannelHandler.Sharable
public class NettyCustomizedMessageToMessageCodec extends MessageToMessageCodec<ByteBuf, CustomizedMessageType> {
	/**
	 * type field mapping type class
	 * key is MessageType ordinate, value is type
	 */
	private static final HashMap<Integer, Class<?>> TYPE_MAPPING = new HashMap<>();
	/**
	 * Message type mapping serialization method
	 * key is MessageType ordinate, value is CustomizedSerializerAlgorithm ordinate
	 */
	private static final HashMap<Integer, Integer> SERIALIZATION_METHOD = new HashMap<>();

	static {
		TYPE_MAPPING.put(MessageType.HeartbeatPacket.ordinal(), HeartbeatPacket.class);
		SERIALIZATION_METHOD.put(MessageType.HeartbeatPacket.ordinal(), CustomizedSerializerAlgorithm.Java.ordinal());
		// TODO: Initialize SERIALIZATION_METHOD and TYPE_MAPPING
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CustomizedMessageType msg, List<Object> out) {
		// ByteBuf byteBuf = ctx.alloc().buffer();
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
		// 4-byte magic number
		byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
		// 1 -byte version
		byteBuf.writeByte(1);
		// 1-byte serialization method
		// Here it is assumed that 0 represents the jdk serialization method, and 1 represents the json serialization method
		byteBuf.writeByte(SERIALIZATION_METHOD.get(msg.getMessageType()));
		// 1-byte instruction type
		byteBuf.writeByte(msg.getMessageType());
		// 4-byte request sequence number
		byteBuf.writeInt(msg.getSequenceId());
		byteBuf.writeByte(0xff);
		// text length
		// Get the byte array of the content
		byte[] bytes = null;
		// TODO: Serialize the message according to the serialization method
		if (SERIALIZATION_METHOD.get(msg.getMessageType()) == CustomizedSerializerAlgorithm.Java.ordinal()) {
			bytes = CustomizedSerializerAlgorithm.Java.serialize(msg);
		}
		// set length
		byteBuf.writeInt(bytes.length);
		// write content
		byteBuf.writeBytes(bytes);
		// add to out list
		out.add(byteBuf);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		Object message = null;
		// read magic number
		int magicNum = msg.readInt();
		// read version number
		byte version = msg.readByte();
		// read serialization type
		byte serializerAlgorithm = msg.readByte();
		// read message type
		byte messageType = msg.readByte();
		// read sequence number
		int sequenceId = msg.readInt();
		// read 0xff
		msg.readByte();
		// read length
		int length = msg.readInt();
		// create a byte array for storing data
		byte[] bytes = new byte[length];
		// read data into byte array
		msg.readBytes(bytes, 0, length);
		// serialization type
		// get message class
		Class<?> messageClass = TYPE_MAPPING.get((int) messageType);
		if (serializerAlgorithm == CustomizedSerializerAlgorithm.Java.ordinal()) {
			System.out.println("JAVA");
			// deserialize message object
			message = CustomizedSerializerAlgorithm.Java.deserialize(messageClass, bytes);
		}
		out.add(message);
	}
}