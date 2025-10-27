package com.sap.ai.sdk;

import static com.sap.ai.sdk.orchestration.OrchestrationAgent.toOrchestrationUserMessage;

import com.sap.ai.sdk.orchestration.OrchestrationAgent;
import com.sap.ai.sdk.orchestration.OrchestrationAiModel;
import com.sap.ai.sdk.orchestration.OrchestrationChatResponse;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TextPart;
import java.util.List;
import java.util.Optional;

public class StatefulAgentExecutor implements AgentExecutor {

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
    // TaskUpdater helps manage task lifecycle and emit events
    TaskUpdater updater = new TaskUpdater(context, eventQueue);

    // Only non-null if a task id was referenced in the request and the TaskStore is enabled.
    // A TaskStore is a server-side storage for persisting task state across multiple requests.
    Task existingTask = context.getTask();

    if (existingTask == null || existingTask.getStatus().state().isFinal()) {
      // Starts a new task if none exists.
      updater.submit();
    }

    // Marks the task as in progress to separate initial submission from actual work
    updater.startWork();

    // Extract current user message
    Message userMessage = context.getMessage();
    OrchestrationPrompt prompt = new OrchestrationPrompt(toOrchestrationUserMessage(userMessage));

    Optional.ofNullable(existingTask)
        .map(Task::getHistory)
        .map(OrchestrationAgent::toOrchestrationMessages)
        .ifPresent(prompt::messageHistory);

    OrchestrationModuleConfig config =
        new OrchestrationModuleConfig().withLlmConfig(OrchestrationAiModel.GPT_4O);
    OrchestrationChatResponse response = OrchestrationAgent.chat(prompt, config);

    // Update the task with the response artifact
    updater.addArtifact(List.of(new TextPart(response.getContent())), null, null, null);

    // Mark the task as complete
    updater.complete();
  }

  @Override
  public void cancel(RequestContext context, EventQueue eventQueue)
      throws JSONRPCError, UnsupportedOperationException {
    throw new UnsupportedOperationException("Cancellation not implemented yet");
  }
}
