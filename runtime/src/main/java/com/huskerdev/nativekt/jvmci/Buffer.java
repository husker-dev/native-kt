package com.huskerdev.nativekt.jvmci;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Buffer {

    private ByteBuffer data = ByteBuffer.allocate(32).order(ByteOrder.nativeOrder());

    private void ensureSize(int length) {
        if (length >= data.limit()) {
            byte[] newBuf = Arrays.copyOf(data.array(), length * 4);
            ByteBuffer newData = ByteBuffer.wrap(newBuf);
            newData.order(data.order());
            newData.position(data.position());
            data = newData;
        }
    }

    public void emitByte(int b) {
        ensureSize(data.position() + 1);
        data.put((byte) (b & 0xFF));
    }

    public void emitInt(int b) {
        ensureSize(data.position() + 4);
        data.putInt(b);
    }

    public void emitLong(long b) {
        ensureSize(data.position() + 8);
        data.putLong(b);
    }

    public byte[] finish() {
        return Arrays.copyOf(data.array(), data.position());
    }
}