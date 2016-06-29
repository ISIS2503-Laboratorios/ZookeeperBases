/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.uniandes.isis2503.zk.directoryzkapp.logic;

import static co.edu.uniandes.isis2503.zk.directoryzkapp.logic.DirectoryLogic.APP_PATH;
import co.edu.uniandes.isis2503.zk.directoryzkapp.models.Microservice;
import co.edu.uniandes.isis2503.zk.directoryzkapp.services.DirectoryService;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.curator.x.discovery.ServiceInstance;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
public class DirectoryHealthCheck implements Job {
    
    public static final Integer HEALTHCHECK_FRECUENCY_SECONDS = 90;
    public static final Long HEALTHCHECK_LIMIT=(long)90000;
    
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            System.out.println("Start Health Check");
            DirectoryLogic logic = DirectoryService.getDirectoryLogic();
            Collection<String> microservicesNames = logic.getAllNames();
            if(!microservicesNames.isEmpty()){
                Long referenceTime = new Date().getTime();
                for(String microserviceName : microservicesNames){
                    Collection<ServiceInstance<Microservice>> instances = logic.getAllMicroservicesInstances(microserviceName);
                    if(!instances.isEmpty()){
                        for(ServiceInstance<Microservice> instance : instances){
                            Long difference = referenceTime - instance.getRegistrationTimeUTC();
                            System.out.println("Microservice: "+microserviceName+"Instance: "+instance.getName()+" Diference: "+difference);
                            if(difference > HEALTHCHECK_LIMIT && !instance.getName().equals("root")){
                                ZookeeperConnection.getZooKeeperClient().delete().forPath(APP_PATH+"/"+microserviceName+"/"+instance.getId());
                                System.out.println("Microservice Instance is Down: "+microserviceName+"("+instance.getId()+")");
                            }
                        }
                    }
                }
            }
            System.out.println("Health Check Done");
        } catch (Exception ex) {
            Logger.getLogger(DirectoryHealthCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static final void startHealthCheckJob(){
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            // and start it off
            scheduler.start();
            // define the job and tie it to our MyJob class
            JobDetail job = newJob(DirectoryHealthCheck.class)
                    .withIdentity("microserviceheartbeat", "heartbeats")
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("microserviceHeartbeatTrigger", "DefaultTrigger")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(HEALTHCHECK_FRECUENCY_SECONDS)
                            .repeatForever())
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(DirectoryHealthCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
