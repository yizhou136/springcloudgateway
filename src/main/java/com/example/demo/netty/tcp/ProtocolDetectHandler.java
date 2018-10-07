package com.example.demo.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProtocolDetectHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectHandler.class);

    private List<ChannelInitializerRegister> initializerRegisterList;
    public ProtocolDetectHandler(List<ChannelInitializerRegister> list){
        initializerRegisterList = list;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            for (ChannelInitializerRegister register : initializerRegisterList) {
                if (register.isSupport((ByteBuf)msg))
                    register.registerInitializer(ctx);
            }
        }
        ctx.fireChannelRead(msg);
    }
}
