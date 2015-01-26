package io.iflux.api.event.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Event DTO defines the data structure of the payload sent by clients to
 * the /events/ endpoint. This data structure is dynamic, in the sense that the
 * list of properties attached to an event is not fixed.
 * 
 * @author Olivier Liechti
 */
public class Event {

  public class EventProperties {
    
    private final Map<String, Object> dynamicProperties = new HashMap<>();
    
    @JsonAnySetter
    public void addProperty(String name, Object value) {
      dynamicProperties.put(name, value);
    }
    
    @JsonAnyGetter
    public Map<String,Object> any() {
        return dynamicProperties;
    }
    
    public Object get(String name) {
      return dynamicProperties.get(name);
    }

  }

  private Date timestamp;
  
  private String source;
  
  private String type;
  
  @JsonProperty("properties")
  private EventProperties properties = new EventProperties();
  

  public Date getTimestamp() {
    return timestamp;
  }

  public String getSource() {
    return source;
  }

  public String getType() {
    return type;
  }
  
  @JsonIgnore
  public List<String> getPropertyNames() {
    return new ArrayList(properties.dynamicProperties.keySet());
  }
    
  public Object get(String name) {
    return properties.get(name);
  }

}
