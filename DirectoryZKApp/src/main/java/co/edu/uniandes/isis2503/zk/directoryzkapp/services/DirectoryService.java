/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.uniandes.isis2503.zk.directoryzkapp.services;

import co.edu.uniandes.isis2503.zk.directoryzkapp.models.MicroserviceReport;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Luis Felipe Mendivelso Osorio <lf.mendivelso10@uniandes.edu.co>
 */
@Path("/directory")
public class DirectoryService {
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerMicroservice(MicroserviceReport microservicio){
         try {
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(microservicio.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("We found errors in your query, please contact the Web Admin.").build();
        }   
    }
    
}
