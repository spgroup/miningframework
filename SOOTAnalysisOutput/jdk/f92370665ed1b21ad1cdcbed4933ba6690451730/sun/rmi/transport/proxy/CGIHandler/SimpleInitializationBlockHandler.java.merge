package sun.rmi.transport.proxy;

import java.io.*;
import java.net.*;
import java.util.Hashtable;

class CGIClientException extends Exception {

    private static final long serialVersionUID = 8147981687059865216L;

    public CGIClientException(String s) {
        super(s);
    }
}

class CGIServerException extends Exception {

    private static final long serialVersionUID = 6928425456704527017L;

    public CGIServerException(String s) {
        super(s);
    }
}

interface CGICommandHandler {

    public String getName();

    public void execute(String param) throws CGIClientException, CGIServerException;
}

public final class CGIHandler {

    static int ContentLength;

    static String QueryString;

    static String RequestMethod;

    static String ServerName;

    static int ServerPort;

    static {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

            public Void run() {
                ContentLength = Integer.getInteger("CONTENT_LENGTH", 0).intValue();
                QueryString = System.getProperty("QUERY_STRING", "");
                RequestMethod = System.getProperty("REQUEST_METHOD", "");
                ServerName = System.getProperty("SERVER_NAME", "");
                ServerPort = Integer.getInteger("SERVER_PORT", 0).intValue();
                return null;
            }
        });
    }

    private static CGICommandHandler[] commands = { new CGIForwardCommand(), new CGIGethostnameCommand(), new CGIPingCommand(), new CGITryHostnameCommand() };

    private static Hashtable<String, CGICommandHandler> commandLookup;

    static {
        commandLookup = new Hashtable<>();
        for (int i = 0; i < commands.length; ++i) commandLookup.put(commands[i].getName(), commands[i]);
    }

    private CGIHandler() {
    }

    public static void main(String[] args) {
        try {
            String command, param;
            int delim = QueryString.indexOf("=");
            if (delim == -1) {
                command = QueryString;
                param = "";
            } else {
                command = QueryString.substring(0, delim);
                param = QueryString.substring(delim + 1);
            }
            CGICommandHandler handler = commandLookup.get(command);
            if (handler != null)
                try {
                    handler.execute(param);
                } catch (CGIClientException e) {
                    returnClientError(e.getMessage());
                } catch (CGIServerException e) {
                    returnServerError(e.getMessage());
                }
            else
                returnClientError("invalid command.");
        } catch (Exception e) {
            returnServerError("internal error: " + e.getMessage());
        }
        System.exit(0);
    }

    private static void returnClientError(String message) {
        System.out.println("Status: 400 Bad Request: " + message);
        System.out.println("Content-type: text/html");
        System.out.println("");
        System.out.println("<HTML>" + "<HEAD><TITLE>Java RMI Client Error" + "</TITLE></HEAD>" + "<BODY>");
        System.out.println("<H1>Java RMI Client Error</H1>");
        System.out.println("");
        System.out.println(message);
        System.out.println("</BODY></HTML>");
        System.exit(1);
    }

    private static void returnServerError(String message) {
        System.out.println("Status: 500 Server Error: " + message);
        System.out.println("Content-type: text/html");
        System.out.println("");
        System.out.println("<HTML>" + "<HEAD><TITLE>Java RMI Server Error" + "</TITLE></HEAD>" + "<BODY>");
        System.out.println("<H1>Java RMI Server Error</H1>");
        System.out.println("");
        System.out.println(message);
        System.out.println("</BODY></HTML>");
        System.exit(1);
    }
}

final class CGIForwardCommand implements CGICommandHandler {

    public String getName() {
        return "forward";
    }

    @SuppressWarnings("deprecation")
    private String getLine(DataInputStream socketIn) throws IOException {
        return socketIn.readLine();
    }

