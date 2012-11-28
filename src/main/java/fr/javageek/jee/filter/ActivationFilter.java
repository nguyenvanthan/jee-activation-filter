package fr.javageek.jee.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

/**
 * <b>Description : Classe abstraite pour l'activation/desactivation d'une
 * application par un Filter</b>
 * 
 * @author Christian NGUYEN VAN THAN alias @JavaGeekFr<br>
 */
public abstract class ActivationFilter extends CronFilter implements Filter {


	private static final Logger LOG = Logger.getLogger(ActivationFilter.class);

	// parametre du filtre: definit la page a charger lors que le site est inactif 
	private static final String FILTER_PARAMS_ERROR_URL = "errorUrl";

	// parametre du filtre: definit les intervalles en minutes entre chaque mise a jour de statut
	private static final String FILTER_PARAMS_CHECK_PERIOD = "checkPeriod";

	// parametre du filtre: definit le delai en secondes avant la premiere execution
	private static final String FILTER_PARAMS__START_DELAY = "startDelay";
	
	// pattern pour message d'avertissement lors d'un parametre non configure
	private static final String WARN_MISSING_PARAM_MSG_PATTERN = "********* ATTENTION : le parametre '%s' du filtre est absent, configuration par defaut *********";

	// pattern pour message d'avertissement lors d'un parametre a une mauvaise valeur
	private static final String WARN_BAD_PARAM_MSG_PATTERN = "********* ATTENTION : le parametre '%s' du filtre est incorrect, configuration par defaut *********";

	// pattern pour message d'info sur la frequence du rechargement
	private static final String INFO_RELOAD_PERIOD_MSG_PATTERN = "Rechargement chaque %d minute(s)";

	// pattern pour message d'info sur le delai avant la premiere execution
	private static final String INFO_DELAY_MSG_PATTERN = "%d minute(s) avant le premier lancement";

	private static byte isAlive = 1;

	protected static final int START_SECONDS_DELAY = 0;

	private String ERROR_PAGE_URL = "error.jsp";

	protected enum Status {
		ALIVE, // site accessible
		DOWN // site en cours de maintenance ou desactive
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.warn("******************************************** DEMARRAGE DU FILTER D'ACTIVATION/DESACTIVATION ****************************************");
		String errorUrl = filterConfig.getInitParameter(FILTER_PARAMS_ERROR_URL);
		if (errorUrl != null && !"".equals(errorUrl)) {
			ERROR_PAGE_URL = errorUrl;
		} else {
			LOG.warn("********* ATTENTION : le parametre 'errorUrl' du filtre est absent, configuration par defaut *********");
		}
		LOG.warn("******************************************** URL de la page d'erreur : " + ERROR_PAGE_URL	+ " ****************************************");

		String checkPeriod = filterConfig.getInitParameter(FILTER_PARAMS_CHECK_PERIOD);
		if (checkPeriod != null) {
			try {
				setReloadPeriodInMinutes(Integer.parseInt(checkPeriod));
			} catch (NumberFormatException ne) {
				LOG.warn(String.format(WARN_BAD_PARAM_MSG_PATTERN, FILTER_PARAMS_CHECK_PERIOD));
			}
		} else {
			LOG.warn(String.format(WARN_MISSING_PARAM_MSG_PATTERN, FILTER_PARAMS_CHECK_PERIOD));
		}
		
		LOG.info(String.format(INFO_RELOAD_PERIOD_MSG_PATTERN, getReloadPeriodInMinutes()));
		
		
		String startDelay = filterConfig.getInitParameter(FILTER_PARAMS__START_DELAY);
		if (startDelay != null) {
			try {
				setStartDelayInSeconds(Integer.parseInt(startDelay));
			} catch (NumberFormatException ne) {
				LOG.warn(String.format(WARN_BAD_PARAM_MSG_PATTERN, FILTER_PARAMS__START_DELAY));
			}
		} else {
			LOG.warn(String.format(WARN_MISSING_PARAM_MSG_PATTERN, FILTER_PARAMS__START_DELAY));
		}
		LOG.info(String.format(INFO_DELAY_MSG_PATTERN, getStartDelayInSeconds()));

		// on charge le CRON de scrutation
		startReloadJob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public final void destroy() {
		LOG.warn("******************************************** DESTRUCTION DU FILTER D'ACTIVATION/DESACTIVATION **************************************");
		stopReloadJob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (isAlive > 0) {
			chain.doFilter(request, response);
		} else {
			// redirection vers la page de maintenance
			request.getRequestDispatcher(ERROR_PAGE_URL).forward(request, response);
		}
	}

	void setApplicationStatus(ActivationFilter.Status status) {
		if (status != null && status.equals(Status.ALIVE)) {
			isAlive = 1;
		} else {
			isAlive = 0;
		}
	}

	protected void printApplicationStatus() {
		if (isAlive > 0) {
			LOG.warn("-------------------------------------------- ETAT : OK ------------------------------------------------------");
		} else {
			LOG.warn("--------------------------------------- !! ETAT : KO !! -----------------------------------------------------");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.caceisct.framework.filter.CronFilter#getJobClass()
	 */
	@Override
	protected Class<ActivationJob> getJobClass() {
		return ActivationJob.class;
	}

	/**
	 * Doit renvoyer le statut de l'application
	 * 
	 * @return ActivationFilter.Status.ALIVE si site OK, sinon
	 *         ActivationFilter.Status.DOWN
	 */
	protected abstract ActivationFilter.Status getStatus();

	/**
	 * Change l'url de la page d'indisponibilite
	 * Methode facultative qui sert a ecraser la valeur definit dans le web.xml
	 * @param url nouvelle url de la page d'indisponibilite
	 */
	protected void setErrorUrlPage(String url) {
		if (url != null && !"".equals(url.trim())) {
			ERROR_PAGE_URL = url;
		}
	}
}
