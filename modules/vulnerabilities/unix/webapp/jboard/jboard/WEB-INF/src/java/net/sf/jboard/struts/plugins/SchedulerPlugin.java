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
 * Plugin lanc� au d�marrage de l'applicati�on par la servlet de Struts.
 * ce plugin est d�di� au lancement des jobs du scheduler.
 * @author cga.
 *
 */
public class SchedulerPlugin implements PlugIn {

    //valeur par d�faut
    //=> d�clenchement tous les jours tous les mois � deux heures du matin
    private String cronExpression ="0 0 2 * * ?";

    /**
     * m�thode appel�e lors du lancement de l'application.
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1)
        throws ServletException {
        
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            CronTrigger trigger;
            try{
            
                Scheduler sched = schedFact.getScheduler();
    
                sched.start();
                
                //d�finition du job � effectuer
                JobDetail jobDetail = new JobDetail("SampleJob",
                                                      Scheduler.DEFAULT_GROUP,
                                                      SampleJob.class);
                // d�finition du trigger qui prend en charge le job
                trigger =             new CronTrigger(
                                            "SampleTrigger",
                                            Scheduler.DEFAULT_GROUP,
                                            "SampleJob",
                                            Scheduler.DEFAULT_GROUP,
                                            cronExpression,
                                            TimeZone.getTimeZone("Europe/Paris"));
                                            
                // les timeZone peuvent aussi �tre exprim�s avec un code � 3 lettres
                // pour "Europe/Paris" c'est ECT
                //on aurait pu aussi d�finir le temps en fontion de GMT � des heures
                                            
                //association au niveau du scheduler du trigger et du job.                                            
                sched.scheduleJob(jobDetail, trigger);
                
                                                            
            }catch(SchedulerException schedEx){
            System.out.println("probl�me d'initialisation du scheduler "+schedEx.getMessage());      
                    
            } catch (ParseException e) {
                  System.out.println("probl�me de parsing de la cron expression "+e.getMessage());
                
            }
        
    }
    /**
     * m�thode appel�e lors de l'arr�t de l'application.
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