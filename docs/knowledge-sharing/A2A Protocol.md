# A2A Protocol

A defined standard of communication between agents.

Typically, agent are backed by LLMs but not necessarily so. The large portion of exchange is expected to be in natural language.

### A2A vs MCP:

MCP is a standard that defines communication from an agent with its tools (APIs, data source, functions).

Typically, tools are not backed by LLMs and hence, unable to parse unstructured requests or even generate them.

## Components of A2A

* A2A client (agent client)
* A2A server (remote agent)
* Agent card
    - Purely descriptive metadata about an agent. This is how an agent advertises its capabilities to other agents - like a business card.
    - Includes: name, description, skills, input/output formats, endpoints, supported transports, authentication requirement etc.
* Message
    - The simplest piece/enitty that can be exchanged between agents.
    - A combination of Parts
    - Has roles: "user" or "agent".
* Parts
    - A piece of data within a Message or Artifact eg: TextPart, FilePart, DataPart.
* Task
    - A single unit of work spawned for an incoming request (spawned on A2A server).
    - Suited for multi-turn conversations.
    - Has defined lifecycle: submitted, working, input-required, completed, failed.
    - Think of them as a way to bind multiple messages together that accomplish a single goal.
* Artifact
    - A tangible "product" of a task like a document, image, structure data etc.
    - Just semantically richer than a Message. Messages are transient while Artifacts are persisted.
    - Also associate to particular Tasks.

## A2A Flow

<img src="https://a2aprotocol.ai/flow.jpg" alt="A2A Protocol Flow Diagram" width="600">

*Source: [A2A Protocol](https://a2aprotocol.ai/)*

## Resources

- [IBM: What is A2A protocol (Agent2Agent)?](https://www.ibm.com/think/topics/agent2agent-protocol)
- [Core Concepts of A2A Protocol](https://a2a-protocol.org/latest/topics/key-concepts/)