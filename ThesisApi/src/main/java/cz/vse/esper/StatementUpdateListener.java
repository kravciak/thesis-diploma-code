package cz.vse.esper;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.util.JSONRenderingOptions;
import cz.vse.dao.ResultsDAOImpl;
import cz.vse.model.StatementBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by Martin Kravec on 13. 12. 2014.
 */

@Component
public class StatementUpdateListener implements StatementAwareUpdateListener {

    @Autowired
    private ResultsDAOImpl resultsDAO;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {
        if (newEvents != null) {
            for(EventBean event : newEvents) {
                StatementBean sb = (StatementBean) statement.getUserObject();
                String json = new JSONRenderer(event.getEventType(), new JSONRenderingOptions()).render(event);
//                String xml = new XMLRenderer(event.getEventType(), new XMLRenderingOptions()).render("event", event);

                resultsDAO.save(sb, json);
            }
        }
    }
}
