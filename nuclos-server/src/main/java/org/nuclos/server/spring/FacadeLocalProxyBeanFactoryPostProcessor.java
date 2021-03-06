//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

@Component
public class FacadeLocalProxyBeanFactoryPostProcessor extends InstantiationAwareBeanPostProcessorAdapter 
	implements BeanFactoryPostProcessor, PriorityOrdered {
	
	private static final Logger LOG = Logger.getLogger(FacadeLocalProxyBeanFactoryPostProcessor.class);
	
	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered
	
	//
	
	private ConfigurableListableBeanFactory beanFactory;
	
	private BeanDefinitionRegistry registry;
	
	private Map<String,Class<?>> beanName2Class = new HashMap<String, Class<?>>();

	FacadeLocalProxyBeanFactoryPostProcessor() {
	}

	/**
	 * Set the order value of this object for sorting purposes.
	 * @see PriorityOrdered
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		registry = (BeanDefinitionRegistry) beanFactory;
		
		for (String bn: beanFactory.getBeanDefinitionNames()) {
			final BeanDefinition bd = beanFactory.getBeanDefinition(bn);
			if (bd.isAbstract()) {
				continue;
			}
			if (!bd.isSingleton()) {
				throw new FatalBeanException("facade bean " + bn + " is not defined as singleton");
			}
			final String bclass = bd.getBeanClassName();
			if (bclass == null) {
				continue;
			}
			if (bclass.startsWith("org.nuclos.server.spring.")) {
				LOG.info("Found bean definition " + bn + ": " + bd);
				LOG.info("bean type for "  + bn + ": " + beanFactory.getType(bn));
			}
			if (bclass.startsWith("org.nuclos.server.") && bclass.endsWith("FacadeBean")) {
				LOG.info("Found bean definition " + bn + ": " + bd);
				final String facadeLocalInterface = mkLocalInterfaceName(bclass);
				final Class<?> facadeLocal;
				try {
					facadeLocal = beanFactory.getBeanClassLoader().loadClass(facadeLocalInterface);
				}
				catch (ClassNotFoundException e) {
					// no local interface -> ignore and continue
					LOG.warn("No local interface for facade bean: " + bn);
					continue;
					// throw new FatalBeanException("Failed on interface " + facadeLocalInterface, e);
				}
				final MutablePropertyValues props = new MutablePropertyValues();
				// final ConstructorArgumentValues props = new ConstructorArgumentValues();
				addPropertyValue(props, "facadeLocalInterface", facadeLocal, "java.lang.Class");
				addPropertyRef(props, "facadeBean", bn);
				addPropertyRef(props, "nuclosRemoteContextHolder", "nuclosRemoteContextHolder");

				final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
				beanDefinition.setBeanClassName("org.nuclos.server.spring.FacadeLocalProxyFactoryBean");
				beanDefinition.setPropertyValues(props);
				
				final String beanName = mkBeanName(facadeLocalInterface);
				registry.registerBeanDefinition(beanName, beanDefinition);
				beanFactory.registerDependentBean(beanName, bn);
				beanName2Class.put(beanName, facadeLocal);
				
				LOG.info("registered bean definition " + beanName + ": " + beanDefinition);
				/*
				LOG.info("contains bean definition for "  + beanName + ": " + beanFactory.containsBeanDefinition(beanName));
				LOG.info("contains bean for "  + beanName + ": " + beanFactory.containsBean(beanName));
				final Object bean = beanFactory.getSingleton(beanName);
				LOG.info("bean for "  + beanName + ": " + beanFactory.getSingleton(beanName));
				 */
			}
		}
		LOG.info("END");
	}
	
	private void addPropertyValue(MutablePropertyValues props, String property, Object value, String type) {
		props.addPropertyValue(property, value);
	}
	
	private void addPropertyRef(MutablePropertyValues props, String property, String refBeanName) {
		props.addPropertyValue(property, new RuntimeBeanReference(refBeanName));
	}
	
	private String mkLocalInterfaceName(String facadeBean) {
		final int len = facadeBean.length();
		return facadeBean.substring(0, len - 4) + "Local";
	}
	
	private String mkBeanName(String classname) {
		final int len = classname.lastIndexOf('.');
		return classname.substring(len + 1, len + 2).toLowerCase() + classname.substring(len + 2);
	}
	
	//
	
	@Override
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		Class<?> result = beanName2Class.get(beanName);
		/*
		if (result != null || interestedInBeanName(beanName)) {
			LOG.info("predictBeanType for " + beanName + ": " + beanClass + " -> " + result);
		}
		 */
		return result;
	}

	private boolean interestedInBeanName(String beanName) {
		return beanName.endsWith("Local") || beanName.startsWith("org.nuclos.server.spring.");
	}
	
}
