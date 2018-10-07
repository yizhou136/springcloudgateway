package com.example.demo.netty.mysql;

import com.example.demo.netty.mysql.handler.FrontendHandler;
import com.example.demo.netty.mysql.handler.MySqlPacketDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private int port = 3307;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ChannelFuture channelFuture;

    public void startServer() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("PROXYSERVER_BOSSGROUP"));
        this.workGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("PROXYSERVER_WORKGROUP"));
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new MySqlPacketDecoder());
                        p.addLast(new FrontendHandler());
                    }
                });

        this.channelFuture = serverBootstrap.bind(port).sync();
    }

    public void closeServer() {
        this.bossGroup.shutdownGracefully();
        this.workGroup.shutdownGracefully();
    }

    public static void main(String argv[]) {
        ProxyServer proxyServer = new ProxyServer();
        try {
            proxyServer.startServer();
            proxyServer.channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            proxyServer.closeServer();
        }
    }
}
