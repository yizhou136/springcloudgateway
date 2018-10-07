package com.example.demo.netty.mysql.handler;

import com.example.demo.netty.mysql.common.MySqlPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public class MySqlPacketDecoder extends LengthFieldBasedFrameDecoder {
    private static final int maxPayloadLength = 16 * 1024 * 1024;
    private static final int maxFrameLength = maxPayloadLength + 1;
    private static final int lengthFieldOffset = 0;
    private static final int lengthFieldLength = 3;
    private static final int lengthAdjustment = 1;
    private static final int initialBytesToStrip = lengthFieldLength;

    //private boolean moreThanMaxFrameLength;
    private ByteBuf pendingFrame;
    private int payloadLength;

    public MySqlPacketDecoder() {
        super(ByteOrder.LITTLE_ENDIAN, maxFrameLength,
                lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip,
                false);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf)super.decode(ctx, in);
        if (this.payloadLength == 0) {
            this.pendingFrame = frame;
            return null;
        }else if (this.pendingFrame != null) {
            frame = Unpooled.wrappedBuffer(this.pendingFrame, frame);
            this.payloadLength += this.maxPayloadLength;
        }
        MySqlPacket mySqlPacket = new MySqlPacket();
        mySqlPacket.setPayloadLength(this.payloadLength);
        mySqlPacket.setSequenceId(frame.readByte());
        mySqlPacket.setPayload(frame.slice());
        return mySqlPacket;
    }

    @Override
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
        long payloadLength = super.getUnadjustedFrameLength(buf, offset, length, order);
        this.payloadLength = (int) payloadLength;
        if (payloadLength == 0) {
            payloadLength = maxPayloadLength;
        }
        return payloadLength;
    }
}
