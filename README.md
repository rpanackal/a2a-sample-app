# A2A Sample Application

This project demonstrates a minimal implementation of an Agent-to-Agent (A2A) client and server integrating the A2A Java SDK with the SAP AI SDK for Java. The A2A protocol defines a standard for communication between AI agents, allowing them to collaborate and exchange information efficiently.

## Project Structure

- **server/**: A2A server contains implementation of `AgentExecutor`, the transport agnostic component that processes incoming A2A messages and generates appropriate responses.
    - A stateless (fire-and-forget) agent that responds to weather queries with `Message` events only
    - A stateful agent that responds with `Task` events only. Look at the [Resources](#resources) section for more details on Tasks vs Messages
- **client/**: A2A client implementation for connecting to and communicating with the server

The project is configured to use REST Transport instead of the default JSON-RPC.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- SAP AI Core service key

## Technology Stack

- **Quarkus**: Server framework
- **A2A Java SDK**: Agent-to-Agent protocol implementation
- **SAP Cloud SDK for AI**: SAP AI CORE service integration

## Quick Start

### 1. Configure AI Core Service

Set up your AI Core service key as an environment variable or in a `.env` file within the `server/` directory:

```bash
cd server
echo "AICORE_SERVICE_KEY=your_service_key_here" > .env
```

### 2. Start the A2A Server

From the project root directory:

```bash
cd a2a-sample
mvn quarkus:dev
```

The server will start on `http://localhost:8080`

### 3. Run the Client (Optional)

In a separate terminal, from the project root directory:

```bash
cd client
mvn exec:java -Dexec.mainClass="org.example.a2a.client.Application"
```

The client will connect to the server and send a sample message, demonstrating the A2A communication flow.

The server is expected to respond with a `Task` and rhe client by default asks for non-streaming response.

To see streaming response, set `IS_STREAMING` to `true` in the client `Application` class.

### Expected Behavior

When running the client, you should see log output showing the client request and agent's response to the sample weather query, demonstrating successful A2A protocol communication between client and server. Currently, the HTTP+JSON/REST transport is used for communication.

## Setting up a Simple A2A Server

Below is an example of setting up a minimal A2A server with a custom Agent Card and Agent Executor.

### 1. Define the Agent Card

The Agent Card is a JSON representation of an AI agent's identity, capabilities and other metadata.
The Agent Card is used by other discover the most relevant agents to interact with for solving specific tasks.

Importantly, it is **purely a placard** and does not have implications on the agent's implementation. Agent skills for example may correspond to one or many tools the agent posses, abstracting them away as skills. To take it further, a routing agent may list _hotel booking_, _flight booking_ and _car rental_ as skills, but delegate the actual work to specialized agents.

```java

@ApplicationScoped
public class AgentConfiguration {
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

```

### 2. Implement the Agent Executor

An Agent Executor is responsible for handling incoming A2A messages and executing the corresponding logic. Below is a simple stateless implementation that responds to weather queries by integrating with the SAP AI SDK.

```java

@ApplicationScoped
public class MinimalAgentExecutor implements AgentExecutor {
    @Produces
    public AgentExecutor agentExecutorStateless() {
        return new MinimalAgentExecutor();
    }
    
    @Override
    public void execute(RequestContext request, EventQueue eventQueue) throws JSONRPCError {
        UserMessage userMessage = toOrchestrationUserMessage(request.getMessage());
        
        OrchestrationPrompt prompt = new OrchestrationPrompt(userMessage);
        OrchestrationModuleConfig config =
            new OrchestrationModuleConfig().withLlmConfig(OrchestrationAiModel.GPT_4O);
        OrchestrationChatResponse response = OrchestrationAgent.chat(prompt, config);
        
        eventQueue.enqueueEvent(toA2AMessage(request, response));
    }
    
    @Override
    public void cancel(RequestContext context, EventQueue eventQueue)
        throws JSONRPCError, UnsupportedOperationException {
        throw new UnsupportedOperationException("Cancellation not implemented yet");
    }
}

```

## Resources

- [A2A Protocol Documentation](https://a2a-protocol.org/latest/)
- [A2A Java SDK on GitHub](https://github.com/a2aproject/a2a-java/tree/main)
- [A2A Samples Repository](https://github.com/a2aproject/a2a-samples)
- [Demystifying Tasks vs Messages](https://discuss.google.dev/t/a2a-protocol-demystifying-tasks-vs-messages/255879)
