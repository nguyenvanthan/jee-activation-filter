package fr.javageek.jee.filter;

import java.text.ParseException;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * <b>Description : Classe qui gere ajoute la fonctionnalite CRON sur un filter.</b><br>
 * Par defaut, intervalle = 1 min
 * @author Christian NGUYEN VAN THAN alias @JavaGeekFr<br> 
 */
public abstract class CronFilter {
    
    private static final Logger LOG = Logger.getLogger(CronFilter.class);
    
    // constante qui servira de cle pour la map JobDataMap (sert au transfert de parametres)
    protected static final String REFERENCE = "jobDataInstanceReference";

    private int default_reload_period = 1; // rechargement automatique toutes les minutes
    
    private int default_start_delay = 0; // delai avant la premiere execution
    
    // commons values used to set job properties
    protected String jobName = "CronFilterJob";
    protected String jobGroupName = "CronFilterJobGroup";
    protected String triggerName = "CronFilterTrigger";
    protected String triggerGroupName = "CronFilterTriggerGroup";
    
    private Scheduler scheduler = null;
    
      
	public void setReloadPeriodInMinutes(int reloadPeriodInMinutes) {
		// toutes les minutes minimum
		if (reloadPeriodInMinutes > default_reload_period) {
			default_reload_period = reloadPeriodInMinutes;
		} else {
			LOG.error("La valeur pour le rechargement est incorrecte : " + reloadPeriodInMinutes + "\n Veuillez saisir une valeur superieur a 1 (minute).");
		}
	}
    
	public void setStartDelay(int seconds) {
		if (seconds > default_start_delay) {
			default_start_delay = seconds;
		} else {
			LOG.error("La valeur pour le delai de demarrage est incorrecte : " + seconds + "\n Veuillez saisir une valeur superieur a 0");
		}
	}
    
	public int getReloadPeriodInMinutes() {
		return default_reload_period;
	}

	public int getStartDelay() {
		return default_start_delay;
	}

	protected abstract Class<? extends Job> getJobClass();

	protected String getJobName() {
		return getJobClass().getName() + jobName;
	}

	protected String getTriggerName() {
		return getJobClass().getName() + triggerName;
	}
    
	protected void startReloadJob() {
		// Fabrique de scheduler non initialiser
		SchedulerFactory sFactory = new StdSchedulerFactory();
		try {
			// variable d'instance qui contient le scheduler
			scheduler = sFactory.getScheduler();
			JobDetail job = new JobDetail(getJobName(), jobGroupName, getJobClass());
			job.getJobDataMap().put(REFERENCE, this);
			String cronExpression = "0 0/" + getReloadPeriodInMinutes() + " * * * ?";
			CronTrigger trigger = null;
			try {
				trigger = new CronTrigger(getTriggerName(), triggerGroupName, getJobName(), jobGroupName,
						cronExpression);
				scheduler.scheduleJob(job, trigger);
				scheduler.startDelayed(default_start_delay);
				LOG.info("--> Auto Reload toutes les " + getReloadPeriodInMinutes() + " minutes");
				LOG.info("--> Delai avant le 1er lancement " + getStartDelay() + " seconde(s)");
			} catch (ParseException e) {
				LOG.error("Mauvaise expression CRON", e);
				LOG.error("Echec de creation du CronFilter");
			}
		} catch (SchedulerException e1) {
			LOG.error("Echec de creation du CronFilter");
		}
	}
    
	protected void stopReloadJob() {
		if (scheduler != null) {
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				LOG.error("Echec de l'arret du du CronFilter");
			}
		}
	}
}
