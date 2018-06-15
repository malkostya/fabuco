package fabuco.impl.storage;

public class FabucoStorageFactory {

  private FabucoStorage storage;

  public FabucoStorageFactory() {
    storage = new FakeStorage();
  }

  public FabucoStorage getStorage() {
    return storage;
  }
}
