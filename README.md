The program is pretty much easy to run and compile. It needs the jre version 12.
• Running:
First, run the project to see the result. (java -jar file.jar)
1. Run the Server.jar
If you consider changing the port, run it by port number default port number is: 1500
2. Run the Process.jar
If you consider changing the port, run it by port number. Otherwise, the default port number is 1500. After running, write the user name in the terminal(command line) in the following pattern:
Username-GroupID like nasrin-1then press enter to show the panel.
Run the Process as much as you want.
• Compiling:
There are two separate packages for server and process.
They could be compiled by the command line. (javac *.java)
If you consider running by command line (java file.java), it is the same rule:
1. Run the Server.java
2. Run the Project.java
• Working with Software:
The server is like a switch for sending and receiving the messages, so we run it first then it waits for processes to join.
The process does the most of program. After running and assigning the user name and group ID, the Panel for the Process is shown. Write the message in the field in front of the "send" button. Then, enter the "send" button to send it to its group. Others in other groups won't receive the message (Because it is a Multicast program). In the ChatArea you can see the messages' types and logical clock and the clock all processes delivered the message.
