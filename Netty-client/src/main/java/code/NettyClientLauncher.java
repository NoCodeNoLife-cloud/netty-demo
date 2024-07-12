package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClientLauncher {
	private final String inetHost;
	private final int inetPort;

	public NettyClientLauncher(String inetHost, int inetPort) {
		this.inetHost = inetHost;
		this.inetPort = inetPort;
	}

	/**
	 * Initializes and connects the client to the server.
	 */
	@SneakyThrows
	public void bootStrap() {
		// Create a logging handler for debugging
		LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);

		// Create a NioEventLoopGroup for event executors
		NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

		try {
			// Create a Bootstrap object and configure the parameters
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventExecutors); // Set the thread groups
			bootstrap.channel(NioSocketChannel.class); // Set the channel implementation type of the client

			// Initialize the channel with an anonymous inner class
			bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
				@Override
				protected void initChannel(@NonNull NioSocketChannel nioSocketChannel) {
					ChannelPipeline channelPipeline = nioSocketChannel.pipeline(); // Get channel pipeline

					// Add a logging handler for the client channel
					channelPipeline.addLast(loggingHandler);

					// Add a String decoder and String encoder handler
					channelPipeline.addLast(new StringDecoder());
					channelPipeline.addLast(new StringEncoder());

					// Add an IdleStateHandler for idle time detection
					channelPipeline.addLast(new IdleStateHandler(0, 3, 0));

					// Add a ChannelDuplexHandler for both inbound and outbound messages
					// Heartbeat mechanism
					channelPipeline.addLast(new ChannelDuplexHandler() {
						@Override
						public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
							if (evt instanceof IdleStateEvent idleStateEvent) {
								if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
									log.info("No data is sent for 3 seconds, send a heartbeat packet");
									// TODO: Send heartbeat packet
								}
							}
							super.userEventTriggered(ctx, evt);
						}
					});

					// TODO: Add handle
				}
			});

			// Connect to the server
			ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(inetHost, inetPort));

			// Add a listener for connection status
			// Uses an anonymous inner class, the ChannelFutureListener interface
// Override the operationComplete method
			channelFuture.addListener((ChannelFutureListener) future -> {
				// Check whether the operation is successful
				if (future.isSuccess()) {
					log.info("Successful connection");
				} else {
					log.error("Connection failure");
				}
			});

			// Wait for connection
			channelFuture.sync();

			// Get the connection channel
			Channel channel = channelFuture.channel();

			// Execute business logic
			executeBusiness(channel);

			// Get the close channel future
			ChannelFuture closeFuture = channel.closeFuture();
			closeFuture.addListener((ChannelFutureListener) future -> log.info("shut down"));

			// Listen for channel closure
			closeFuture.sync();
		} finally {
			// Shutdown the event executors gracefully
			eventExecutors.shutdownGracefully();
		}
	}

	/**
	 * Executes the business logic after a connection has been established.
	 *
	 * @param channel the channel representing the connection
	 */
	@SneakyThrows
	private static void executeBusiness(Channel channel) {
		// Write a message to the server
		// while (true) {
		// 	TimeUnit.SECONDS.sleep(1);
		// 	channel.writeAndFlush("Hello, Server!\n");
		// }
		System.out.println("Hello, Server!\n");
	}

	/**
	 * Prints the hexadecimal representation of the given ByteBuf object.
	 *
	 * @param byteBuf the ByteBuf object to print
	 */
	private static void printByteBuf(ByteBuf byteBuf) {
		System.out.println(ByteBufUtil.prettyHexDump(byteBuf));
	}

	/**
	 * Retrieves a list of EventExecutors from the provided EventLoopGroup.
	 *
	 * @param nioEventLoopGroup The EventLoopGroup from which to retrieve EventExecutors.
	 *
	 * @return A list of EventExecutors.
	 */
	private static List<EventExecutor> getEventLoopGroupLists(EventLoopGroup nioEventLoopGroup) {
		// Create a new ArrayList to store the EventExecutors
		List<EventExecutor> nioEventLoopList = new ArrayList<>();

		// Iterate over each EventExecutor in the EventLoopGroup and add it to the ArrayList
		nioEventLoopGroup.forEach(nioEventLoopList::add);

		// Return the ArrayList of EventExecutors
		return nioEventLoopList;
	}
}