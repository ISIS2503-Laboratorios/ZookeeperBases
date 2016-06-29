/*
 * The MIT License
 *
 * Copyright 2016 Universidad de los Andes - Departamento de Ingeniería de Sistemas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.edu.uniandes.isis2503.zk.competition.models;

import co.edu.uniandes.isis2503.zk.competition.coordination.MicroserviceDTO;
import co.edu.uniandes.isis2503.zk.competition.coordination.MicroserviceRegistrar;
import co.edu.uniandes.isis2503.zk.competition.models.dtos.CompetitorDTO;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.jboss.logging.Logger;

/**
 *
 * @author Luis Felipe Mendivelso Osorio <lf.mendivelso10@uniandes.edu.co>
 */
public class CompetitorLogic {

    public static final String MICROSERVICE_NAME = "competitors";

    public static CompetitorDTO addCompetitor(CompetitorDTO competitor) {
        try {
            MicroserviceDTO competitorMicroservice = getMicroserviceEndPoint();
            if(competitorMicroservice==null){
                throw new RuntimeException("No nodes for Competitor Microservices");
            }
            Client client = Client.create();
            WebResource webResource = client.resource(competitorMicroservice.getPath());
            competitor = webResource.accept("application/json").post(CompetitorDTO.class,competitor);
            return competitor;
        } catch (Exception e) {
            Logger.getLogger(CompetitionLogic.class.getName()).log(Logger.Level.FATAL, e);
            return null;
        }
    }
    
    private static MicroserviceDTO getMicroserviceEndPoint(){
        try {
            String URL = MicroserviceRegistrar.DIRECTORY_URL + "/microservice=" + MICROSERVICE_NAME;
            Client client = Client.create();
            WebResource webResource = client.resource(URL);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }
            return response.getEntity(MicroserviceDTO.class);
        } catch (Exception e) {
            Logger.getLogger(CompetitionLogic.class.getName()).log(Logger.Level.FATAL, e);
            return null;
        }
    }

}
