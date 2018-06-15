package fabuco.impl.util.scanner;

import java.util.List;
import lombok.Value;

@Value
public class PerformerInfos {

  private final List<PerformerInfo> performerInfoList;
  private final List<PerformerMethodInfo> performerMethodInfos;
}