    public void execute(String param) throws CGIClientException, CGIServerException {
        if (!CGIHandler.RequestMethod.equals("POST"))
            throw new CGIClientException("can only forward POST requests");
        int port;
        try {
            port = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new CGIClientException("invalid port number.");
        }
        if (port <= 0 || port > 0xFFFF)
            throw new CGIClientException("invalid port: " + port);
        if (port < 1024)
            throw new CGIClientException("permission denied for port: " + port);
        byte[] buffer;
        Socket socket;
        try {
            socket = new Socket(InetAddress.getLocalHost(), port);
        } catch (IOException e) {
            throw new CGIServerException("could not connect to local port");
        }
        DataInputStream clientIn = new DataInputStream(System.in);
        buffer = new byte[CGIHandler.ContentLength];
        try {
            clientIn.readFully(buffer);
        } catch (EOFException e) {
            throw new CGIClientException("unexpected EOF reading request body");
        } catch (IOException e) {
            throw new CGIClientException("error reading request body");
        }
        try {
            DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
            socketOut.writeBytes("POST / HTTP/1.0\r\n");
            socketOut.writeBytes("Content-length: " + CGIHandler.ContentLength + "\r\n\r\n");
            socketOut.write(buffer);
            socketOut.flush();
        } catch (IOException e) {
            throw new CGIServerException("error writing to server");
        }
        DataInputStream socketIn;
        try {
            socketIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new CGIServerException("error reading from server");
        }
        String key = "Content-length:".toLowerCase();
        boolean contentLengthFound = false;
        String line;
        int responseContentLength = -1;
        do {
            try {
                line = getLine(socketIn);
            } catch (IOException e) {
                throw new CGIServerException("error reading from server");
            }
            if (line == null)
                throw new CGIServerException("unexpected EOF reading server response");
            if (line.toLowerCase().startsWith(key)) {
                if (contentLengthFound) {
                    throw new CGIServerException("Multiple Content-length entries found.");
                } else {
                    responseContentLength = Integer.parseInt(line.substring(key.length()).trim());
                    contentLengthFound = true;
                }
            }
        } while ((line.length() != 0) && (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
        if (!contentLengthFound || responseContentLength < 0)
            throw new CGIServerException("missing or invalid content length in server response");
        buffer = new byte[responseContentLength];
        try {
            socketIn.readFully(buffer);
        } catch (EOFException e) {
            throw new CGIServerException("unexpected EOF reading server response");
        } catch (IOException e) {
            throw new CGIServerException("error reading from server");
        }
        System.out.println("Status: 200 OK");
        System.out.println("Content-type: application/octet-stream");
        System.out.println("");
        try {
            System.out.write(buffer);
        } catch (IOException e) {
            throw new CGIServerException("error writing response");
        }
        System.out.flush();
    }
}

final class CGIGethostnameCommand implements CGICommandHandler {

    public String getName() {
        return "gethostname";
    }

    public void execute(String param) {
        System.out.println("Status: 200 OK");
        System.out.println("Content-type: application/octet-stream");
        System.out.println("Content-length: " + CGIHandler.ServerName.length());
        System.out.println("");
        System.out.print(CGIHandler.ServerName);
        System.out.flush();
    }
}

final class CGIPingCommand implements CGICommandHandler {

    public String getName() {
        return "ping";
    }

    public void execute(String param) {
        System.out.println("Status: 200 OK");
        System.out.println("Content-type: application/octet-stream");
        System.out.println("Content-length: 0");
        System.out.println("");
    }
}

final class CGITryHostnameCommand implements CGICommandHandler {

    public String getName() {
        return "tryhostname";
    }

    public void execute(String param) {
        System.out.println("Status: 200 OK");
        System.out.println("Content-type: text/html");
        System.out.println("");
        System.out.println("<HTML>" + "<HEAD><TITLE>Java RMI Server Hostname Info" + "</TITLE></HEAD>" + "<BODY>");
        System.out.println("<H1>Java RMI Server Hostname Info</H1>");
        System.out.println("<H2>Local host name available to Java VM:</H2>");
        System.out.print("<P>InetAddress.getLocalHost().getHostName()");
        try {
            String localHostName = InetAddress.getLocalHost().getHostName();
            System.out.println(" = " + localHostName);
        } catch (UnknownHostException e) {
            System.out.println(" threw java.net.UnknownHostException");
        }
        System.out.println("<H2>Server host information obtained through CGI interface from HTTP server:</H2>");
        System.out.println("<P>SERVER_NAME = " + CGIHandler.ServerName);
        System.out.println("<P>SERVER_PORT = " + CGIHandler.ServerPort);
        System.out.println("</BODY></HTML>");
    }
}