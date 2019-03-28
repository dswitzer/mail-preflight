package eu.kk42.mailpreflight.exception;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class MailPreflightException extends RuntimeException {
    public MailPreflightException() {
    }

    public MailPreflightException(String message) {
        super(message);
    }

    public MailPreflightException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailPreflightException(Throwable cause) {
        super(cause);
    }

    public MailPreflightException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
