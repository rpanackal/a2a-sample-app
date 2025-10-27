package org.example.a2a.server;

import static org.example.a2a.server.orchestration.OrchestrationAgent.toA2AMessage;
import static org.example.a2a.server.orchestration.OrchestrationAgent.toOrchestrationUserMessage;

import org.example.a2a.server.orchestration.OrchestrationAgent;
import com.sap.ai.sdk.orchestration.OrchestrationAiModel;
import com.sap.ai.sdk.orchestration.OrchestrationChatResponse;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import com.sap.ai.sdk.orchestration.UserMessage;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.JSONRPCError;

public class MinimalAgentExecutor implements AgentExecutor {

  /**
   * Handles incoming message for all supported transport mechanisms.
   *
   * <p>More precisely, this method is invoked when the client sends a message using the {@code
   * sendMessage(...)} method.
   *
   * @param context The request context containing incoming data and any related state stored on the
   *     server-side, such as tasks.
   * @param eventQueue The event queue used to enqueue response events to be sent back to the
   *     client.
   * @throws JSONRPCError If an error occurs during the processing of the JSON-RPC transport.
   */
  @Override
  public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
    UserMessage userMessage = toOrchestrationUserMessage(context.getMessage());

    OrchestrationPrompt prompt = new OrchestrationPrompt(userMessage);
    OrchestrationModuleConfig config =
        new OrchestrationModuleConfig().withLlmConfig(OrchestrationAiModel.GPT_4O);
    OrchestrationChatResponse response = OrchestrationAgent.chat(prompt, config);

    // Response is consumed by the EventQueue to send back to the client
    eventQueue.enqueueEvent(toA2AMessage(context, response));
  }

  @Override
  public void cancel(RequestContext context, EventQueue eventQueue)
      throws JSONRPCError, UnsupportedOperationException {
    throw new UnsupportedOperationException("Cancellation not implemented yet");
  }
}
