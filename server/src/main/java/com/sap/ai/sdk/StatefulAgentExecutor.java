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

  @Override
  public void execute(RequestContext request, EventQueue eventQueue) throws JSONRPCError {

    // TaskUpdater helps manage task lifecycle and state transitions.
    TaskUpdater updater = new TaskUpdater(request, eventQueue);

    Task existingTask = request.getTask();
    if (existingTask == null) {
      // Starts a new task if none exists
      updater.submit();
    }

    // Marks the task as in progress to separate initial submission from actual work
    updater.startWork();

    // Extract current user message
    Message userMessage = request.getMessage();
    OrchestrationPrompt prompt = new OrchestrationPrompt(toOrchestrationUserMessage(userMessage));

    // TODO: What if the message history contains non-user messages?
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
