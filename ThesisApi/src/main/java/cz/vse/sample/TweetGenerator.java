package cz.vse.sample;

import com.espertech.esper.client.EPServiceProvider;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Martin Kravec
 */
public class TweetGenerator {

    private EPServiceProvider epService;
    private final TwitterStream stream;

    private final StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            TweetEvent event = new TweetEvent(status.getUser().getName(), status.getText());
            tick(event);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
        }

        @Override
        public void onStallWarning(StallWarning warning) {
        }

        @Override
        public void onException(Exception excptn) {
        }
    };

    public TweetGenerator(EPServiceProvider epService) {
        this.epService = epService;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("mampm0Yhxqj6QXymcRfUTnPVB")
                .setOAuthConsumerSecret("gV486UTibCD6kC5qH7KTRYPvkY5y5mjVB8OcoSsUkCw3SK2Ix1")
                .setOAuthAccessToken("307580546-rre2kr15r7BQEMgLWfaptVRKOcnjqlZ7cNJ63uEC")
                .setOAuthAccessTokenSecret("wkONOZXTdRomnkYka9xNz0Lt24hlaDwrRuHfugHs2qWqN");

        stream = new TwitterStreamFactory(cb.build()).getInstance();
        stream.addListener(listener);
    }

    public void start() {
        stream.sample();
    }

    public void stop() {
        stream.cleanUp();
        stream.shutdown();
    }

    public void tick(Object event) {
        epService.getEPRuntime().sendEvent(event);
    }

}
