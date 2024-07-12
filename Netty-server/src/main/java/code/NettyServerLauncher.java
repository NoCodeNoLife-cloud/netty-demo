package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

@Slf4j
public class NettyServerLauncher {
	private final int nioEventLoopGroup;
	private final int inetPort;

	public NettyServerLauncher(int inetPort) {
		this.nioEventLoopGroup = Runtime.getRuntime().availableProcessors();
		this.inetPort = inetPort;
	}

	public NettyServerLauncher(int inetPort, int nioEventLoopGroup) {
		this.nioEventLoopGroup = nioEventLoopGroup;
		this.inetPort = inetPort;
	}

	/**
	 * Initializes the Netty server bootstrap with specified configurations and handlers.
	 */
	@SneakyThrows
	public void bootStrap() {
		LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);

		// Create two thread groups, boosGroup and workerGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(nioEventLoopGroup);
		try {
			// Create a start object for the server and set its parameters
			ServerBootstrap bootstrap = new ServerBootstrap();
			// Set up two thread groups, boosGroup and workerGroup
			bootstrap.group(bossGroup, workerGroup);
			// Set the server-side channel implementation type
			bootstrap.channel(NioServerSocketChannel.class);
			// Set the thread queue to get the number of connections
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			// Set to keep the connection active
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			// Set receive buffer size
			bootstrap.option(ChannelOption.SO_RCVBUF, 10);
			// Netty will allocate for each Channel to store received data when receiving data.
			bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
			// Initialize the channel object as an anonymous inner class
			bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
				@Override
				protected void initChannel(@NonNull NioSocketChannel nioSocketChannel) {
					// Get channel pipeline
					ChannelPipeline channelPipeline = nioSocketChannel.pipeline();

					// Add log handler
					channelPipeline.addLast(loggingHandler);

					// Optional 1: Fix the length to solve the separator problem, and unnecessary messages will be transmitted
					// channelPipeline.addLast(new FixedLengthFrameDecoder(10));

					// Optional 2: Split character based on newline
					// channelPipeline.addLast(new LineBasedFrameDecoder(1024));

					// Optional3: Used to process messages that contain a length field. Its constructor parameters specify how to parse and process the message's length field and other properties of the message.
					// lengthFieldOffset length field offset
					// lengthFieldLength length field length
					// The lengthAdjustment length field is the benchmark, and a few bytes are the content
					// initialBytesToStrip strips a few bytes from the header
					// channelPipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));

					// Add a processor for the String decoder
					// channelPipeline.addLast(new StringDecoder());

					// Add String encoder handler
					// channelPipeline.addLast(new StringEncoder());

					channelPipeline.addLast(new CustomizedLengthFieldBasedFrameDecoder());
					channelPipeline.addLast(new NettyCustomizedMessageToMessageCodec());

					// Idle time detection, 0 means no timeout
					channelPipeline.addLast(new IdleStateHandler(9, 0, 0));

					// ChannelDuplexHandler can handle both inbound and outbound messages
					// Heartbeat mechanism
					channelPipeline.addLast(new ChannelDuplexHandler() {
						@Override
						public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
							if (evt instanceof IdleStateEvent idleStateEvent) {
								if (idleStateEvent.state() == IdleState.READER_IDLE) {
									log.info("read idle timed out");
									ctx.close();
								}
							}
							super.userEventTriggered(ctx, evt);
						}
					});

					channelPipeline.addLast(new SimpleChannelInboundHandler<HeartbeatPacket>() {
						@Override
						protected void channelRead0(ChannelHandlerContext channelHandlerContext, HeartbeatPacket heartbeatPacket) throws Exception {
							log.info("Received heartbeat packet: {}", heartbeatPacket);
						}
					});
				}
			});
			// Bind to the port
			ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(inetPort));

			// Add listener
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

			// Get connection channel
			Channel channel = channelFuture.channel();

			// Get close channel future
			ChannelFuture closeFuture = channel.closeFuture();
			closeFuture.addListener((ChannelFutureListener) future -> log.info("shut down"));

			// Listens for the closed channel
			closeFuture.sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * Prints the hexadecimal representation of the given ByteBuf object.
	 *
	 * @param byteBuf the ByteBuf object to print
	 */
	private static void printByteBuf(ByteBuf byteBuf) {
		// Convert the ByteBuf object to a hexadecimal string representation
		String hexDump = ByteBufUtil.prettyHexDump(byteBuf);

		// Print the hexadecimal string representation
		System.out.println(hexDump);
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

	/**
	 * Entry point of the application.
	 *
	 * @param args The command line arguments.
	 */
	@SneakyThrows
	public static void main(String[] args) {
		NettyServerLauncher nettyServerLauncher = new NettyServerLauncher(8080);
		nettyServerLauncher.bootStrap();
	}
}