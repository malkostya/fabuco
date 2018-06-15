package fabuco.process;

public interface ProcessCall<P extends ProcessParameter<R>, R> {

  boolean succeeded();

  P getParameter();

  R getResult();

  ProcessError getError();

}
