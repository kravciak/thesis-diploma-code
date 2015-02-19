package cz.vse.sample;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 *
 * @author Martin Kravec
 */
public class TweetGeneratorSimulator {
        
    private final CSVInputAdapter ia;

    public TweetGeneratorSimulator(EPServiceProvider epService) {
        CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource("tweet_generator_sim.csv"), "TweetEvent");
        ia = new CSVInputAdapter(epService, spec);
    }

    public void start() {
        ia.start();
    }

    public void stop() {
        ia.stop();
    }

}
