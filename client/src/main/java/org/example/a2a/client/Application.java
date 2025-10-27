package org.example.a2a.client;

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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

  // A latch used to wait for asynchronous operations to complete
  private static final CountDownLatch LATCH = new CountDownLatch(1);
  // The base URL for the A2A service
  private static final String URL = "http://localhost:8080";
  // Flag to indicate whether streaming mode is enabled
  private static final boolean IS_STREAMING = false;

  /**
   * Main entry point of the application. Initializes the agent client, sends a message, and waits
   * for server events.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    try {
      // Retrieve the agent card
      AgentCard agentCard = getAgentCard();
      // Get all event callbacks
      List<BiConsumer<ClientEvent, AgentCard>> callbacks = getAllEventCallbacks();
      // Define an error handler for streaming errors
      Consumer<Throwable> errorHandler =
          error -> {
            throw new UnsupportedOperationException("Error handling not implemented yet");
          };

      // Create the agent client
      Client agent = getAgent(agentCard, callbacks, errorHandler);
      // Send a message to the agent
      agent.sendMessage(A2A.toUserMessage("What is the weather in Potsdam?"));

      // Wait until the latch is counted down to 0
      LATCH.await();

    } catch (A2AClientError e) {
      throw new RuntimeException("Agent card retrieval failed", e);
    } catch (A2AClientException e) {
      throw new RuntimeException("Client initialization failed", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Waiting for server events was interrupted", e);
    }
  }

  /**
   * Retrieves the agent card from the A2A service.
   *
   * @return The agent card
   * @throws A2AClientError If the agent card retrieval fails
   */
  private static AgentCard getAgentCard() throws A2AClientError {
    return new A2ACardResolver(getCardResolutionHttpClient(), URL).getAgentCard();
  }

  /**
   * Creates an agent client with the specified configuration.
   *
   * @param agentCard The agent card
   * @param consumers The event consumers
   * @param errorHandler The error handler for streaming errors
   * @return The agent client
   * @throws A2AClientException If client creation fails
   */
  private static Client getAgent(
      AgentCard agentCard,
      List<BiConsumer<ClientEvent, AgentCard>> consumers,
      Consumer<Throwable> errorHandler)
      throws A2AClientException {
    ClientConfig clientConfig =
        new ClientConfig.Builder()
            .setAcceptedOutputModes(List.of("text/plain"))
            .setStreaming(IS_STREAMING) // When true, sendMessage is streaming if server supports it
            .build();

    return Client.builder(agentCard)
        .clientConfig(clientConfig)
        // Set up REST transport with messaging HTTP client
        .withTransport(RestTransport.class, new RestTransportConfig(getMessagingHttpClient()))
        .addConsumers(consumers)
        .streamingErrorHandler(errorHandler)
        .build();
  }

  /**
   * Creates an HTTP client for card resolution.
   *
   * <p>May use different transport for agent card resolution
   *
   * @return The HTTP client
   */
  private static A2AHttpClient getCardResolutionHttpClient() {
    return new LoggingA2AHttpClient(new JdkA2AHttpClient());
  }

  /**
   * Creates an HTTP client for messaging.
   *
   * @return The HTTP client
   */
  private static A2AHttpClient getMessagingHttpClient() {
    return new LoggingA2AHttpClient(new JdkA2AHttpClient());
  }

  /**
   * Retrieves all event callbacks for the agent client.
   *
   * @return A list of event callbacks
   */
  private static List<BiConsumer<ClientEvent, AgentCard>> getAllEventCallbacks() {
    return List.of(
        (event, card) -> {
          if (event instanceof MessageEvent messageEvent) {
            messageCallback(messageEvent);
          } else if (event instanceof TaskEvent taskEvent) {
            taskCallback(taskEvent);
          } else if (event instanceof TaskUpdateEvent updateEvent) {
            taskUpdateCallback(updateEvent);
          }
        });
  }

  /**
   * Callback for task update events when streaming is enabled.
   *
   * @param updateEvent The task update event
   */
  private static void taskUpdateCallback(TaskUpdateEvent updateEvent) {
    log.info("Task Update, Id: {}", updateEvent.getTask().getId());
    if (updateEvent.getTask().getStatus().state().isFinal()) {
      LATCH.countDown();
    }
  }

  /**
   * Callback for message events.
   *
   * @param messageEvent The message event
   */
  private static void messageCallback(MessageEvent messageEvent) {
    log.info("Message Received, Id: {}", messageEvent.getMessage().getMessageId());
    LATCH.countDown();
  }

  /**
   * Callback for task events when streaming is disabled.
   *
   * @param taskEvent The task event
   */
  private static void taskCallback(TaskEvent taskEvent) {
    log.info("Task Processed, Id: {}", taskEvent.getTask().getId());
    LATCH.countDown();
  }
}
