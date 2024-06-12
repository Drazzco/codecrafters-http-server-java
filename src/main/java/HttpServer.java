import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final ExecutorService exec;

    public HttpServer(final int port, final int concurrencyLevel) {
        this.port = port;
        this.exec = Executors.newFixedThreadPool(concurrencyLevel);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                exec.submit(() -> handleRequest(clientSocket));
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private void handleRequest(Socket clientSocket) {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            String[] HttpRequest = line.split(" ");
            OutputStream output = clientSocket.getOutputStream();
            String response;
            String endpoint = getEndpoint(HttpRequest[1]);
            switch (endpoint) {
                case "/":
                    System.out.println("version");
                    response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + 
                    "Content-Length: 0\r\n\r\n";
                    output.write(response.getBytes());
                    break;
                case "echo":
                    String queryParam = HttpRequest[1].split("/")[2];
                    response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + 
                    "Content-Length: " + queryParam.length() + "\r\n\r\n" + queryParam;
                    output.write(response.getBytes());
                    break;
                case "user-agent":
                    reader.readLine();
                    String userAgent = reader.readLine().split("\\s+")[1];
                    response = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s\r\n", 
                    userAgent.length(), userAgent);
                    output.write(response.getBytes());
                    break;
                default:
                    response = "HTTP/1.1 404 Not Found\r\n\r\n";
                    output.write(response.getBytes());
            }
            output.flush();
            System.out.println("accepted new connection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getEndpoint(String httpRequest) {
        if(httpRequest.equals("/")) {
          return "/";
        }
        String[] command = httpRequest.split("/");
        if(command[1].equals("echo") && command.length > 2) {
          return "echo";
        }
        if(command[1].equals("user-agent")) {
          return "user-agent";
        }
        return "404";
      }
}
