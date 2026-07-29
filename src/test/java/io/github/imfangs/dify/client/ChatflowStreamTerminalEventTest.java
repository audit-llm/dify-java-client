package io.github.imfangs.dify.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.imfangs.dify.client.callback.ChatflowStreamCallback;
import io.github.imfangs.dify.client.event.MessageEndEvent;
import io.github.imfangs.dify.client.impl.DefaultDifyClient;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 Chatflow 的工作流完成事件不会截断后续 message_end 事件。
 */
class ChatflowStreamTerminalEventTest {

    @Test
    void shouldDeliverMessageEndAfterWorkflowFinished() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat-messages", this::writeChatflowEvents);
        server.start();

        try {
            CountDownLatch messageEndReceived = new CountDownLatch(1);
            DefaultDifyClient client = new DefaultDifyClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(), "test-key");

            client.sendChatMessageStream(ChatMessage.builder().query("测试").user("tester").build(),
                    new ChatflowStreamCallback() {
                        @Override
                        public void onMessageEnd(MessageEndEvent event) {
                            messageEndReceived.countDown();
                        }
                    });

            assertTrue(messageEndReceived.await(3, TimeUnit.SECONDS),
                    "workflow_finished 后仍应分发 message_end");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldNotifyCompletionWhenChatflowEndsWithoutMessageEnd() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat-messages", exchange -> writeEvents(exchange,
                "data: {\"event\":\"workflow_finished\",\"data\":{\"id\":\"workflow-1\",\"status\":\"succeeded\"}}\n\n"));
        server.start();

        try {
            CountDownLatch streamCompleted = new CountDownLatch(1);
            DefaultDifyClient client = new DefaultDifyClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(), "test-key");

            client.sendChatMessageStream(ChatMessage.builder().query("测试").user("tester").build(),
                    new ChatflowStreamCallback() {
                        @Override
                        public void onStreamComplete() {
                            streamCompleted.countDown();
                        }
                    });

            assertTrue(streamCompleted.await(3, TimeUnit.SECONDS),
                    "未收到 message_end 时仍应通知流已结束");
        } finally {
            server.stop(0);
        }
    }

    private void writeChatflowEvents(HttpExchange exchange) throws IOException {
        writeEvents(exchange,
                "data: {\"event\":\"workflow_finished\",\"data\":{\"id\":\"workflow-1\",\"status\":\"succeeded\"}}\n\n"
                        + "data: {\"event\":\"message_end\",\"message_id\":\"message-1\",\"conversation_id\":\"conversation-1\",\"metadata\":{}}\n\n");
    }

    private void writeEvents(HttpExchange exchange, String events) throws IOException {
        byte[] body = events.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
