//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContextHolder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static SpringApplicationContextHolder holder;
	

	/**
	 * private Constructor which
	 * initialize Spring ApplicationContext
	 */
	public SpringApplicationContextHolder() {
		
	}
	
	/*
	 *  
	 * @return SpringApplicationWrapper
	 */
	public static synchronized SpringApplicationContextHolder getInstance()	{
		if(holder == null)
			holder = new SpringApplicationContextHolder();
		return holder;
	}
	
	
	/*
	 * loads the Spring ApplicationContext from config-File
	 *
	 * @return ApplicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		if(applicationContext == null)
			throw new RuntimeException("Spring Context not set");
		return applicationContext;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) {
		System.out.println("ApplicationContext set");
		if(applicationContext != null)
			return;
		applicationContext = context;
	}	
	
		
	/*
	 * try to find a Bean in the Spring ApplicationContext
	 * 
	 * @param strBean String
	 * @return Object
	 */
	public static synchronized Object getBean(String strBean) {		
		Object bean = null;
		try {
			bean = applicationContext.getBean(strBean);
		} catch (BeansException e) {
			throw new NuclosFatalException(e);
		} 
		return bean;
	}
	
	/*
	 * try to find a Bean in the Spring ApplicationContext
	 * 
	 * @param strBean String
	 * @return Object
	 */
	public static synchronized <T> T getBean(Class<T> c) {		
		T bean = null;
		try{
			bean = applicationContext.getBean(c);
		} catch (BeansException e) {
			throw new NuclosFatalException(e);
		} 
		return bean;
	}
	
}
