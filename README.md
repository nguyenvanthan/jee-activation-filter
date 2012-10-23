jee-activation-filter
=====================

Filter to activate or desactivate an web application

Sorry but all the documentation and comment are in french :P

Version 0.0.1-snapshot

Here you can find an example of web.xml configuration (you can find this in the example folder) :

	<web-app>

	...

	<filter>
		<filter-name>activationFilter</filter-name>
		<!-- ici remplacer cette classe par votre implementation (extends ActivationFilter) -->
		<filter-class>fr.javageek.jee.filter.MyCustomActivationFilter</filter-class>
		<init-param>
			<description>
				Definit la page a charger lors que le site est inactif
			</description>
			<param-name>errorUrl</param-name>
			<param-value>/error/maintenance.jsp</param-value>
		</init-param>
		<init-param>
			<description>
				Definit les intervalles en minutes entre chaque mise a jour de statut
			</description>
			<param-name>checkPeriod</param-name>
			<param-value>5</param-value>
		</init-param>
		<init-param>
			<description>
			    definit le delai en secondes avant la premiere execution
			</description>
			<param-name>startDelay</param-name>
			<param-value>0</param-value>
		</init-param>
	</filter>

	<filter-mapping>
		<filter-name>activationFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	...
	</web-app>	


