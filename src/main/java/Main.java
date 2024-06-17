public class Main {
  public static void main(String[] args) {
    HttpServer server = new HttpServer(4221, 10, args);
    server.run();
  }
}


