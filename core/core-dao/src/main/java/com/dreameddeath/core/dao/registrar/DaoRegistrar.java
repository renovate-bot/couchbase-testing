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

package com.dreameddeath.core.dao.registrar;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.curator.registrar.impl.CuratorRegistrarImpl;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 28/10/2015.
 */
public class DaoRegistrar extends CuratorRegistrarImpl<DaoInstanceInfo> {
    public static String getRootPath(){
        try {
            return CouchbaseDaoConfigProperties.DAO_REGISTER_PATH.getMandatoryValue("The daemon registering path isn't set");
        }
        catch (ConfigPropertyValueNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public DaoRegistrar(CuratorFramework curatorFramework) {
        super(curatorFramework, getRootPath());
    }

    @Override
    protected byte[] serialize(DaoInstanceInfo obj) throws Exception {
        return mapper.writeValueAsBytes(obj);
    }
}
