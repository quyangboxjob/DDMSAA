package com.example.ddmsaa.auth.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * テスト用の {@link org.springframework.mail.javamail.JavaMailSender}。実際には送信せず、
 * 送ろうとしたメールを記録する。{@link #failNext(RuntimeException)} で送信失敗も再現できる。
 */
public class RecordingMailSender extends JavaMailSenderImpl {

    private final List<MimeMessage> mimeMessages = new CopyOnWriteArrayList<>();

    private final List<SimpleMailMessage> simpleMessages = new CopyOnWriteArrayList<>();

    private volatile RuntimeException nextFailure;

    /** {@inheritDoc} */
    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        throwIfFailureScheduled();
        mimeMessages.add(mimeMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage message : mimeMessages) {
            send(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        throwIfFailureScheduled();
        simpleMessages.add(simpleMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        for (SimpleMailMessage message : simpleMessages) {
            send(message);
        }
    }

    /**
     * 次の 1 回の送信を失敗させる。
     *
     * @param failure 投げる例外（通常は {@link org.springframework.mail.MailSendException}）
     */
    public void failNext(RuntimeException failure) {
        this.nextFailure = failure;
    }

    /**
     * 送信を試みたメールの件数（MIME と単純メールの合計）を返す。
     *
     * @return 件数
     */
    public int sentCount() {
        return mimeMessages.size() + simpleMessages.size();
    }

    /**
     * 記録した MIME メールを返す。
     *
     * @return 送信順のメール（コピー）
     */
    public List<MimeMessage> mimeMessages() {
        return List.copyOf(mimeMessages);
    }

    /**
     * 記録した単純メールを返す。
     *
     * @return 送信順のメール（コピー）
     */
    public List<SimpleMailMessage> simpleMessages() {
        return List.copyOf(simpleMessages);
    }

    /**
     * 最後に記録した MIME メールの本文を文字列で返す。
     *
     * @return 本文
     * @throws IllegalStateException MIME メールがない、または本文を読めない場合
     */
    public String lastMimeText() {
        if (mimeMessages.isEmpty()) {
            throw new IllegalStateException("no mime message recorded");
        }
        try {
            return String.valueOf(mimeMessages.get(mimeMessages.size() - 1).getContent());
        } catch (java.io.IOException | MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 最後に記録した単純メールの本文を返す（auth-service の Magic Link メールはこの形式）。
     *
     * @return 本文
     * @throws IllegalStateException 単純メールがない場合
     */
    public String lastSimpleText() {
        if (simpleMessages.isEmpty()) {
            throw new IllegalStateException("no simple message recorded");
        }
        return simpleMessages.get(simpleMessages.size() - 1).getText();
    }

    /** 記録と失敗予約を消す。 */
    public void clear() {
        mimeMessages.clear();
        simpleMessages.clear();
        nextFailure = null;
    }

    private void throwIfFailureScheduled() {
        RuntimeException failure = nextFailure;
        if (failure != null) {
            nextFailure = null;
            throw failure;
        }
    }
}
