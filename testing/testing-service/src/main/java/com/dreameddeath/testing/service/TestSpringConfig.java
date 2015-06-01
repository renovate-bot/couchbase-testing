/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.testing.service;

import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceInstanceJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 24/03/2015.
 */
@Configuration
@ImportResource({"classpath:META-INF/cxf/cxf.xml"})
public class TestSpringConfig implements ServletContextAware
{
    @Autowired
    private ConfigurableApplicationContext ctxt;
    private ServletContext _servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this._servletContext = servletContext;
    }

    public void setCtxt(ConfigurableApplicationContext ctxt) {
        this.ctxt = ctxt;
    }


    @Bean(name="curatorClient")
    public CuratorFramework getClient() throws Exception{
        return (CuratorFramework)_servletContext.getAttribute("curatorClient");
    }


    @Bean(name="serviceDiscoverer")
    public ServiceDiscoverer getDiscoverer(){
        return (ServiceDiscoverer)_servletContext.getAttribute("serviceDiscoverer");
    }


    @Bean(name="testingJaxRsServer")
    public Server buildJaxRsServer() {
        SpringJAXRSServerFactoryBean factory = new SpringJAXRSServerFactoryBean();
        factory.setTransportId("http://cxf.apache.org/transports/http");
        factory.setAddress("/apis");
        ObjectMapper mapper =(ObjectMapper)_servletContext.getAttribute("jacksonObjectMapper");
        if(mapper==null){
            mapper = ServiceInstanceJacksonMapper.getInstance();
        }
        factory.setProviders(Arrays.asList(new JacksonJsonProvider(mapper)));

        List<ResourceProvider> resourceProviders = new LinkedList<>();
        Map<String,AbstractExposableService> servicesMap = (Map)_servletContext.getAttribute("servicesMap");
        final IRestEndPointDescription endPointDescr=(IRestEndPointDescription)_servletContext.getAttribute("endPointInfo");
        for(Map.Entry<String,AbstractExposableService> serviceDef:servicesMap.entrySet()){
            serviceDef.getValue().setServiceRegistrar((ServiceRegistrar)_servletContext.getAttribute("serviceRegistrar"));
            serviceDef.getValue().setEndPoint(new IRestEndPointDescription() {
                @Override
                public int port() {
                    return endPointDescr.port();
                }

                @Override
                public String path() {
                    return (endPointDescr.path() + factory.getAddress()).replaceAll("//{2,}", "/");
                }

                @Override
                public String host() {
                    return endPointDescr.host();
                }
            });
            ctxt.getBeanFactory().registerSingleton(serviceDef.getKey(), serviceDef.getValue());
            SpringResourceFactory factoryResource = new SpringResourceFactory(serviceDef.getKey());
            try{
                serviceDef.getValue().getClass().getMethod("setGlobalContextTranscoder",IGlobalContextTranscoder.class).invoke(serviceDef.getValue(),
                new IGlobalContextTranscoder() {
                    @Override
                    public String encode(IGlobalContext ctxt) {
                        return "";
                    }

                    @Override
                    public IGlobalContext decode(String encodedContext) {
                        return null;
                    }
                });
            }
            catch(Exception e){

            }
            factoryResource.setApplicationContext(ctxt);
            resourceProviders.add(factoryResource);
        }

        factory.setResourceProviders(resourceProviders);
        factory.setApplicationContext(ctxt);
        return factory.create();
    }

}