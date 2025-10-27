# A2A Sample Application

The A2A (Agent-to-Agent) protocol defines a standard for communication between AI agents, allowing them to collaborate and exchange information efficiently. This project demonstrates a minimal implementation of an A2A client and server that integrates the A2A Java SDK with the SAP AI SDK for Java.
## Project Structure

- **server/**: Contains A2A server implementations of `AgentExecutor`, the transport-agnostic component that processes incoming A2A messages and generates appropriate responses:
    - A stateless (fire-and-forget) agent that responds to weather queries with `Message` events only
    - A stateful agent that responds with `Task` events only. See the [Resources](#resources) section for more details on Tasks vs Messages
- **client/**: Contains the A2A client implementation for connecting to and communicating with the server

This project is configured to use HTTP+JSON/REST transport instead of the default JSON-RPC transport.

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

The client will connect to the server and send a sample weather query, demonstrating the A2A communication flow.

### Expected Behavior

When you run the client, you should see log output showing the client request and the agent's response to the sample weather query. This demonstrates successful A2A protocol communication between client and server using HTTP+JSON/REST transport.

By default, the server responds with a `Task` and the client requests a non-streaming response. To see streaming responses, set `IS_STREAMING` to `true` in the client `Application` class.

## Setting up a Simple A2A Server

Below is an example of setting up a minimal A2A server with a custom Agent Card and Agent Executor. For more comprehensive examples, see the actual implementations in this repository.

### 1. Define the Agent Card

The Agent Card is a JSON representation of an AI agent's identity, capabilities, and other metadata. It is used by others to discover the most relevant agents to interact with for solving specific tasks.

The Agent Card is **purely a placard** and does not have implications on the agent's actual implementation. For example, agent skills may correspond to one or many tools the agent possesses, abstracting them away as high-level capabilities. A routing agent could list _hotel booking_, _flight booking_, and _car rental_ as skills while delegating the actual work to specialized agents.

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

### 2. Implement the `AgentExecutor`

The `AgentExecutor` is responsible for handling incoming A2A messages and executing the corresponding logic. Below is a simple stateless implementation that responds to weather queries by integrating with the SAP AI SDK.

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
