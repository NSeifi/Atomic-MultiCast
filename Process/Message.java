package Process;

import java.io.Serializable;

/**
 * @Author: Nasrin Seifi
 * Purpose: this class provides the Process.Message object for the multicast, it handles data
 * printing the message and set the clock and deliverable and etc.
 * msgType=0:first message
 * msgType=1:clock reply
 * These message types are based on Skeen algorithm
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private String messageId;
    private String senderAddress;
    private int senderPort;
    private String recieverAddress;
    private int recieverPort;
    private int msgType;
    private String data;
    private int group;
    private int gcount;
    private int logicalClockValue;
    private boolean deliverable;

    /**
     * Constructor assigns the first values deliverable is always false for the first message.
     * @param msgId
     * @param senderAddress
     * @param senderPort
     * @param recieverAddress
     * @param recvPort
     * @param group
     * @param gcount the server assigns this value based on the count of members in a group
     * @param type
     * @param data
     */
    public Message(String msgId, String senderAddress, int senderPort, String recieverAddress, int recvPort,
                   int group, int gcount, int type, String data) {
        this.messageId = msgId;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.recieverAddress = recieverAddress;
        this.recieverPort = recvPort;
        this.msgType = type;
        this.data = data;
        this.group = group;
        this.gcount = gcount;
        this.deliverable = false;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setRecieverAddress(String recieverAddress) {
        this.recieverAddress = recieverAddress;
    }

    public int getMsgType() {
        return msgType;
    }

    public int getLogicalClockValue() {
        return logicalClockValue;
    }

    public void setLogicalClockValue(int logicalClockValue) {
        this.logicalClockValue = logicalClockValue;
    }

    public int getGroup() {
        return this.group;
    }

    public void setGCount(int gcount) {
        this.gcount = gcount;
    }

    public int getGcount() {
        return this.gcount;
    }

    /**
     * Usage: when a process wants to send a clock reply it change the receiver and sender and set the type=1
     * also set the clock for the message based on the time that it receive
     * @param time
     * @return a new message based on the new time
     */
    public Message getClockReplyMessage(int time) {
        Message msg = new Message(this.messageId, this.recieverAddress, this.recieverPort, this.senderAddress, this.senderPort,
                this.group, this.gcount, 1, this.data);
        msg.setLogicalClockValue(time);
        return msg;
    }

    /**
     * The method is for printing the message data
     * @return a string
     */
    public String printMessage() {
        return "MID-" + this.messageId +
                "\nType-" + this.msgType +
                "\nSender-" + this.senderAddress + ":" + this.senderPort +
                "\nReciever-" + this.recieverAddress + ":" + this.recieverPort +
                "\nGroup- " + this.group +
                "\nNumber of Group- " + this.gcount +
                "\nContent-" + this.data +
                "\nPiggybankClockValue-" + this.logicalClockValue +
                "\nDeliverable- " + this.deliverable;

    }

    public boolean isDeliverable() {
        return deliverable;
    }

    /**
     * Usage: when a message is decided to be delivered set the message as deliverable
     * @param deliverable
     */
    public void setDeliverable(boolean deliverable) {
        this.deliverable = deliverable;
    }

}
