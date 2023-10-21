package dslab.data;

public interface PacketFactory<TPacket extends Packet> {
    TPacket create(String data);
}
