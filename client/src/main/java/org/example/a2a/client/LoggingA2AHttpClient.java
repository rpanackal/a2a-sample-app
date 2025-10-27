package org.example.a2a.client;

import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.http.A2AHttpResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * A logging wrapper for the A2AHttpClient interface.
 *
 * <p>This class is unnecessary for regular usage of client and is only provided for debugging
 * purposes.
 */
@Slf4j
public class LoggingA2AHttpClient implements A2AHttpClient {
  private final A2AHttpClient delegate;

  public LoggingA2AHttpClient(A2AHttpClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public GetBuilder createGet() {
    return new LoggingGetBuilder(delegate.createGet());
  }

  @Override
  public PostBuilder createPost() {
    return new LoggingPostBuilder(delegate.createPost());
  }

  @Override
  public DeleteBuilder createDelete() {
    return new LoggingDeleteBuilder(delegate.createDelete());
  }

  // Shared logging utilities
  private static void logRequest(String method, String url, String body) {
    log.info("{} {}", method, url);
    if (body != null && !body.trim().isEmpty()) {
      log.info("Request body: {}", body);
    }
  }

  private static A2AHttpResponse logResponse(A2AHttpResponse response) {
    log.info("Response {} {}", response.status(), response.status() >= 400 ? "ERROR" : "OK");
    if (response.body() != null && !response.body().trim().isEmpty()) {
      log.info("Response body: {}", response.body());
    }
    return response;
  }

  private static class LoggingGetBuilder implements GetBuilder {
    private final GetBuilder delegate;
    private String url;

    public LoggingGetBuilder(GetBuilder delegate) {
      this.delegate = delegate;
    }

    @Override
    public GetBuilder url(String url) {
      this.url = url;
      delegate.url(url);
      return this;
    }

    @Override
    public GetBuilder addHeader(String name, String value) {
      delegate.addHeader(name, value);
      return this;
    }

    @Override
    public GetBuilder addHeaders(Map<String, String> headers) {
      delegate.addHeaders(headers);
      return this;
    }

    @Override
    public A2AHttpResponse get() throws IOException, InterruptedException {
      logRequest("GET", url, null);
      return logResponse(delegate.get());
    }

    @Override
    public CompletableFuture<Void> getAsyncSSE(
        Consumer<String> messageConsumer,
        Consumer<Throwable> errorConsumer,
        Runnable completeRunnable)
        throws IOException, InterruptedException {
      logRequest("GET SSE", url, null);
      return delegate.getAsyncSSE(messageConsumer, errorConsumer, completeRunnable);
    }
  }

  private static class LoggingPostBuilder implements PostBuilder {
    private final PostBuilder delegate;
    private String url;
    private String body;

    public LoggingPostBuilder(PostBuilder delegate) {
      this.delegate = delegate;
    }

    @Override
    public PostBuilder url(String url) {
      this.url = url;
      delegate.url(url);
      return this;
    }

    @Override
    public PostBuilder addHeader(String name, String value) {
      delegate.addHeader(name, value);
      return this;
    }

    @Override
    public PostBuilder addHeaders(Map<String, String> headers) {
      delegate.addHeaders(headers);
      return this;
    }

    @Override
    public PostBuilder body(String body) {
      this.body = body;
      delegate.body(body);
      return this;
    }

    @Override
    public A2AHttpResponse post() throws IOException, InterruptedException {
      logRequest("POST", url, body);
      return logResponse(delegate.post());
    }

    @Override
    public CompletableFuture<Void> postAsyncSSE(
        Consumer<String> messageConsumer,
        Consumer<Throwable> errorConsumer,
        Runnable completeRunnable)
        throws IOException, InterruptedException {
      logRequest("POST SSE", url, body);
      return delegate.postAsyncSSE(messageConsumer, errorConsumer, completeRunnable);
    }
  }

  private static class LoggingDeleteBuilder implements DeleteBuilder {
    private final DeleteBuilder delegate;
    private String url;

    public LoggingDeleteBuilder(DeleteBuilder delegate) {
      this.delegate = delegate;
    }

    @Override
    public DeleteBuilder url(String url) {
      this.url = url;
      delegate.url(url);
      return this;
    }

    @Override
    public DeleteBuilder addHeader(String name, String value) {
      delegate.addHeader(name, value);
      return this;
    }

    @Override
    public DeleteBuilder addHeaders(Map<String, String> headers) {
      delegate.addHeaders(headers);
      return this;
    }

    @Override
    public A2AHttpResponse delete() throws IOException, InterruptedException {
      logRequest("DELETE", url, null);
      return logResponse(delegate.delete());
    }
  }
}
