package Process;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

/**
 * @Author: Nasrin Seifi
 * Purpose: The class provides the GUI for running the AtomicMulticast
 * Also it has another class for that makes threads for each process
 */
public class ProcessGUI extends javax.swing.JFrame {

    // notification
    private String notif = " *** ";
    // for I/O
    private ObjectInputStream sInput;         // to read from the socket
    private ObjectOutputStream sOutput;       // to write on the socket
    private Socket socket;                   // socket object
    private LogicalClock lg;
    private String server, username;        // server and username
    private int port;                       // port
    private int group;                      // process's group
    private HashMap<String, Integer> memeberCount; // for checking the member's number when message sent
    private ConcurrentHashMap<String, List<Integer>> localTime; // for saving the local time of processes for each message
    private ArrayBlockingQueue<Message> globalTime; // for saving the global time of messages
    private int i = 0;                      // number of messages
    // Variables declaration GUI
    private JTextArea chatArea;
    private JButton jButton1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTextField jTextField1;
    private JLabel status;

    /**
     * Constructor: to set below things and the others such as logical clock, maps and etc.
     * @param server
     * @param port
     * @param username
     * @param group
     * First, it calls the initComponents method to run the GUI for the process
     * then assign the values or memory to maps
     */
    ProcessGUI(String server, int port, String username, int group) {
        initComponents();
        this.server = server;
        this.port = port;
        this.username = username;
        this.lg = new LogicalClock();
        this.group = group;
        this.memeberCount = new HashMap<>();
        this.localTime = new ConcurrentHashMap<>();
        this.globalTime = new ArrayBlockingQueue<>(100, true);
        this.setTitle("Process: " + username);
        this.setVisible(true);
        status.setVisible(true);
    }

