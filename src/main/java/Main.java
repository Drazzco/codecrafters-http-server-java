public class Main {
  public static void main(String[] args) {
    String dir = null;
    if (args.length > 1 && args[0].equals("--directory")) {
      dir = args[1];
    }
    final HttpServer server = new HttpServer(4221, 10, dir);
    server.run();
  }
}


