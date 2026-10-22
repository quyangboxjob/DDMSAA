package com.example.ddmsaa.infrastructure.scan;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ClamAV の clamd に TCP（INSTREAM）で依頼する {@link VirusScanService} 実装。
 *
 * <p>追加ライブラリを使わずソケットで実装する。{@code app.scan.type=clamd} のとき有効。
 * 接続・送受信の失敗、タイムアウト、clamd の ERROR 応答は
 * {@link VirusScanUnavailableException} とする。</p>
 */
@Component
@ConditionalOnProperty(name = "app.scan.type", havingValue = "clamd")
public class ClamdVirusScanService implements VirusScanService {

    private static final byte[] COMMAND = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);

    private static final int CHUNK_SIZE = 8192;

    private static final int MAX_REPLY_BYTES = 4096;

    private final String host;

    private final int port;

    private final int timeoutSeconds;

    /**
     * 接続先とタイムアウトを指定して生成する。
     *
     * @param host clamd のホスト
     * @param port clamd の TCP ポート
     * @param timeoutSeconds 接続および 1 回の読み書きの待ち時間の上限（秒）
     */
    public ClamdVirusScanService(
            @Value("${app.scan.clamd.host:localhost}") String host,
            @Value("${app.scan.clamd.port:3310}") int port,
            @Value("${app.scan.clamd.timeout-seconds:30}") int timeoutSeconds) {
        this.host = host;
        this.port = port;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * {@inheritDoc}
     *
     * <p>内容は EOF まで読み取って送る（contentLength は参照しない）。</p>
     *
     * @throws IllegalArgumentException content が null の場合
     */
    @Override
    public ScanResult scan(InputStream content, long contentLength) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        int timeoutMillis = Math.toIntExact(timeoutSeconds * 1000L);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            sendStream(socket.getOutputStream(), content);
            return parse(readReply(socket.getInputStream()));
        } catch (IOException e) {
            throw new VirusScanUnavailableException(
                    "clamd is unavailable: " + host + ":" + port, e);
        }
    }

    private void sendStream(OutputStream out, InputStream content) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.write(COMMAND);
        byte[] buffer = new byte[CHUNK_SIZE];
        int read;
        while ((read = content.read(buffer)) != -1) {
            if (read == 0) {
                continue;
            }
            data.writeInt(read);
            data.write(buffer, 0, read);
        }
        data.writeInt(0);
        data.flush();
    }

    private String readReply(InputStream in) throws IOException {
        ByteArrayOutputStream reply = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1 && b != 0 && reply.size() < MAX_REPLY_BYTES) {
            reply.write(b);
        }
        return reply.toString(StandardCharsets.UTF_8).trim();
    }

    private ScanResult parse(String reply) {
        if (reply.endsWith("OK")) {
            return ScanResult.SAFE;
        }
        if (reply.endsWith("FOUND")) {
            return ScanResult.REJECTED;
        }
        throw new VirusScanUnavailableException("Unexpected clamd reply: " + reply);
    }
}
