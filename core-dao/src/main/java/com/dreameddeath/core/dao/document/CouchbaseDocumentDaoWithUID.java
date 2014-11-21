package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public abstract class CouchbaseDocumentDaoWithUID<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {

    public abstract String getKeyFromUID(String uid);

    public T getFromUID(String uid) throws DaoException,StorageException{
        T result= get(getKeyFromUID(uid));
        result.getBaseMeta().setStateSync();
        return result;
    }
}
