package com.example.demo.netty.mysql.client;

import com.example.demo.netty.mysql.common.MySqlPacket;
import com.example.demo.netty.mysql.handler.BackendHandler;
import com.example.demo.netty.mysql.handler.FrontendHandler;
import com.example.demo.netty.mysql.handler.MySqlPacketDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import static com.example.demo.netty.mysql.client.MysqlClient.State.INIT;

/**
 * @author zhouguobao
 * 2018/9/30
 */
public class MysqlClient {
    public enum State {
        INIT,
        CONNECTED,
        AUTHING,
        AUTHED,
        COMMAND
    }
    private static final EventLoopGroup clientEventLoopGroup;
    private static final Bootstrap bootstrap;

    static {
        clientEventLoopGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("BACKEND_GROUP"));
        bootstrap = new Bootstrap();
        bootstrap.group(clientEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    public static void shutdown() {
        clientEventLoopGroup.shutdownGracefully();
    }

    public static final MysqlClient newDefaultInstance(Channel frontChannel) {
        try {
            return new MysqlClient(frontChannel,"127.0.0.1", 3306);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ChannelFuture clientConnectFuture;
    private final Channel frontChannel;
    private Channel backChannel;
    private State state;

    public MysqlClient(Channel frontChannel, String host, int port) throws InterruptedException {
        this.frontChannel = frontChannel;
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new LoggingHandler(LogLevel.INFO));
                p.addLast(new MySqlPacketDecoder());
                p.addLast(new BackendHandler(MysqlClient.this));
            }
        });
        this.clientConnectFuture = bootstrap.connect(host, port).sync();
        this.state = INIT;
    }

    public Channel getFrontChannel() {
        return frontChannel;
    }

    public void setBackChannel(Channel backChannel) {
        this.backChannel = backChannel;
    }

    public Channel getBackChannel() {
        return backChannel;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ChannelFuture writeToBackChannel(MySqlPacket mySqlPacket) {
        Channel backChannel = getBackChannel();
        if (backChannel == null) {
            return null;
        }
        return writeToChannel(backChannel, mySqlPacket);
    }

    public ChannelFuture writeToFrontChannel(MySqlPacket mySqlPacket) {
        Channel frontChannel = getFrontChannel();
        if (frontChannel == null) {
            return null;
        }
        return writeToChannel(frontChannel, mySqlPacket);
    }

    private ChannelFuture writeToChannel(Channel channel, MySqlPacket mySqlPacket) {
        ByteBuf byteBuf = channel.alloc().ioBuffer(mySqlPacket.getPayloadLength()+4);
        byteBuf.writeMediumLE(mySqlPacket.getPayloadLength());
        byteBuf.writeByte(mySqlPacket.getSequenceId());
        return channel.writeAndFlush(Unpooled.wrappedBuffer(byteBuf,mySqlPacket.getPayload()));
    }

    public static void main(String argv[]) {
        MysqlClient mysqlClient = MysqlClient.newDefaultInstance(null);

    }
}
