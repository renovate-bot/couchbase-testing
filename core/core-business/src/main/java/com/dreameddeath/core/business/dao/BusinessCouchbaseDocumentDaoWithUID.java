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

package com.dreameddeath.core.business.dao;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.IDaoForDocumentWithUID;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public abstract class BusinessCouchbaseDocumentDaoWithUID<T extends BusinessDocument> extends BusinessCouchbaseDocumentDao<T> implements IDaoForDocumentWithUID<T> {
    public abstract String getKeyFromUID(String uid);

    public T getFromUID(ICouchbaseSession session,String uid) throws DaoException,StorageException{
        T result= get(session,getKeyFromUID(uid));
        result.getBaseMeta().setStateSync();
        return result;
    }
}
