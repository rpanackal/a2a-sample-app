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

  private static AssistantMessage toOrchestrationAssistantMessage(Message message) {
    List<ContentItem> textItems =
        message.getParts().stream()
            .filter(TextPart.class::isInstance)
            .map(TextPart.class::cast)
            .map(TextPart::getText)
            .<ContentItem>map(TextItem::new)
            .toList();
    return new AssistantMessage(new MessageContent(textItems));
  }

  public static List<com.sap.ai.sdk.orchestration.Message> toOrchestrationMessages(
      List<Message> messages) {
    return messages.stream()
        .map(
            msg ->
                switch (msg.getRole()) {
                  case USER -> toOrchestrationUserMessage(msg);
                  case AGENT -> toOrchestrationAssistantMessage(msg);
                })
        .toList();
  }

  public static Message toA2AMessage(RequestContext context, OrchestrationChatResponse response) {
    return new Message.Builder()
        .role(Message.Role.AGENT)
        .messageId(UUID.randomUUID().toString())
        .contextId(context.getContextId())
        .taskId(context.getTaskId())
        .parts(List.of(new TextPart(response.getContent())))
        .build();
  }
}
