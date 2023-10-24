package dslab.data;

public class AnonymousPacket implements Packet<AnonymousPacket> {

    public String message;
    public boolean encodeLinebreaks = false;
    public AnonymousPacket parseString(String data)  {
        this.message = encodeLinebreaks ? data.replace("<br>", "\n") : data;
        return this;
    }

    @Override
    public String toPacketString() {
        return encodeLinebreaks ? this.message.replace("\n", "<br>") : message;
    }

}
