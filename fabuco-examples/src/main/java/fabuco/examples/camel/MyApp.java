package fabuco.examples.camel;

import fabuco.FabucoApplication;

public class MyApp {

  private static FabucoApplication fabucoApp;

  public static void main(String[] args) {
    fabucoApp = new FabucoApplication();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        fabucoApp.close();
      }
    });
  }

}
