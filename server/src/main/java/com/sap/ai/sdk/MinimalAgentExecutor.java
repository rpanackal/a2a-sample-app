package com.sap.ai.sdk;

import static com.sap.ai.sdk.orchestration.OrchestrationAgent.toA2AMessage;
import static com.sap.ai.sdk.orchestration.OrchestrationAgent.toOrchestrationUserMessage;

import com.sap.ai.sdk.orchestration.OrchestrationAgent;
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

  @Override
  public void execute(RequestContext request, EventQueue eventQueue) throws JSONRPCError {
    UserMessage userMessage = toOrchestrationUserMessage(request.getMessage());

    OrchestrationPrompt prompt = new OrchestrationPrompt(userMessage);
    OrchestrationModuleConfig config =
        new OrchestrationModuleConfig().withLlmConfig(OrchestrationAiModel.GPT_4O);
    OrchestrationChatResponse response = OrchestrationAgent.chat(prompt, config);

    // Response is consumed by the EventQueue to send back to the client
    eventQueue.enqueueEvent(toA2AMessage(request, response));
  }

  @Override
  public void cancel(RequestContext context, EventQueue eventQueue)
      throws JSONRPCError, UnsupportedOperationException {
    throw new UnsupportedOperationException("Cancellation not implemented yet");
  }
}
