package fr.javageek.jee.filter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <b>Description : Job CRON qui scrute la base pour activer/desactiver l'application</b> 
 * @author Christian NGUYEN VAN THAN alias @JavaGeekFr<br>
 */
public class ActivationJob implements Job {
    
    private static final Logger LOG = Logger.getLogger(ActivationJob.class);
	
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
	    LOG.warn(" ----------------- Mise a jour du statut de l'application ------------------------- ");
	    JobDataMap parameters = ctx.getMergedJobDataMap();
	    Object o = parameters.get(ActivationFilter.REFERENCE);
	    if (o instanceof ActivationFilter){
		ActivationFilter filter = (ActivationFilter) o;
		filter.setApplicationStatus( filter.getStatus() );
		LOG.warn("******************************************** STATUT DE L'APPLICATION ****************************************");
		filter.printApplicationStatus();
		LOG.warn("*************************************************************************************************************");
	    }
	}
	
}
