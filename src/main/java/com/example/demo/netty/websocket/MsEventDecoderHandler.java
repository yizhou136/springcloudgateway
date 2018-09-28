//package com.example.demo.netty.websocket;
//
//import com.zy.learning.beans.MsEvent;
//import com.zy.learning.beans.NettyCxtMsEvent;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.ByteToMessageDecoder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
///**
// * Created by zhougb on 2016/9/13.
// */
//public class MsEventDecoderHandler extends ByteToMessageDecoder{
//    private Logger logger = LoggerFactory.getLogger(MsEventDecoderHandler.class);
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        logger.info("decode in"+in+" out:"+out);
//        out.add(new NettyCxtMsEvent(ctx, new MsEvent()));
//        logger.info("decode in"+in+" out:"+out);
//    }
//}
