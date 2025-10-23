package com.sap.ai.sdk;

import io.a2a.server.PublicAgentCard;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class AgentConfiguration {
  @Produces
  public AgentExecutor agentExecutorStateful() {
    return new StatefulAgentExecutor();
  }

  @Produces
  @DefaultBean
  public AgentExecutor agentExecutorStateless() {
    return new MinimalAgentExecutor();
  }

  @Produces
  @PublicAgentCard
  public AgentCard agentCard() {
    return new AgentCard.Builder()
        .name("Weather Agent")
        .description("An agent that retrieves and forecasts weather information.")
        .url("http://localhost:8080/")
        .version("1.0.0")
        .defaultInputModes(List.of("text/plain"))
        .defaultOutputModes(List.of("text/plain"))
        .capabilities(new AgentCapabilities.Builder().streaming(false).build())
        .skills(getAgentSkills())
        .build();
  }

  private static List<AgentSkill> getAgentSkills() {
    AgentSkill skill =
        new AgentSkill.Builder()
            .id("weather_search")
            .name("Search weather")
            .description("Helps with weather in city, or states")
            .tags(Collections.singletonList("weather"))
            .examples(List.of("weather in LA, CA"))
            .build();
    return List.of(skill);
  }
}
