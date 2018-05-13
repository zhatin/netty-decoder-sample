package com.webyun.samples.tcp;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelRepository {
	private ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<String, Channel>();

	public ChannelRepository put(String key, Channel value) {
		channelCache.put(key, value);
		return this;
	}

	public Channel get(String key) {
		return channelCache.get(key);
	}

	public void remove(String key) {
		this.channelCache.remove(key);
	}

	public int size() {
		return this.channelCache.size();
	}
}
