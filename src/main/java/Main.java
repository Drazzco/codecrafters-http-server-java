import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  void main(String[] args) {
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    try {
      //TCP Connection
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors 1
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept(); // Wait for connection from client.
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
      System.out.println("IOException: " + e.getMessage());
    }
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
