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
package co.edu.uniandes.isis2503.zk.competitor.coordination;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Luis Felipe Mendivelso Osorio <lf.mendivelso10@uniandes.edu.co>
 */
public class MicroserviceRegistrar implements Job {

    private static final String DIRECTORY_SERVER = "localhost";
    private static final Integer DIRECTORY_PORT = 8080;
    private static final String DIRECTORY_PATH = "/directory";
    private static final String DIRECTORY_URL = "http://" + DIRECTORY_SERVER + ":" + DIRECTORY_PORT + DIRECTORY_PATH;

    private static final String MICROSERVICE_APPNAME = "Concourse";
    private static final String MICROSERVICE_ID = "201420653";
    private static final String MICROSERVICE_NAME = "competitors";
    private static final String MICROSERVICE_TYPE = MicroserviceDTO.TYPE_STATIC;
    private static final String MICROSERVICE_SERVER = "localhost";
    private static final Integer MICROSERVICE_PORT = 8081;
    private static final String MICROSERVICE_PATH = "/competitors";
    private static final String MICROSERVICE_STATUS = MicroserviceDTO.STATUS_UP;
    
    public static final Integer HEARTBEAT_FRECUENCY_SECONDS = 30;

    public static final void registerMicroservice() {
        try {
            MicroserviceDTO microservicio = new MicroserviceDTO(MICROSERVICE_APPNAME, MICROSERVICE_NAME, MICROSERVICE_ID, MICROSERVICE_SERVER, MICROSERVICE_PORT, MICROSERVICE_PATH, MICROSERVICE_TYPE, MICROSERVICE_STATUS);
            Client client = Client.create();
            WebResource target = client.resource(DIRECTORY_URL);
            target.post(MicroserviceDTO.class, microservicio);
            client.destroy();
        } catch (Exception ex) {
            Logger.getLogger(MicroserviceRegistrar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        registerMicroservice();
        System.out.println("Heartbeat sended: "+new Date().toString());
    }

    public static final void startHeartbeat() {

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            // and start it off
            scheduler.start();
            // define the job and tie it to our MyJob class
            JobDetail job = newJob(MicroserviceRegistrar.class)
                    .withIdentity("microserviceheartbeat", "heartbeats")
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("microserviceHeartbeatTrigger", "DefaultTrigger")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(5)
                            .repeatForever())
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(MicroserviceRegistrar.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
