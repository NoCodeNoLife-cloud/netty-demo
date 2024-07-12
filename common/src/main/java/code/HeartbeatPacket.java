package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString(callSuper = true)
public class HeartbeatPacket extends CustomizedMessageType {
	private String info;

	public HeartbeatPacket() {
		super(MessageType.HeartbeatPacket);
	}

	public HeartbeatPacket(String info) {
		super(MessageType.HeartbeatPacket);
		this.info = info;
	}
}