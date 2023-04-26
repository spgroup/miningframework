package cpw.mods.fml.common.network;

import java.util.Arrays;
import java.util.logging.Level;
import net.minecraft.src.NetHandler;
import net.minecraft.src.INetworkManager;
import com.google.common.base.Throwables;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.common.FMLLog;

public abstract class FMLPacket {

    enum Type {

        MOD_LIST_REQUEST(ModListRequestPacket.class),
        MOD_LIST_RESPONSE(ModListResponsePacket.class),
        MOD_IDENTIFIERS(ModIdentifiersPacket.class),
        MOD_MISSING(ModMissingPacket.class),
        GUIOPEN(OpenGuiPacket.class),
        ENTITYSPAWN(EntitySpawnPacket.class),
        ENTITYSPAWNADJUSTMENT(EntitySpawnAdjustmentPacket.class);

        private Class<? extends FMLPacket> packetType;

        private Type(Class<? extends FMLPacket> clazz) {
            this.packetType = clazz;
        }

        FMLPacket make() {
            try {
                return this.packetType.newInstance();
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                FMLLog.log(Level.SEVERE, e, "A bizarre critical error occured during packet encoding");
                throw new FMLNetworkException(e);
            }
        }
    }

    private Type type;

    public static byte[] makePacket(Type type, Object... data) {
        byte[] packetData = type.make().generatePacket(data);
        return Bytes.concat(new byte[] { UnsignedBytes.checkedCast(type.ordinal()) }, packetData);
    }

    public static FMLPacket readPacket(byte[] payload) {
        int type = UnsignedBytes.toInt(payload[0]);
        return Type.values()[type].make().consumePacket(Arrays.copyOfRange(payload, 1, payload.length));
    }

    public FMLPacket(Type type) {
        this.type = type;
    }

    public abstract byte[] generatePacket(Object... data);

    public abstract FMLPacket consumePacket(byte[] data);

    public abstract void execute(INetworkManager network, FMLNetworkHandler handler, NetHandler netHandler, String userName);

    {
    }
}
