package Process;

import java.util.Scanner;

/**
 * @Author: Nasrin Seifi
 * The project class is the main class for running
 * We Cannot make a static reference to the non-static method, so we have to separate main from Process.ProcessGUI
 */
public class Project {
    public static void main(String[] args) {
        // default values if not entered
        int i = 0;
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";
        int group = 0;
        Scanner scan = new Scanner(System.in);
        String usergroup = scan.nextLine();
        String[] userG = usergroup.split("-");
        userName = userG[0];
        group = Integer.parseInt(userG[1]);
        // different case according to the length of the arguments.
        switch (args.length) {
            case 3:
                // for > javac Client username portNumber serverAddr
                serverAddress = args[2];
            case 2:
                // for > javac Client username portNumber
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:
                // for > javac Client username
                userName = args[0];
            case 0:
                // for > java Client
                break;
            // if number of arguments are invalid
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                return;
        }
        // create the process object
        ProcessGUI gs = new ProcessGUI(serverAddress, portNumber, userName, group);
        // try to connect to the server and return if could not connect
        if (!gs.start())
            return;

    }

}

