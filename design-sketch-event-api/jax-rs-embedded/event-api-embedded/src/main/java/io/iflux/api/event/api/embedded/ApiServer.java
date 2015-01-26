package io.iflux.api.event.api.embedded;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;

/**
 *
 * @author Olivier Liechti
 */
public class ApiServer {

  public void start() throws GlassFishException, IOException {
    GlassFishProperties glassfishProperties = new GlassFishProperties();
    glassfishProperties.setPort("http-listener", 9090);
    glassfishProperties.setPort("https-listener", 9191);
    GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
    glassfish.start();
    
    Logger.getLogger(ApiServer.class.getName()).log(Level.SEVERE, null, new File("./").toString());
    ScatteredArchive scattered = new ScatteredArchive("api", ScatteredArchive.Type.WAR, new File("./"));
  }

  public static void main(String[] args) {
    ApiServer server = new ApiServer();
    try {
      server.start();
    } catch (GlassFishException ex) {
      Logger.getLogger(ApiServer.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(ApiServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
