package Server;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import Process.Message;

/**
 * @Author: Nasrin Seifi
 * Purpose: This class provides the Socket connection for processes,
 * Processes can send/receive message by sending the message to server then server distribute
 * the messages based on message's group to the specific group
 * this class has another class "ClientThread" that makes a thread for each process
 */
public class Server extends javax.swing.JFrame {
    private static int uniqueId; // a unique ID for each connection
    private ArrayList<ClientThread> al; // an ArrayList to keep the list of the Client
    private SimpleDateFormat sdf; // to display time
    private int port; // the port number to listen for connection
    private boolean keepGoing; // to check if server is running
    private String notif = " *** "; // notification
    private JPanel jPanel; // GUI panel
    private JTextArea chatArea; // Show messages
    private JScrollPane jScrollPane1;

    /**
     * constructor that receive the port to listen to for connection as parameter
     * First, it calls the initComponent method for running the GUI
     * @param port
     */
    public Server(int port) {
        initComponents();
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss"); // to display hh:mm:ss
        al = new ArrayList<ClientThread>(); // an ArrayList to keep the list of the Client
        this.setTitle("Server");
        this.setVisible(true);
    }

    /**
     * Usage: The method provides the GUI for the processes
     */
    private void initComponents() {
        jPanel = new JPanel();
        chatArea = new JTextArea();
        jScrollPane1 = new JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel.setLayout(null);

        chatArea.setColumns(20);
        chatArea.setRows(5);
        chatArea.setEditable(false);
        jScrollPane1.setViewportView(chatArea);

        jPanel.add(jScrollPane1);
        jScrollPane1.setBounds(30, 10, 360, 380);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(414, 428));
        setLocationRelativeTo(null);
    }

    /**
     * Usage: It is the first method that main calls to create the Socket connection
     * and after successful connection it is waiting for processes to join
     */
    public void start() {
        keepGoing = true;
        //create socket server and wait for connection requests
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);
            // infinite loop to wait for connections ( till server is active )
            while (keepGoing) {
                display("Server.Server waiting for Processes on port " + port + ".");
                // accept connection if requested from client
                Socket socket = serverSocket.accept();
                // break if server stopped
                if (!keepGoing)
                    break;
                // if client is connected, create its thread
                ClientThread t = new ClientThread(socket);
                //add this client to arraylist
                al.add(t);
                t.start();
            }
            // try to stop the server
            try {
                serverSocket.close();
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        // close all data streams and socket
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and Process: " + e);
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    /**
     * Usage: to stop the server
     */
    protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
        }
    }

    /**
     * Display an event(Process.Message) on the console
     * @param msg
     */
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        chatArea.append("\n" + time);
    }

    /**
     * Usage: to multicast a message to all Processes of the group
     * @param message
     * @return true if the message send successfully to a group
     * @throws IOException
     */
    private synchronized boolean sendMessage(Message message) throws IOException {
        int group = message.getGroup();
        int gcount = 0;
        // to gain the number of members in a group
        for (int i = al.size(); --i >= 0; ) {
            ClientThread ct = al.get(i);
            if (ct.getGroup() == group)
                gcount++;
        }
        // we loop in reverse order in case we would have to remove a Process that might be disconnected
        for (int i = al.size(); --i >= 0; ) {
            ClientThread ct = al.get(i);
            if (ct.getGroup() == group) {
                // check to send a message to its group not all processes
                message.setRecieverAddress(ct.getUsername());
                message.setGCount(gcount);
                // try to write to the process if it fails remove it from the list
                if (!ct.writeMsg(message)) {
                    al.remove(i);
                    display("Disconnected Process " + ct.username + " removed from list.");
                }
            }
        }
        return true;
    }

    /**
     *  Usage: if a process sent LOGOUT message, it will be removed from the array that contains process info.
     * @param id
     */
    synchronized void remove1(int id) {
        String disconnectedClient = "";
        // scan the array list until we found the Id
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // if found remove it
            if (ct.id == id) {
                disconnectedClient = ct.getUsername();
                al.remove(i);
                break;
            }
        }
    }

    /**
     *  To run as a console application
     * > java Server.Server
     * > java Server.Server portNumber
     * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch (args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server.Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server.Server [portNumber]");
                return;
        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /**
     * Purpose: One instance of this thread will run for each process and make connection to the server
     */
    class ClientThread extends Thread {
        Socket socket; // the socket to get messages from Process
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;      // my unique id (easier for disconnection)
        String username; // the Username of the Process
        int group; // message object to receive message and its type
        Message cm;
        String date; // timestamp

        /**
         * Constructor: receive the Socket and makes the connection for the process
         * @param socket
         */
        ClientThread(Socket socket) {
            id = ++uniqueId; // a unique id
            this.socket = socket;
            chatArea.append("\n Thread trying to create Object Input/Output Streams");
            //Creating both Data Stream
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // read the username
                String userG = (String) sInput.readObject();
                String[] splitUG = userG.split("-");
                username = splitUG[0];
                group = Integer.parseInt(splitUG[1]);
                display(notif + username + " has joined the group " + group + notif);
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        public String getUsername() {
            return username;
        }

        public int getGroup() {
            return group;
        }

        /**
         * Usage: the method provides the infinite loop to read and forward message
         */
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while (keepGoing) {
                // read a String (which is a Process.Message object)
                try {
                    cm = (Message) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // different actions based on type message
                switch (cm.getMsgType()) {
                    case 0:
                    case 1:
                    case 2:
                        try {
                            display(cm.printMessage());
                            boolean confirmation = sendMessage(cm);
                            if (confirmation == false) {
                                String msg = notif + "Sorry. No such user exists." + notif;
                                display(msg);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    case -1:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case -2:
                        display("List of the users connected at " + sdf.format(new Date()) + "\n");
                        // send list of active clients
                        for (int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            display((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // if out of the loop then disconnected and remove from client list
            remove1(id);
            close();
        }

        /**
         * Usage: teh method closes data streams
         */
        private void close() {
            try {
                if (sOutput != null) sOutput.close();
            } catch (Exception e) {
            }
            try {
                if (sInput != null) sInput.close();
            } catch (Exception e) {
            }
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
            }
        }

        /**
         * Usage: write a Process.Message object to the Process by output stream
         * @param msg
         * @return true if it could send message successfully, otherwise return false
         */
        private boolean writeMsg(Message msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                display(notif + "Error sending message to " + username + notif);
                display(e.toString());
            }
            return true;
        }
    }
}


