/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.document.IDaoForDocumentWithUID;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.ReadOnlyException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.Validator;
import com.dreameddeath.core.validation.ValidatorContext;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CouchbaseSession implements ICouchbaseSession {
    final private CouchbaseSessionFactory _sessionFactory;
    final private SessionType _sessionType;
    final private IDateTimeService _dateTimeService;
    final private IUser _user;

    private Set<CouchbaseDocument> _objectCache = new HashSet<CouchbaseDocument>();
    private Map<String,CouchbaseDocument> _sessionCache = new HashMap<String,CouchbaseDocument>();
    private Map<String,CouchbaseUniqueKey> _keyCache = new HashMap<String,CouchbaseUniqueKey>();
    private Map<String,Long> _counters = new HashMap<String, Long>();

    public CouchbaseSession(CouchbaseSessionFactory factory, IUser user){
        this(factory, SessionType.READ_ONLY,user);
    }

    public CouchbaseSession(CouchbaseSessionFactory factory, SessionType type, IUser user){
        _sessionFactory = factory;
        _dateTimeService = factory.getDateTimeServiceFactory().getService();
        _sessionType = type;
        _user = user;
    }

    protected CouchbaseDocumentDaoFactory getDocumentFactory(){
        return _sessionFactory.getDocumentDaoFactory();
    }

    protected CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return _sessionFactory.getCounterDaoFactory();
    }

    @Override
    public void reset(){
        _sessionCache.clear();
        _counters.clear();
    }

    public boolean isCalcOnly(){
        return _sessionType== SessionType.CALC_ONLY;
    }
    public boolean isReadOnly(){
        return _sessionType== SessionType.READ_ONLY;
    }
    protected void checkReadOnly(CouchbaseDocument doc) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(doc);
        }
    }

    protected void checkReadOnly(Class docClass) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(docClass);
        }
    }


    protected void checkReadOnly(String counterKey) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(counterKey);
        }
    }



    @Override
    public long getCounter(String key) throws DaoException,StorageException {
        CouchbaseCounterDao dao = _sessionFactory.getCounterDaoFactory().getDaoForKey(key);
        if(isCalcOnly() && _counters.containsKey(key)){
            return _counters.get(key);
        }
        Long value = dao.getCounter(key,isCalcOnly());
        if(isCalcOnly()){
            _counters.put(key,value);
        }
        return value;
    }

    @Override
    public long incrCounter(String key, long byVal) throws DaoException,StorageException {
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result+=byVal;
            _counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = _sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.incrCounter(key,byVal,isCalcOnly());
        }
    }

    @Override
    public long decrCounter(String key, long byVal) throws DaoException,StorageException {
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result-=byVal;
            _counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = _sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.decrCounter(key, byVal, isCalcOnly());
        }
    }

    
    public void attachDocument(CouchbaseDocument doc){
        if(doc.getBaseMeta().getKey()!=null){
            _sessionCache.put(doc.getBaseMeta().getKey(),doc);
        }
    }
    

    @Override
    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
        try{
            return attachEntity(clazz.newInstance());
        }
        catch(Exception e){
            ///TODO log something
            return null;
        }
    }


    @Override
    public <T extends CouchbaseDocument> T attachEntity(T entity){
        try{
            attachDocument(entity);
            return entity;
        }
        catch(Exception e){
            ///TODO log something
            return null;
        }
    }



    @Override
    public <T extends CouchbaseDocument> T create(T obj) throws ValidationException,DaoException,StorageException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = (CouchbaseDocumentDao<T>) _sessionFactory.getDocumentDaoFactory().getDaoForClass(obj.getClass());
        dao.create(this,obj,isCalcOnly());
        attachDocument(obj);
        return obj;
    }

    @Override
    public <T extends CouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException {
        if(obj.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
            ((CouchbaseDocumentDao<T>) _sessionFactory.getDocumentDaoFactory().getDaoForClass(obj.getClass())).buildKey(this,obj);
        }
        return obj;
    }

    @Override
    public CouchbaseDocument get(String key) throws DaoException,StorageException {
        CouchbaseDocument result = _sessionCache.get(key);
        if(result==null){
            CouchbaseDocumentDao dao = _sessionFactory.getDocumentDaoFactory().getDaoForKey(key);
            result = dao.get(key);
            attachDocument(result);
        }
        return result;
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> targetClass) throws DaoException,StorageException {
        CouchbaseDocument cacheResult = _sessionCache.get(key);
        if(cacheResult !=null){
            return (T)cacheResult;
        }
        else{
            CouchbaseDocumentDao<T> dao = _sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
            T result = dao.get(key);
            attachDocument(result);
            return result;
        }
    }

    @Override
    public <T extends CouchbaseDocument> T update(T obj)throws ValidationException,DaoException,StorageException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = _sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        dao.update(this,obj,isCalcOnly());
        return obj;
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T obj)throws ValidationException,DaoException,StorageException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = _sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        dao.delete(this,obj,isCalcOnly());
        return obj;
    }

    @Override
    public void validate(CouchbaseDocument doc) throws ValidationException {
        ((Validator<CouchbaseDocument>)_sessionFactory.getValidatorFactory().getValidator(doc.getClass())).validate(ValidatorContext.buildContext(this),doc);
    }

    @Override
    public <T extends CouchbaseDocument> T getFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException {
        IDaoForDocumentWithUID dao = (IDaoForDocumentWithUID) _sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return get(dao.getKeyFromUID(uid),targetClass);
    }


    @Override
    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException{
        IDaoForDocumentWithUID dao = (IDaoForDocumentWithUID) _sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromUID(uid);
    }


    @Override
    public <T extends CouchbaseDocument> T save(T obj) throws ValidationException,DaoException,StorageException {
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            return create(obj);
        }
        else if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            return delete(obj);
        }
        else{
            return update(obj);
        }
    }

    @Override
    public void addOrUpdateUniqueKey(CouchbaseDocument doc, Object value, String nameSpace)throws ValidationException,DaoException,StorageException,DuplicateUniqueKeyException{
        //Skip null value
        if(value==null){
            return;
        }
        checkReadOnly(doc);
        CouchbaseUniqueKeyDao dao = _sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace);
        CouchbaseUniqueKey keyDoc =dao.addOrUpdateUniqueKey(this,nameSpace,value.toString(),doc,isCalcOnly());
        _keyCache.put(keyDoc.getBaseMeta().getKey(),keyDoc);
    }



    @Override
    public CouchbaseUniqueKey getUniqueKey(String internalKey)throws DaoException,StorageException {
        CouchbaseUniqueKey keyDoc =_keyCache.get(_sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey).buildKey(internalKey));
        if(keyDoc==null){
            CouchbaseUniqueKeyDao dao = _sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
            return dao.getFromInternalKey(internalKey);
        }
        return keyDoc;
    }


    @Override
    public void removeUniqueKey(String internalKey) throws DaoException,StorageException,ValidationException {
        CouchbaseUniqueKey obj = getUniqueKey(internalKey);
        checkReadOnly(obj);
        CouchbaseUniqueKeyDao dao = _sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
        dao.removeUniqueKey(this,obj,internalKey,isCalcOnly());
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            _keyCache.remove(obj.getBaseMeta().getKey());
        }
    }

    @Override
    public DateTime getCurrentDate() {
        return null;
    }


    public enum SessionType{
        READ_ONLY,
        CALC_ONLY,
        READ_WRITE
    }

    public IDateTimeService getDateTimeService(){ return _dateTimeService; }

    @Override
    public <T extends CouchbaseDocument> IViewQuery initViewQuery(Class<T> forClass,String viewName) throws DaoException{
        CouchbaseViewDao viewDao = _sessionFactory.getDocumentDaoFactory().getViewDaoFactory().getViewDaoFor(forClass,viewName);
        return viewDao.buildViewQuery();
    }

    @Override
    public IViewQueryResult executeQuery(IViewQuery query){
        return query.getDao().query(this,isCalcOnly(),query);
    }

    @Override
    public Observable<IViewAsyncQueryResult> executeAsyncQuery(IViewQuery query) throws DaoException,StorageException {
        return query.getDao().asyncQuery(this,isCalcOnly(),query);
    }


}
