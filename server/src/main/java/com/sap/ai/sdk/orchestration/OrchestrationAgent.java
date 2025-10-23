package com.sap.ai.sdk.orchestration;

import io.a2a.server.agentexecution.RequestContext;
import io.a2a.spec.Message;
import io.a2a.spec.TextPart;
import java.util.List;
import java.util.UUID;

public class OrchestrationAgent {

  public static OrchestrationChatResponse chat(
      OrchestrationPrompt prompt, OrchestrationModuleConfig config) {
    return new OrchestrationClient().chatCompletion(prompt, config);
  }

  public static UserMessage toOrchestrationUserMessage(Message message) {
    List<String> textParts =
        message.getParts().stream()
            .filter(TextPart.class::isInstance)
            .map(TextPart.class::cast)
            .map(TextPart::getText)
            .toList();

    UserMessage userMessage = com.sap.ai.sdk.orchestration.Message.user(textParts.get(0));
    textParts.stream().skip(1).forEach(userMessage::withText);
    return userMessage;
  }

  public static List<com.sap.ai.sdk.orchestration.Message> toOrchestrationMessages(
      List<Message> messages) {
    return messages.stream()
        .filter(m -> m.getRole() == Message.Role.USER)
        .map(OrchestrationAgent::toOrchestrationUserMessage)
        .map(com.sap.ai.sdk.orchestration.Message.class::cast)
        .toList();
  }

  public static Message toA2AMessage(RequestContext request, OrchestrationChatResponse response) {
    return new Message.Builder()
        .role(Message.Role.AGENT)
        .messageId(UUID.randomUUID().toString())
        .contextId(request.getContextId())
        .taskId(request.getTaskId())
        .parts(List.of(new TextPart(response.getContent())))
        .build();
  }
}
