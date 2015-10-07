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

package com.dreameddeath.infrastructure.daemon.services;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusUpdateRequest;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
@Path("/daemon")
@ServiceDef(name= RestLocalDaemonAdminService.DAEMON_SERVICE_NAME,version= RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION,status = VersionStatus.STABLE)
@Api(value = "/daemon", description = "Daemon Administration service")
public class RestLocalDaemonAdminService extends AbstractExposableService {
    public static final String DAEMON_SERVICE_NAME ="daemon#admin#status";
    public static final String DAEMON_SERVICE_VERSION ="1.0";

    private final RestLocalWebServerAdminService webServerAdminResource=new RestLocalWebServerAdminService();
    private AbstractDaemon daemon;

    public void setDaemon(AbstractDaemon daemon){
        this.daemon = daemon;
        webServerAdminResource.setDaemon(daemon);
    }

    @Override
    public String getId(){
        return daemon.getUuid().toString();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "give the status of the daemon",
            response = DaemonInfo.class,
            position = 0)
    public DaemonInfo getInfo(){
        return new DaemonInfo(daemon);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "give the status of the daemon",
            response = StatusResponse.class,
            position = 0)
    public StatusResponse getStatus(){
        return buildStatus(daemon.getStatus());
    }


    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "set the status of the daemon",
            response = StatusResponse.class,
            position = 1)
    public StatusResponse setStatus(StatusUpdateRequest statusUpdateRequest){
        try{
            if (statusUpdateRequest.getStatus() == StatusUpdateRequest.Status.START) {
                daemon.getDaemonLifeCycle().start();
            }
            else if(statusUpdateRequest.getStatus() == StatusUpdateRequest.Status.STOP) {
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        daemon.getDaemonLifeCycle().stop();
                    }
                    catch(Exception e){

                    }
                }).start();
                return buildStatus(IDaemonLifeCycle.Status.STOPPING);
            }
            else{
                daemon.getDaemonLifeCycle().halt();
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return getStatus();
    }

    protected StatusResponse buildStatus(IDaemonLifeCycle.Status status){
        StatusResponse result = new StatusResponse();
        result.setStatus(status);
        return result;
    }

    @Path("webservers")
    public RestLocalWebServerAdminService getWebServerItemResource(){
        return webServerAdminResource;
    }
}