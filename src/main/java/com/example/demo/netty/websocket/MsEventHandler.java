//package com.example.demo.netty.websocket;
//
//import io.netty.channel.ChannelDuplexHandler;
//import io.netty.channel.ChannelHandlerContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Created by zhougb on 2016/9/13.
// */
//public class MsEventHandler extends ChannelDuplexHandler{
//    private Logger logger = LoggerFactory.getLogger(MsEventHandler.class);
//
//    private ExecutorService executorService = Executors.newFixedThreadPool(1);
//    private MsEventService msEventService = new MsEventService();
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
//        logger.info("channelRead msg:"+msg);
//        if (msg instanceof NettyCxtMsEvent){NettyCxtMsEvent
//            executorService.execute(new Runnable() {
//                public void run() {
//                    msEventService.doMsEvent((NettyCxtMsEvent) msg);
//                }
//            });
//        }else
//            ctx.fireChannelRead(msg);
//    }
//}
