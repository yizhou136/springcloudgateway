package com.example.demo.netty.mysql.handler;

import com.example.demo.netty.mysql.client.MysqlClient;
import com.example.demo.netty.mysql.common.MySqlPacket;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin.dom.exception.InvalidStateException;

public class BackendHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(BackendHandler.class);

    private final MysqlClient mysqlClient;

    public BackendHandler(MysqlClient mysqlClient) {
        this.mysqlClient = mysqlClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        mysqlClient.setState(MysqlClient.State.CONNECTED);
        mysqlClient.setBackChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MySqlPacket) {
            MySqlPacket mySqlPacket = (MySqlPacket)msg;
            logger.info("mySqlPacket:{} MysqlClient:{}", mySqlPacket, this.mysqlClient);
            switch (mysqlClient.getState()) {
                case CONNECTED:
                    ChannelFuture channelFuture = this.mysqlClient.writeToFrontChannel(mySqlPacket);
                    this.mysqlClient.setState(MysqlClient.State.AUTHING);
                    break;
                case AUTHING:
                    channelFuture = this.mysqlClient.writeToFrontChannel(mySqlPacket);
                    this.mysqlClient.setState(MysqlClient.State.AUTHED);
                    break;
                case COMMAND:
                    channelFuture = this.mysqlClient.writeToFrontChannel(mySqlPacket);
                    break;
                default:
                    throw new InvalidStateException("the mysqlClient:"+this.mysqlClient+" has invalid state.");
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //ctx.read
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ConnectTimeoutException) {
            logger.warn("connect is timeout");
        }
        //super.exceptionCaught(ctx, cause);
        logger.error("", cause);
    }

    private void connected(MySqlPacket mySqlPacket) {

    }

    private void auth() {

    }

    private void command() {

    }
}
