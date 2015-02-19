package cz.vse.sample;

/**
 *
 * @author Martin Kravec
 */

public class TweetEvent {

    private String username;
    private String message;

    public TweetEvent() {
    }

    public TweetEvent(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TweetEvent [" + username + ": " + message + "]";
    }
}
