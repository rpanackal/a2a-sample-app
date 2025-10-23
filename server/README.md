# A2A Server

A simple a2a server carrying multiple agent executor implementations.

## Quickstart

1. Set up the service key for AI CORE instance in a `.env` file in the directory `server/`.

```bash
echo "AICORE_SERVICE_KEY=your_api_key_here" > .env
```

2. Then, run the A2A server with:

```bash
mvn quarkus:dev
```

3. (Optionally) You can call the server but running the `A2AClient.java` or any other compatible client application.


