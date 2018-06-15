package fabuco.impl.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import fabuco.impl.core.FabucoConstants;
import fabuco.impl.consumer.config.SortedOrdersConfig;
import fabuco.impl.consumer.config.UnsortedOrdersConfig;
import fabuco.impl.executor.config.PerformerConfig;
import fabuco.impl.executor.config.ProcessConfig;
import fabuco.performer.PerformerParameter;
import fabuco.process.ProcessParameter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class FabucoConfigFactory {

  private FabucoSystemConfig systemConfig;
  private Map<String, SortedOrdersConfig> sortedOrdersConfigs;
  private UnsortedOrdersConfig unsortedOrdersConfig;
  private Map<String, ProcessConfig> processConfigs;
  private Map<String, PerformerConfig> performerConfigs;
  private SortedOrdersConfig sortedOrdersDefaultConfig;
  private ProcessConfig processDefaultConfig;
  private PerformerConfig performerDefaultConfig;

  public FabucoConfigFactory() {
    Config config = ConfigFactory.load("fabuco.conf");
    Config consumeConfig = config.getConfig("consumer");
    systemConfig = new FabucoSystemConfig(config.getConfig("system"));
    unsortedOrdersConfig = new UnsortedOrdersConfig(consumeConfig.getConfig("unsorted-group"));
    fillSortedOrdersConfigs(consumeConfig);
    fillProcessConfigs(config);
    fillPerformerConfigs(config);
  }

  public FabucoSystemConfig getSystemConfig() {
    return systemConfig;
  }

  public Set<String> getSortedGroups() {
    return sortedOrdersConfigs.keySet();
  }

  public SortedOrdersConfig getSortedOrdersConfig(String sortedGroup) {
    SortedOrdersConfig config = sortedOrdersConfigs.get(sortedGroup);
    return config != null ? config : sortedOrdersDefaultConfig;
  }

  public UnsortedOrdersConfig getUnsortedOrdersConfig() {
    return unsortedOrdersConfig;
  }

  public ProcessConfig getProcessParametersConfig(
      Class<? extends ProcessParameter> parameterType) {
    ProcessConfig config = processConfigs.get(parameterType.getName());
    return config != null ? config : processDefaultConfig;
  }

  public PerformerConfig getPerformerParametersConfig(
      Class<? extends PerformerParameter> parameterType) {
    PerformerConfig config = performerConfigs.get(parameterType.getName());
    return config != null ? config : performerDefaultConfig;
  }

  private void fillSortedOrdersConfigs(Config config) {
    FabucoConfigInfo<SortedOrdersConfig> info = getConfigs(config.getConfig("sorted-groups"),
        SortedOrdersConfig.class);
    sortedOrdersDefaultConfig = info.getDefaultConfig();
    sortedOrdersConfigs = info.getConfigs();
  }

  private void fillProcessConfigs(Config config) {
    FabucoConfigInfo<ProcessConfig> info = getConfigs(config.getConfig("process-parameters"),
        ProcessConfig.class);
    processDefaultConfig = info.getDefaultConfig();
    processConfigs = info.getConfigs();
  }

  private void fillPerformerConfigs(Config config) {
    FabucoConfigInfo<PerformerConfig> info = getConfigs(config.getConfig("performer-parameters"),
        PerformerConfig.class);
    performerDefaultConfig = info.getDefaultConfig();
    performerConfigs = info.getConfigs();
  }

  private <T extends FabucoConfig> FabucoConfigInfo<T> getConfigs(Config config,
      Class<T> configType) {
    final Config defaultConfig = config.getConfig(FabucoConstants.DEFAULT_CONFIG_PATH);
    final Map<String, T> configs = config.root()
        .entrySet()
        .stream()
        .filter(e -> !e.getKey().equals(FabucoConstants.DEFAULT_CONFIG_PATH))
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> newConfig(configType, ((ConfigObject) e.getValue()).toConfig(), defaultConfig)
        ));
    return new FabucoConfigInfo<>(newConfig(configType, null, defaultConfig), configs);
  }

  private <T extends FabucoConfig> T newConfig(Class<T> configType, Config config,
      Config defaultConfig) {
    try {
      T result = configType.newInstance();
      result.init(config, defaultConfig);
      return result;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
