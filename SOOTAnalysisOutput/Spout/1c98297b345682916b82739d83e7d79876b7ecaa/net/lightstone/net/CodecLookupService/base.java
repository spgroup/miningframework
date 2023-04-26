package net.lightstone.net;

import java.util.HashMap;
import java.util.Map;
import net.lightstone.msg.Message;
import net.lightstone.net.codec.ChatMessageCodec;
import net.lightstone.net.codec.CompressedChunkMessageCodec;
import net.lightstone.net.codec.FlyingMessageCodec;
import net.lightstone.net.codec.HandshakeMessageCodec;
import net.lightstone.net.codec.IdentificationMessageCodec;
import net.lightstone.net.codec.KickMessageCodec;
import net.lightstone.net.codec.LoadChunkMessageCodec;
import net.lightstone.net.codec.MessageCodec;
import net.lightstone.net.codec.PingMessageCodec;
import net.lightstone.net.codec.PositionMessageCodec;
import net.lightstone.net.codec.PositionRotationMessageCodec;
import net.lightstone.net.codec.RotationMessageCodec;
import net.lightstone.net.codec.SyncInventoryCodec;
import net.lightstone.net.codec.TimeMessageCodec;

public final class CodecLookupService {

    private static MessageCodec<?>[] opcodeTable = new MessageCodec<?>[256];

    private static Map<Class<? extends Message>, MessageCodec<?>> classTable = new HashMap<Class<? extends Message>, MessageCodec<?>>();

    static {
        try {
            bind(PingMessageCodec.class);
            bind(IdentificationMessageCodec.class);
            bind(HandshakeMessageCodec.class);
            bind(ChatMessageCodec.class);
            bind(TimeMessageCodec.class);
            bind(SyncInventoryCodec.class);
            bind(FlyingMessageCodec.class);
            bind(PositionMessageCodec.class);
            bind(RotationMessageCodec.class);
            bind(PositionRotationMessageCodec.class);
            bind(LoadChunkMessageCodec.class);
            bind(CompressedChunkMessageCodec.class);
            bind(KickMessageCodec.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static <T extends Message, C extends MessageCodec<T>> void bind(Class<C> clazz) throws InstantiationException, IllegalAccessException {
        MessageCodec<T> codec = clazz.newInstance();
        opcodeTable[codec.getOpcode()] = codec;
        classTable.put(codec.getType(), codec);
    }

    public static MessageCodec<?> find(int opcode) {
        return opcodeTable[opcode];
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> MessageCodec<T> find(Class<T> clazz) {
        return (MessageCodec<T>) classTable.get(clazz);
    }

    private CodecLookupService() {
    }
}
