package fabuco.process;

import java.io.Serializable;

/**
 * An interface is to be extended by the parameter of process.
 *
 * @param <R> defines the type of process' result class.
 */
public interface ProcessParameter<R> extends Serializable {

  ParameterKey getKey();
}
