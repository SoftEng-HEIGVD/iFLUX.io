package io.iflux.api.event.enpoints;

import io.iflux.api.event.dto.Event;
import java.util.List;
import javax.ejb.Local;
import javax.ws.rs.core.Response;

/**
 *
 * @author Olivier Liechti
 */
@Local
public interface EventResourceLocal {

  Response postEvent(List<Event> event);
  
}
