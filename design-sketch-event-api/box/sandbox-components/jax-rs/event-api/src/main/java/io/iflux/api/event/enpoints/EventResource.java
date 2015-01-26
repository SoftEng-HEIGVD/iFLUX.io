package io.iflux.api.event.enpoints;

import io.iflux.api.event.dto.Event;
import io.iflux.api.event.util.InMemoryManager;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Olivier Liechti
 */
@Stateless
@Path("/events")
public class EventResource implements EventResourceLocal {
  
  @EJB
  private InMemoryManager inMemoryManager;
  
  @Override
  @POST
  @Consumes("application/json")
  public Response postEvent(List<Event> events) {
    for (Event event : events) {
      Date ts = event.getTimestamp();
      String hello = (String)event.get("hello");
      Double temperature = (Double)event.get("temperature");
      List<String> propertyNames = event.getPropertyNames();
      inMemoryManager.push(event);
    }
    return Response.accepted().build();
  }
  
}
