package io.iflux.api.event.util;

import io.iflux.api.event.dto.Event;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Olivier Liechti
 */
@Singleton
@Path("/debug")
public class InMemoryManager {
  
  private List<Event> events = new ArrayList<>();
  
  public void push(Event event) {
    events.add(event);
  }
  
  @GET
  public Response getEvents() {
    return Response.ok(events).build();
  }

}
