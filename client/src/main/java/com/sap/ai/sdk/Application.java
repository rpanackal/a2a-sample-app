package com.sap.ai.sdk;

import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.http.JdkA2AHttpClient;
import io.a2a.client.transport.rest.RestTransport;
import io.a2a.client.transport.rest.RestTransportConfig;
import io.a2a.spec.A2AClientError;
import io.a2a.spec.A2AClientException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.TextPart;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

  private static final String URL = "http://localhost:8080";

  public static void main(String[] args) {
    try {
      AgentCard agentCard = getAgentCard();
      List<BiConsumer<ClientEvent, AgentCard>> callbacks = getAllEventCallbacks();
      Consumer<Throwable> errorHandler =
          error -> {
            throw new UnsupportedOperationException("Error handling not implemented yet");
          };

      Client agent = getAgent(agentCard, callbacks, errorHandler);
      agent.sendMessage(A2A.toUserMessage("What is the weather in Potsdam?"));

    } catch (A2AClientError e) {
      throw new RuntimeException("Agent card retrieval failed", e);
    } catch (A2AClientException e) {
      throw new RuntimeException("Client initialization failed", e);
    }
  }

  private static AgentCard getAgentCard() throws A2AClientError {
    return new A2ACardResolver(getCardResolutionHttpClient(), URL).getAgentCard();
  }

  private static Client getAgent(
      AgentCard agentCard,
      List<BiConsumer<ClientEvent, AgentCard>> consumers,
      Consumer<Throwable> errorHandler)
      throws A2AClientException {
    ClientConfig clientConfig =
        new ClientConfig.Builder().setAcceptedOutputModes(List.of("text/plain")).build();

    return Client.builder(agentCard)
        .clientConfig(clientConfig)
        .withTransport(RestTransport.class, new RestTransportConfig(getMessagingHttpClient()))
        .addConsumers(consumers)
        .streamingErrorHandler(errorHandler)
        .build();
  }

  private static A2AHttpClient getCardResolutionHttpClient() {
    return new LoggingA2AHttpClient(new JdkA2AHttpClient());
  }

  private static A2AHttpClient getMessagingHttpClient() {
    return new LoggingA2AHttpClient(new JdkA2AHttpClient());
  }

  private static List<BiConsumer<ClientEvent, AgentCard>> getAllEventCallbacks() {
    return List.of(
        (event, card) -> {
          if (event instanceof MessageEvent messageEvent) {
            messageCallback(messageEvent);
          } else if (event instanceof TaskEvent taskEvent) {
            taskCallback(taskEvent);
          } else if (event instanceof TaskUpdateEvent updateEvent) {
            throw new UnsupportedOperationException("Task update handling not implemented yet");
          }
        });
  }

  private static void messageCallback(MessageEvent messageEvent) {
    log.info(
        "Received: \n{}",
        messageEvent.getMessage().getParts().stream()
            .filter(TextPart.class::isInstance)
            .map(TextPart.class::cast)
            .map(TextPart::getText)
            .toList());
  }

  private static void taskCallback(TaskEvent taskEvent) {
    log.info("Received Task Id: {}", taskEvent.getTask().getId());
  }
}
