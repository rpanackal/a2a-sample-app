package org.example.a2a.server.orchestration;

import com.sap.ai.sdk.orchestration.AssistantMessage;
import com.sap.ai.sdk.orchestration.OrchestrationChatResponse;
import com.sap.ai.sdk.orchestration.OrchestrationClient;
import com.sap.ai.sdk.orchestration.OrchestrationModuleConfig;
import com.sap.ai.sdk.orchestration.OrchestrationPrompt;
import com.sap.ai.sdk.orchestration.UserMessage;
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
    List<String> textParts =
        message.getParts().stream()
            .filter(TextPart.class::isInstance)
            .map(TextPart.class::cast)
            .map(TextPart::getText)
            .toList();

    return com.sap.ai.sdk.orchestration.Message.assistant(
        String.join(System.lineSeparator(), textParts));
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
