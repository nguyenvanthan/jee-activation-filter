package fr.javageek.web;

import fr.javageek.jee.filter.ActivationFilter;

public class MyActivationFilter extends ActivationFilter {
	
	
	protected Status getStatus() {
		return Status.ALIVE;
	}
	
}
