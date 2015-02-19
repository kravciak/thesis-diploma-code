package cz.vse.sample;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AdapterInputSource;
import com.espertech.esperio.csv.CSVInputAdapter;
import com.espertech.esperio.csv.CSVInputAdapterSpec;

/**
 *
 * @author Martin Kravec
 */
public class StockGeneratorSimulator {

    private final CSVInputAdapter ia;

    public StockGeneratorSimulator(EPServiceProvider epService) {
        CSVInputAdapterSpec spec = new CSVInputAdapterSpec(new AdapterInputSource("stock_generator_sim.csv"), "StockEvent");
//        spec.setEventsPerSec(100);
//        spec.setLooping(true);
        ia = new CSVInputAdapter(epService, spec);
    }

    public void start() {
        ia.start();
    }

    public void stop() {
        ia.stop();
    }

}
