package com.example.demo.netty.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.stereotype.Component;
import reactor.ipc.netty.http.server.HttpServerOptions;

@Component
public class ProtocolDetectNettyCustomizer implements NettyServerCustomizer {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectNettyCustomizer.class);

    @Override
    public void customize(HttpServerOptions.Builder builder) {
        logger.info("builder:{}", builder);
        builder.afterChannelInit()
    }
}
