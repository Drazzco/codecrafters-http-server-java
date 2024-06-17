import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final ExecutorService exec;
    private final String directory;

    public HttpServer(final int port, final int concurrencyLevel, final String directory) {
        this.port = port;
        this.exec = Executors.newFixedThreadPool(concurrencyLevel);
        this.directory = directory;
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
            StringBuilder data = new StringBuilder();
            while(reader.ready()) {
                data.append((char)reader.read());
            }
            String preBody = data.toString();
            String body = null;
            if(!preBody.isEmpty()) {
                String[] allBody = preBody.split("\r\n");
                body = allBody[3];
            }
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
                case "files":
                    String fileName = HttpRequest[1].substring(7);
                    if(body == null) {
                        File file = new File(directory, fileName);
                        if(file.exists()) {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            String content = new String(fileContent);
                            response = "HTTP/1.1 200 OK\r\n" + "Content-Type: application/octet-stream\r\n" + 
                                "Content-Length: " + fileContent.length + "\r\n\r\n" + content;
                            output.write(response.getBytes());
                        } else {
                            response = "HTTP/1.1 404 Not Found\r\n\r\n";
                            output.write(response.getBytes());
                        }
                    } else {
                        Path path = Paths.get(directory + fileName);
                        Files.write(path, body.getBytes());
                        response = "HTTP/1.1 201 Created\r\n\r\n";
                        output.write(response.getBytes());
                        output.write(body.getBytes());
                    }
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

    private String getEndpoint(String httpRequest) {
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
        if(httpRequest.startsWith("/files/")) {
            return "files";
        }
        return "404";
      }
}
