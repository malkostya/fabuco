package fabuco.impl.util.scanner;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessParameter;
import lombok.Getter;

@Getter
public class ProcessInfo {

  private Class<? extends ProcessParameter> parameterType;
  private Class<? extends FabucoProcess> processType;
  private Class<?> resultType;
  private ProcessInfo parentInfo;
  private boolean childFree = true;

  public ProcessInfo(Class<? extends FabucoProcess> processType) {
    this.processType = processType;
  }

  public void setParameterType(Class<? extends ProcessParameter> parameterType) {
    this.parameterType = parameterType;
  }

  public void setResultType(Class<?> resultType) {
    this.resultType = resultType;
  }

  public void setParentInfo(ProcessInfo parentInfo) {
    this.parentInfo = parentInfo;
  }

  public void setHasChildren() {
    this.childFree = false;
  }
}
