package com.example.demo.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface ChannelInitializerRegister {

    boolean isSupport(ByteBuf byteBuf);

    void  registerInitializer(ChannelHandlerContext ctx);
}
