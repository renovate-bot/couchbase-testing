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

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.model.WebServerInfo;
import com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusUpdateRequest;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 19/09/2015.
 */
@Api(value = "/", description = "Daemon Webservers Administration service")
@Path("/")
public class RestLocalWebServerAdminService {
    private AbstractDaemon _daemon;

    public void setDaemon(AbstractDaemon daemon){
        _daemon = daemon;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "give info on all web servers",
            response = WebServerInfo.class,
            responseContainer = "List",
            position = 0)
    public List<WebServerInfo> getInfo(){
        List<WebServerInfo> result = new ArrayList<>(_daemon.getAdditionnalWebServers().size());
        for(AbstractWebServer webServer:_daemon.getAdditionnalWebServers()){
            result.add(new WebServerInfo(webServer));
        }
        return result;
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{name}")
    @ApiOperation(value = "give info on a given webserver",
            response = WebServerInfo.class,
            position = 1)
    public WebServerInfo getInfo(@PathParam("name") String name){
        return new WebServerInfo(findByName(name));
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{name}/status")
    @ApiOperation(value = "give info on a given webserver",
            response = StatusResponse.class,
            position = 2)
    public StatusResponse getStatus(@PathParam("name") String name){
        return buildStatus(findByName(name).getStatus());
    }

    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("{name}/status")
    @ApiOperation(value = "give info on a given webserver",
            response = WebServerInfo.class,
            position = 3)
    public StatusResponse putStatus(@PathParam("name") String name, StatusUpdateRequest statusUpdateRequest){
        AbstractWebServer webServer = findByName(name);
        try{
            switch (statusUpdateRequest.getStatus()){
                case START:
                    webServer.start();
                    break;
                case STOP:
                    webServer.stop();
                    break;
                case RESTART:
                    webServer.stop();
                    webServer.start();
                    break;
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }


        return buildStatus(webServer.getStatus());
    }


    protected StatusResponse buildStatus(AbstractWebServer.Status status){
        StatusResponse result = new StatusResponse();
        result.setStatus(status);
        return result;
    }


    private AbstractWebServer findByName(String name) throws NotFoundException{
        for(AbstractWebServer webServer:_daemon.getAdditionnalWebServers()){
            if(webServer.getName().equals(name)){
                return webServer;
            }
        }
        throw new NotFoundException("Cannot find server "+name);
    }


}