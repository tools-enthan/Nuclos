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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common2.ContextConditionVariable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class SpringApplicationSubContextsHolder {

	private static final Logger LOG = Logger.getLogger(SpringApplicationSubContextsHolder.class);

	private static SpringApplicationSubContextsHolder INSTANCE;

	//
	
	private AbstractXmlApplicationContext clientContext;

	private final ArrayList<AbstractXmlApplicationContext> subContexts = new ArrayList<AbstractXmlApplicationContext>();

	private ContextConditionVariable lastContextCondition;
	
	/**
	 * private Constructor which
	 * initialize Spring ApplicationContext
	 */
	SpringApplicationSubContextsHolder() {
		INSTANCE = this;
	}

	public static SpringApplicationSubContextsHolder getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public void setClientContext(AbstractXmlApplicationContext ctx) {
		this.clientContext = ctx;
	}
	
	public void setLastContextCondition(ContextConditionVariable lastContextCondition) {
		this.lastContextCondition = lastContextCondition;
	}

	public synchronized void registerSubContext(AbstractXmlApplicationContext ctx) {
		subContexts.add(ctx);
	}

	/*
	 * try to find a Bean in the Spring ApplicationContext
	 * 
	 * @param strBean String
	 * @return Object
	 */
	public Object searchBean(String strBean) {
		Object bean = null;
		try {
			final List<AbstractXmlApplicationContext> subs;
			synchronized (this) {
				subs = (List<AbstractXmlApplicationContext>) subContexts.clone();
			}
			if (subs.isEmpty()) {
				if (clientContext == null) {
					throw new IllegalStateException("too early");
				}
				if (clientContext.containsBean(strBean)) {
					bean = clientContext.getBean(strBean);
				}
			}
			else {
				bean = getSubContextBean(strBean, subs);
			}
			if (bean == null) {
				LOG.info("waiting for lastContextCondition in searchBean()");
				synchronized (lastContextCondition) {
					lastContextCondition.waitFor();
				}
				bean = getSubContextBean(strBean, subs);
			}
			if (bean == null) {
				throw new NoSuchBeanDefinitionException(strBean);
			}
		}
		catch (BeansException e) {
			throw new NuclosFatalException(e);
		}
		return bean;
	}
	
	private static <T> T getSubContextBean(String strBean, List<AbstractXmlApplicationContext> subs) {
		T bean = null;
		for (AbstractXmlApplicationContext c : subs) {
			if (c.containsBean(strBean)) {
				bean = (T) c.getBean(strBean);
				break;
			}
		}
		return bean;
	}

	public <T> T getBean(Class<T> c) {		
		T bean = null;
		try{
			final List<AbstractXmlApplicationContext> subs;
			synchronized (this) {
				subs = (List<AbstractXmlApplicationContext>) subContexts.clone();
			}
			if (subs.isEmpty()) {
				if (clientContext == null) {
					throw new IllegalStateException("too early");
				}
				if (!clientContext.getBeansOfType(c).isEmpty()) {
					bean = clientContext.getBean(c);
				}
			}
			else {
				bean = getSubContextBean(c, subs);
			}
			if (bean == null) {
				LOG.info("waiting for lastContextCondition in getBean()");
				synchronized (lastContextCondition) {
					lastContextCondition.waitFor();
				}
				bean = getSubContextBean(c, subs);
			}
			if (bean == null) {
				throw new NoSuchBeanDefinitionException(c.getName());
			}
		} catch (BeansException e) {
			throw new NuclosFatalException(e);
		} 
		return bean;
	}
	
	private static <T> T getSubContextBean(Class<T> c, List<AbstractXmlApplicationContext> subs) {
		T bean = null;
		for (AbstractXmlApplicationContext sub : subs) {
			if (!sub.getBeansOfType(c).isEmpty()) {
				bean = sub.getBean(c);
				break;
			}
		}
		return bean;
	}
	
	public <T> Map<String, T> getBeansOfType(Class<T> c) {		
		Map<String, T> result = new HashMap<String, T>();
		try{
			final List<AbstractXmlApplicationContext> subs;
			synchronized (this) {
				subs = (List<AbstractXmlApplicationContext>) subContexts.clone();
			}
			if (subs.isEmpty()) {
				Map<String, T> subResult = clientContext.getBeansOfType(c);
				if (subResult != null) {
					result.putAll(subResult);
				}
			}
			else {
				for (AbstractXmlApplicationContext sub : subs) {
					Map<String, T> subResult = sub.getBeansOfType(c);
					if (subResult != null) {
						for (String key : subResult.keySet()) {
							if (result.containsKey(key)) {
								throw new NuclosFatalException(String.format("getBeansOfType(%s) failed! Duplicate key \"%s\" in result", c.getName(), key));
							}
							result.put(key, subResult.get(key));
						}
					}
				}
			}
		} catch (BeansException e) {
			throw new NuclosFatalException(e);
		} 
		return result;
	}

}
