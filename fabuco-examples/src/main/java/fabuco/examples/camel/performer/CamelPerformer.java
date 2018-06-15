package fabuco.examples.camel.performer;

import fabuco.examples.camel.GetGreeting;
import fabuco.impl.annotation.Performer;
import fabuco.impl.annotation.PerformerDestroy;
import fabuco.impl.annotation.PerformerInit;
import fabuco.impl.annotation.PerformerMethod;
import fabuco.performer.PerformerCallContext;
import fabuco.performer.PerformerParameter;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.ExchangeHelper;

@Performer
public class CamelPerformer {

  protected ProducerTemplate producer;
  protected CamelContext camel;
  protected Map<Class<? extends PerformerParameter>, Endpoint> endpoints = new HashMap<>();

  @PerformerInit
  public void start() throws Exception {
    camel = new DefaultCamelContext();
    camel.start();
    producer = camel.createProducerTemplate();
    camel.addRoutes(new RouteBuilder() {
      public void configure() {
        from("jetty://http://localhost:8888/greeting")
            .setBody(simple("Hello, ", String.class))
            .convertBodyTo(String.class, "UTF-8");
        from("direct:mytest").to("jetty://http://localhost:8888/greeting");
      }
    });
    fillEndpoints();
  }

  private void fillEndpoints() {
    endpoints.put(GetGreeting.class, camel.getEndpoint("direct:mytest"));
  }

  @PerformerDestroy
  public void stop() throws Exception {
    camel.stop();
  }

  @PerformerMethod
  public void call(PerformerCallContext<GetGreeting, String> context) {
    Endpoint endpoint = endpoints.get(context.getParameter().getClass());
    Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
    exchange.getIn().setBody(context.getParameter());

    exchange = producer.send(endpoint, exchange);
    if (exchange.isFailed()) {
      if (ExchangeHelper.hasFaultMessage(exchange)) {
        context.setFailed((String) exchange.getOut().getBody());
      } else {
        context.setRecoverableError(exchange.getException());
      }
    } else {
      context.setCompleted(
          new String((byte[]) exchange.getOut().getBody()) + context.getParameter().getName()
              + "!");
    }
  }
}
