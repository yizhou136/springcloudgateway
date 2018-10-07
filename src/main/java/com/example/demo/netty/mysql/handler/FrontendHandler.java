package com.example.demo.netty.mysql.handler;

import com.example.demo.netty.mysql.client.MysqlClient;
import com.example.demo.netty.mysql.common.MySqlPacket;
import com.example.demo.netty.mysql.common.Session;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrontendHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(FrontendHandler.class);

    private static final Map<ChannelId, Session> sessionMap = new ConcurrentHashMap();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelId frontChannelId = ctx.channel().id();
        sessionMap.compute(frontChannelId, (key, oldValue) -> {
            Session session = oldValue;
            if (oldValue == null) {
                MysqlClient backendClient = MysqlClient.newDefaultInstance(ctx.channel());
                session = new Session(ctx.channel(), backendClient);
            }
            return session;
        });
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MySqlPacket) {
            MySqlPacket mySqlPacket = (MySqlPacket)msg;
            MysqlClient mysqlClient = receiveBackendClient(ctx);
            switch (mysqlClient.getState()) {
                case CONNECTED:
                    break;
                case AUTHING:
                    sendAuth(mysqlClient, mySqlPacket);
                    break;
                case AUTHED:
                    sendQuery(mysqlClient, mySqlPacket);
                    break;
                case COMMAND:
                    mysqlClient.writeToBackChannel(mySqlPacket);
                    break;
                default:
                    throw new InvalidStateException("the mysqlClient:"+mysqlClient+" has invalid state.");
            }


        }
        super.channelRead(ctx, msg);
    }

    private void sendAuth(MysqlClient mysqlClient, MySqlPacket mySqlPacket) {
        ChannelFuture channelFuture = mysqlClient.writeToBackChannel(mySqlPacket);
    }

    private void sendQuery(MysqlClient mysqlClient, MySqlPacket mySqlPacket) {
        ChannelFuture channelFuture = mysqlClient.writeToBackChannel(mySqlPacket);
        mysqlClient.setState(MysqlClient.State.COMMAND);
    }

    private MysqlClient receiveBackendClient(ChannelHandlerContext ctx) {
        return sessionMap.get(ctx.channel().id()).getMysqlClient();
    }
}
