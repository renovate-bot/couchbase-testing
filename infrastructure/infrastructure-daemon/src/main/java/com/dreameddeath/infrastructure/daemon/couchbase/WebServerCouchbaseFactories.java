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

package com.dreameddeath.infrastructure.daemon.couchbase;

import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class WebServerCouchbaseFactories{
    private final CouchbaseDocumentDaoFactory documentDaoFactory;
    private final CouchbaseSessionFactory couchbaseSessionFactory;

    public WebServerCouchbaseFactories(CouchbaseSessionFactory sessionFactory,CouchbaseDocumentDaoFactory documentDaoFactory) {
        //super(clusterFactory, bucketFactory);
        this.documentDaoFactory = documentDaoFactory;
        this.couchbaseSessionFactory=sessionFactory;
    }

    public CouchbaseDocumentDaoFactory getDocumentDaoFactory() {
        return documentDaoFactory;
    }

    public CouchbaseSessionFactory getCouchbaseSessionFactory() {
        return couchbaseSessionFactory;
    }

    public void close(){
        documentDaoFactory.cleanup();
    }
}
