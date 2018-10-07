package com.example.demo.netty.mysql.common;

import io.netty.buffer.ByteBuf;

public class MySqlPacket {
    private int payloadLength;
    private byte sequenceId;
    private ByteBuf payload;

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public byte getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(byte sequenceId) {
        this.sequenceId = sequenceId;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{payloadLength:").append(getPayloadLength())
                .append(" sequenceId:").append(getSequenceId())
                .append(" payload:").append(getPayload());
        return stringBuilder.toString();
    }
}
