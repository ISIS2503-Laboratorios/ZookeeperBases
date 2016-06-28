/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.uniandes.isis2503.zk.directoryzkapp.logic;

import co.edu.uniandes.isis2503.zk.directoryzkapp.models.Microservice;
import co.edu.uniandes.isis2503.zk.directoryzkapp.models.MicroserviceReport;
import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.curator.x.discovery.strategies.StickyStrategy;

/**
 *
 * @author Luis Felipe Mendivelso Osorio <lf.mendivelso10@uniandes.edu.co>
 */
public class DirectoryLogic implements Closeable {

    public static final String APP_PATH = "/Contects";

    private ServiceDiscovery<Microservice> appServiceDiscovery;
    private List<MicroserviceConnection> connectedMicroservices;

    public DirectoryLogic() {
        try {
            // Create a JSON object with the Directory Information, which is serialized to be stored in Zookeeper. 
            // In other words, this is the registration of the App in Zookeeper.
            JsonInstanceSerializer<Microservice> appInfoSerialization = new JsonInstanceSerializer(Microservice.class);

            // In this part, create a ServiceDiscovery with de Application Root Path and the Directory Information in the JSON.
            // This ServiceDiscovery include a ZookeeperConnection.
            appServiceDiscovery = ServiceDiscoveryBuilder
                    .builder(Microservice.class)
                    .client(ZookeeperConnection.getZooKeeperClient())
                    .basePath(APP_PATH)
                    .serializer(appInfoSerialization)
                    .build();

            // Remember that the service is connected with the Zookeeper Server, then the appServiceDiscovery has to
            // start a synchronous connection.
            appServiceDiscovery.start();
        } catch (Exception ex) {
            Logger.getLogger(DirectoryLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean addNewMicroservice(MicroserviceReport microservice) {

        UriSpec uriSpec = new UriSpec("http://" + microservice.getServer() + ":" + microservice.getPort() + microservice.getPath());
        ServiceInstance<Microservice> microserviceInstance = new ServiceInstance(
                microservice.getMicroserviceName(),
                microservice.getMicroserviceId(),
                microservice.getServer(),
                microservice.getPort(),
                Integer.SIZE,
                microservice.toString(),
                new Date().getTime(),
                ServiceType.valueOf(microservice.getType()),
                uriSpec);
        try {
            MicroserviceConnection microserviceConnection = new MicroserviceConnection(
                    microserviceInstance,
                    microservice.getPath(),
                    microservice.getMicroserviceName(),
                    microservice.getMicroserviceId()
            );
            microserviceConnection.start();
            connectedMicroservices.add(microserviceConnection);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(DirectoryLogic.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean removeMicroservice(String name, String id) {
        final String nameS = name;

        MicroserviceConnection microservice = null;
        for(MicroserviceConnection connection : connectedMicroservices){
            if(connection.getMicroserviceInstance().getId().equals(name)){
                microservice = connection;
                break;
            }
        }

        if (microservice == null) {
            System.err.println("No servers found named: " + nameS);
            return false;
        }
        connectedMicroservices.remove(microservice);
        CloseableUtils.closeQuietly(microservice);
        System.out.println("Removed a random instance of: " + nameS);
        return true;
    }
    
    public void getMicroservice(String microserviceName, String id) {
        
        Map<String,ServiceProvider<Microservice>> zookeeperRecords;
        zookeeperRecords = (Map<String, ServiceProvider<Microservice>>) appServiceDiscovery
                .serviceProviderBuilder()
                .serviceName(microserviceName)
                .providerStrategy(new RandomStrategy<Microservice>()).build();
      
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