    /**
     * Usage: The method provides the GUI for the processes
     */
    private void initComponents() {

        jPanel1 = new JPanel();
        jTextField1 = new JTextField();
        jButton1 = new JButton();
        jScrollPane1 = new JScrollPane();
        chatArea = new JTextArea();
        jLabel2 = new JLabel();
        status = new JLabel();
        jLabel1 = new JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(null);

        jPanel1.add(jTextField1);
        jTextField1.setBounds(30, 50, 270, 30);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(310, 50, 80, 30);

        chatArea.setColumns(20);
        chatArea.setRows(5);
        jScrollPane1.setViewportView(chatArea);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(30, 110, 360, 270);

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Write your text here");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(30, 30, 150, 20);

        status.setText("...");
        jPanel1.add(status);
        status.setBounds(30, 80, 300, 40);

        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 420, 410);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(414, 428));
        setLocationRelativeTo(null);
    }

    /**
     * Usage: when "send" button is pressed, the action behind the button calls this method to send the message
     * after sending each message the field for writing message becomes empty
     * @param evt
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String msg = jTextField1.getText();
        // logout if message is LOGOUT
        if (msg.equalsIgnoreCase("LOGOUT")) {
            sendMessage(new Message(i + " " + username, username, port, server,
                    port, -1, 0, -1, msg));
        }
        // regular text message
        else {
            i++;
            sendMessage(new Message(i + " " + username, username, port, server,
                    port, group, 0, 0, msg));
        }
        jTextField1.setText("");
    }

    /**
     * It is the first method we call to make connection to the server,
     * Also, in the method, "ListenFromServer" class is called to create a Thread for listening from the server
     * @return false in case it couldn't connect to the server, otherwise, return true.
     */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        // exception handler if it failed
        catch (Exception ec) {
            status.setText("Error connectiong to server:" + ec);
            return false;
        }
        status.setText("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        // Creating both Data Stream
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            status.setText("Exception creating new Input/output Streams: " + eIO);
            return false;
        }
        // creates the Thread to listen from the server
        new ProcessGUI.ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be Process.Message objects
        try {
            sOutput.writeObject(username + "-" + group);
        } catch (IOException eIO) {
            status.setText("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        chatArea.append("\nHello.! Welcome to the Multicast. \nInstructions: " +
                "\n1. Simply type the message to send multicast to all active clients in the group " +
                "\n2. Type 'LOGOUT' without quotes to logoff from server");

        chatArea.append("\n>");
        // success we inform the caller that it worked
        return true;
    }

    /**
     * When something goes wrong
     * Close the Input/Output streams and disconnect
     */
    private void disconnect() {
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }

    /**
     * Usage: for sending message to the server,
     * it sends the Process.Message object
     * @param msg
     */
    void sendMessage(Message msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            status.setText("Exception writing to server: " + e);
        }
    }

    /**
     * The class creates a Thread for listening from server,
     * The Algorithm for AtomicMulticast is implemented in this class,
     * The main role of the class is receiving the messages from server
     * then do the appropriate action based on the messages types.
     * Count: is the number of group member when first message is sent, to check a member joined or left
     * Random: is called for making the random number to change the clock
     * to prevent the processes have the same clock after delivering a message
     * Also, we use the random number for the thread sleep
     * to make a random delay for simulating the messages concurrency
     */
    class ListenFromServer extends Thread {
        int count = 0;
        Random r = new Random();

        public void run() {
            while (true) {
                try {
                    // read the message form the input datastream
                    Message msg = (Message) sInput.readObject();
                    // type=0 is the first message that the process should send its clock
                    if (msg.getMsgType() == 0) {
                        // incrementing the local clock for replying
                        lg.incrementValue();
                        // number of members of group that server attached to the message
                        count = msg.getGcount();
                        // save the number of members of first message to the map
                        memeberCount.put(msg.getMessageId(), count);
                        // make a new message for replying to the request (clock reply)
                        Message msg1 = msg.getClockReplyMessage(lg.getValue());
                        // We makes the Thread sleep for simulating the random delay to provide the concurrency
                        Thread.sleep(r.nextInt(10) * 1000);
                        sendMessage(msg1);
                    }
                    // type=1 is the
                    if (msg.getMsgType() == 1) {
                        String msgID = msg.getMessageId();
                        // this condition is for new members, when a new process joined in the middle of time negotiation
                        // so the new process didn't receive the message with the type=0 hence didn't send its local time
                        // the new process should delete the message if it didn't receive message with type=0
                        if (memeberCount.containsKey(msgID)) {
                            // if the process received the message with type=0 then it should save the others local time
                            // to be able to compute the maximum time for deciding the global time
                            if (memeberCount.get(msgID) == msg.getGcount()) {
                                if (localTime.containsKey(msgID)) {
                                    List<Integer> ts = localTime.get(msgID);
                                    ts.add(msg.getLogicalClockValue());
                                    localTime.replace(msgID, ts);
                                } else {
                                    List<Integer> ts = new ArrayList<Integer>();
                                    ts.add(msg.getLogicalClockValue());
                                    localTime.put(msgID, ts);
                                }
                                // chatarea is the place for showing the messages
                                chatArea.append("\n" + msg.printMessage());
                                // if the process receive the message from all members then compute the global timestamp
                                if (localTime.get(msgID).size() == count) {
                                    List<Integer> ts = localTime.get(msgID);
                                    int maxtime = Collections.max(ts);
                                    msg.setLogicalClockValue(maxtime);
                                    msg.setDeliverable(true);
                                    globalTime.add(msg);
                                    //after put the message in global time array we don't need that any more
                                    // also to prevent conflict and more computation
                                    // we remove the message that added to global array
                                    localTime.remove(msg.getMessageId(), ts);
                                    //set process clock to maximum to deliver message
                                    lg.incrementValue(maxtime);
                                    deliver();
                                }
                            } else {
                                memeberCount.remove(msgID);
                                localTime.remove(msg);
                                chatArea.append("\n NEW MEMBER Joined/Left! ***The message with ID" + msgID + " is deleted, please resend it! ***");
                            }
                        } else
                            chatArea.append("\n NEW MEMBER Joined/Left! ***The message with ID" + msgID + " is deleted, please resend it! ***");
                    }//type 1
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    status.setText(notif + "Server.Server has closed the connection: " + e + notif);
                    break;
                }
            }
        }

        /**
         * the method is for delivering the message and then remove them from global time queue
         * then change the clock of process to a greater (random) clock
         * to prevent all process having the same clock after sending a message in order to simulate the processes
         * have different clocks again
         */
        private void deliver() {
            Message messages = globalTime.peek();
            globalTime.remove(messages);

            chatArea.append("\n" + messages.printMessage());
            //to simulate that process have different clock again
            lg = new LogicalClock();
            int newtime = lg.getValue() + messages.getLogicalClockValue();
            lg.incrementValue(newtime);

        }
    }


}
