package com.example.ddmsaa.infrastructure.scan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ClamdVirusScanService の INSTREAM 通信")
class ClamdVirusScanServiceTest {

    private ServerSocket server;

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) {
            server.close();
        }
    }

    /** 1 接続だけ受け付け、受信したペイロードを返しつつ reply を応答する偽 clamd。 */
    private CompletableFuture<byte[]> startFakeClamd(String reply, boolean readPayload)
            throws IOException {
        server = new ServerSocket(0);
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = server.accept()) {
                ByteArrayOutputStream received = new ByteArrayOutputStream();
                if (readPayload) {
                    InputStream in = socket.getInputStream();
                    DataInputStream data = new DataInputStream(in);
                    byte[] command = new byte[10];
                    data.readFully(command);
                    received.write(command);
                    int length;
                    while ((length = data.readInt()) != 0) {
                        byte[] chunk = new byte[length];
                        data.readFully(chunk);
                        received.write(chunk);
                    }
                }
                if (reply != null) {
                    OutputStream out = socket.getOutputStream();
                    out.write((reply + "\0").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                return received.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private ClamdVirusScanService service(int timeoutSeconds) {
        return new ClamdVirusScanService("127.0.0.1", server.getLocalPort(), timeoutSeconds);
    }

    @Test
    @DisplayName("OK 応答なら SAFE を返し、コマンドと内容を INSTREAM 形式で送る")
    void okReplyIsSafe() throws Exception {
        CompletableFuture<byte[]> received = startFakeClamd("stream: OK", true);
        byte[] payload = "hello clamd".getBytes(StandardCharsets.UTF_8);

        ScanResult result = service(5).scan(new ByteArrayInputStream(payload), payload.length);

        assertThat(result).isEqualTo(ScanResult.SAFE);
        byte[] expected = ("zINSTREAM\0hello clamd").getBytes(StandardCharsets.UTF_8);
        assertThat(received.get(5, TimeUnit.SECONDS)).isEqualTo(expected);
    }

    @Test
    @DisplayName("FOUND 応答なら REJECTED を返す")
    void foundReplyIsRejected() throws Exception {
        startFakeClamd("stream: Eicar-Test-Signature FOUND", true);

        ScanResult result = service(5).scan(new ByteArrayInputStream(new byte[] {1}), 1);

        assertThat(result).isEqualTo(ScanResult.REJECTED);
    }

    @Test
    @DisplayName("大きな内容は複数チャンクに分けて送る")
    void largeContentIsChunked() throws Exception {
        CompletableFuture<byte[]> received = startFakeClamd("stream: OK", true);
        byte[] payload = new byte[20000];
        java.util.Arrays.fill(payload, (byte) 7);

        service(5).scan(new ByteArrayInputStream(payload), payload.length);

        assertThat(received.get(5, TimeUnit.SECONDS)).hasSize(10 + payload.length);
    }

    @Test
    @DisplayName("ERROR 応答は VirusScanUnavailableException")
    void errorReplyIsUnavailable() throws Exception {
        startFakeClamd("INSTREAM size limit exceeded. ERROR", true);

        assertThatThrownBy(() -> service(5).scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isInstanceOf(VirusScanUnavailableException.class);
    }

    @Test
    @DisplayName("接続できなければ VirusScanUnavailableException")
    void connectFailureIsUnavailable() throws Exception {
        server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        ClamdVirusScanService service = new ClamdVirusScanService("127.0.0.1", port, 2);

        assertThatThrownBy(() -> service.scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isInstanceOf(VirusScanUnavailableException.class);
    }

    @Test
    @DisplayName("応答が無くタイムアウトしたら VirusScanUnavailableException")
    void readTimeoutIsUnavailable() throws Exception {
        startFakeClamd(null, true);

        assertThatThrownBy(() -> service(1).scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isInstanceOf(VirusScanUnavailableException.class);
    }

    @Test
    @DisplayName("content が null なら IllegalArgumentException")
    void nullContentIsRejected() throws Exception {
        server = new ServerSocket(0);

        assertThatThrownBy(() -> service(1).scan(null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
