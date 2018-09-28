package com.example.demo.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhougb on 2016/9/13.
 */
public class ProtocolDetecterHandler extends ChannelInboundHandlerAdapter{
    private Logger logger = LoggerFactory.getLogger(ProtocolDetecterHandler.class);
    private static final String WEBSOCKET_PATH = "/websocket";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("ProtocolDetecterHandler start");
        if (msg instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf)msg;
            byte[] tmpdata = new byte[1024];
            byteBuf.getBytes(0, tmpdata);
            logger.info("ProtocolDetecterHandler tmpdata:"+new String(tmpdata));
            ChannelPipeline pipeline = ctx.pipeline();
            if (byteBuf.getByte(0) == 'G' || byteBuf.getByte(0) == 'P'){
                logger.info("ProtocolDetecterHandler goto Http");
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new WebSocketServerCompressionHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
                pipeline.addLast(new WebSocketFrameHandler());
            }else {
                logger.info("ProtocolDetecterHandler goto msevent");
                //pipeline.addLast(new MsEventDecoderHandler());
                //pipeline.addLast(new MsEventHandler());
            }
        }

        ctx.pipeline().remove(this);
        ctx.fireChannelRead(msg);
    }
}