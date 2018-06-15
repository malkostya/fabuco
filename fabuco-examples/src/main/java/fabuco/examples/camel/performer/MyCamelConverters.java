package fabuco.examples.camel.performer;

import fabuco.examples.camel.GetGreeting;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import org.apache.camel.Converter;

@Converter
public class MyCamelConverters {
  @Converter
  public static InputStream toInputStream(GetGreeting parameter) throws IOException {
    return new StringBufferInputStream(parameter.getName().toString());
  }
}
