package com.sun.xml.internal.ws.api.pipe;

import javax.xml.ws.Dispatch;
import com.sun.xml.internal.ws.api.message.Message;
import com.sun.xml.internal.ws.api.message.Packet;
import com.oracle.webservices.internal.api.message.BasePropertySet;
import com.oracle.webservices.internal.api.message.PropertySet;

public class ThrowableContainerPropertySet extends BasePropertySet {

    public ThrowableContainerPropertySet(final Throwable throwable) {
        this.throwable = throwable;
    }

    public static final String FIBER_COMPLETION_THROWABLE = "com.sun.xml.internal.ws.api.pipe.fiber-completion-throwable";

    private Throwable throwable;

    @Property(FIBER_COMPLETION_THROWABLE)
    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(final Throwable throwable) {
        this.throwable = throwable;
    }

    public static final String FAULT_MESSAGE = "com.sun.xml.internal.ws.api.pipe.fiber-completion-fault-message";

    private Message faultMessage;

    @Property(FAULT_MESSAGE)
    public Message getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(final Message faultMessage) {
        this.faultMessage = faultMessage;
    }

    public static final String RESPONSE_PACKET = "com.sun.xml.internal.ws.api.pipe.fiber-completion-response-packet";

    private Packet responsePacket;

    @Property(RESPONSE_PACKET)
    public Packet getResponsePacket() {
        return responsePacket;
    }

    public void setResponsePacket(final Packet responsePacket) {
        this.responsePacket = responsePacket;
    }

    public static final String IS_FAULT_CREATED = "com.sun.xml.internal.ws.api.pipe.fiber-completion-is-fault-created";

    private boolean isFaultCreated = false;

    @Property(IS_FAULT_CREATED)
    public boolean isFaultCreated() {
        return isFaultCreated;
    }

    public void setFaultCreated(final boolean isFaultCreated) {
        this.isFaultCreated = isFaultCreated;
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;

    static {
        model = parse(ThrowableContainerPropertySet.class);
    }
}
