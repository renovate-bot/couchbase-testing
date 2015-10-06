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

package com.dreameddeath.core.service.model;

import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 15/01/2015.
 */
public abstract class AbstractExposableService {
    private String address="";
    private IRestEndPointDescription endPoint;
    private ServiceRegistrar _serviceRegistrar;


    public String getId(){return UUID.randomUUID().toString();}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setEndPoint(IRestEndPointDescription obj){
        endPoint = obj;
    }
    public IRestEndPointDescription getEndPoint() {
        return endPoint;
    }
    public void setServiceRegistrar(ServiceRegistrar serviceRegistrar){
        _serviceRegistrar = serviceRegistrar;
        serviceRegistrar.addService(this);
    }

    public ServiceRegistrar getServiceRegistrar() {
        return _serviceRegistrar;
    }
}
