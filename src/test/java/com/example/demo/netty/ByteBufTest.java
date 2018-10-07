package com.example.demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBufTest {
    private static final Logger logger = LoggerFactory.getLogger(ByteBufTest.class);

    @Test
    public void testReadInt() {
        ByteBuf b = (ByteBuf) t();
        byte[] bytes = new byte[]{0x58, 0x00, 0x00, 0x01};
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        logger.info("byteBuf i:{} b:{}",
                byteBuf.readUnsignedMediumLE(), b);
    }

    private Object t(){
        return null;
    }
}
