package net.sf.jboard.struts.plugins;


import java.text.ParseException;
import java.util.TimeZone;

import javax.servlet.ServletException;

import net.sf.jboard.scheduler.jobs.SampleJob;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;



/**
 * Plugin lancé au démarrage de l'applicatiàon par la servlet de Struts.
 * ce plugin est dédié au lancement des jobs du scheduler.
 * @author cga.
 *
 */
public class SchedulerPlugin implements PlugIn {

    //valeur par défaut
    //=> déclenchement tous les jours tous les mois à deux heures du matin
    private String cronExpression ="0 0 2 * * ?";

    /**
     * méthode appelée lors du lancement de l'application.
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1)
        throws ServletException {
        
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            CronTrigger trigger;
            try{
            
                Scheduler sched = schedFact.getScheduler();
    
                sched.start();
                
                //définition du job à effectuer
                JobDetail jobDetail = new JobDetail("SampleJob",
                                                      Scheduler.DEFAULT_GROUP,
                                                      SampleJob.class);
                // définition du trigger qui prend en charge le job
                trigger =             new CronTrigger(
                                            "SampleTrigger",
                                            Scheduler.DEFAULT_GROUP,
                                            "SampleJob",
                                            Scheduler.DEFAULT_GROUP,
                                            cronExpression,
                                            TimeZone.getTimeZone("Europe/Paris"));
                                            
                // les timeZone peuvent aussi être exprimés avec un code à 3 lettres
                // pour "Europe/Paris" c'est ECT
                //on aurait pu aussi définir le temps en fontion de GMT ± des heures
                                            
                //association au niveau du scheduler du trigger et du job.                                            
                sched.scheduleJob(jobDetail, trigger);
                
                                                            
            }catch(SchedulerException schedEx){
            System.out.println("problème d'initialisation du scheduler "+schedEx.getMessage());      
                    
            } catch (ParseException e) {
                  System.out.println("problème de parsing de la cron expression "+e.getMessage());
                
            }
        
    }
    /**
     * méthode appelée lors de l'arrêt de l'application.
     * @see org.apache.struts.action.PlugIn#destroy()
     */
    public void destroy() {
       

    }
    /**
     * @return
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * @param string
     */
    public void setCronExpression(String string) {
        cronExpression = string;
    }

}