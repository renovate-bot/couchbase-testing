package com.dreameddeath.common.dao;

import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.RuntimeException;

import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.CASValue;
import net.spy.memcached.CASResponse;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;
import com.dreameddeath.common.storage.OperationFutureWrapper;



public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private CouchbaseClientWrapper _client;
    public abstract Transcoder<T> getTranscoder();
    protected abstract void buildKey(T newObject);
    
    public CouchbaseClientWrapper getClientWrapper(){
        return _client;
    }
    
    //Maybe overriden to improve (bulk key attribution)
    protected void buildKeys(Collection<T> newObjects){
        for(T newObject:newObjects){
            if(newObject.getKey()!=null){
                ///TODO throw an error
            }
            buildKey(newObject);
        }
    }
    
    public CouchbaseDocumentDao(CouchbaseClientWrapper client){
        _client = client;
    }
    
    public T create(T obj){
        if(obj.getKey()!=null){/**TODO throw an error*/}
        buildKey(obj);
        try {
            _client.add(obj,getTranscoder()).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for cas update", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof CancellationException) {
                throw (CancellationException) e.getCause();
            } else {
                throw new RuntimeException("Exception waiting for cas update", e);
            }
        }
        
        return obj;
    }
    
    public Collection<T> createBulk(Collection<T> objs){
        List<OperationFutureWrapper<Boolean,T>> futures = new ArrayList<OperationFutureWrapper<Boolean,T>>(objs.size());
        
        buildKeys(objs);
        for(T obj:objs){
            futures.add(_client.add(obj,getTranscoder()));
        }
        List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for(OperationFutureWrapper<Boolean,T> future:futures){
            try{
                Boolean result = future.get();
                if(!result){
                    future.getDoc().setKey(null);
                    ///TODO better error management for errors
                }
            }
            catch (InterruptedException e) {
                exceptions.add(new RuntimeException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add((CancellationException) e.getCause());
                } else {
                    exceptions.add(new RuntimeException("Exception waiting for value", e));
                }
            }
        }
        return objs;
    }

    public T get(String key){
        return _client.gets(key,getTranscoder());
    }
    
    public List<T> bulkGet(Set<String> keys){
        List<OperationFutureWrapper<CASValue<T>,T>> futures = new ArrayList<OperationFutureWrapper<CASValue<T>,T>>(keys.size());
        List<T> results = new ArrayList<T>(keys.size());
        for(String key : keys){
            futures.add(_client.asyncGets(key,getTranscoder()));
        }
        List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for(OperationFutureWrapper<CASValue<T>,T> future : futures){
            try{
                results.add(future.get().getValue());
            }
            catch (InterruptedException e) {
                exceptions.add(new RuntimeException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add((CancellationException) e.getCause());
                } else {
                    exceptions.add(new RuntimeException("Exception waiting for value", e));
                }
            }
        }
        return results;
    }
    
    public T update(T obj){
        if(obj.getKey()==null){/**TODO throw an error*/}
        _client.cas(obj,getTranscoder());
        return obj;
    }
    
    public Collection<T> updateBulk(Collection<T> objs){
        List<OperationFutureWrapper<CASResponse,T>> futures = new ArrayList<OperationFutureWrapper<CASResponse,T>>(objs.size());
        
        buildKeys(objs);
        for(T obj:objs){
            futures.add(_client.asyncCas(obj,getTranscoder()));
        }
        
        List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
        for(OperationFutureWrapper<CASResponse,T> future:futures){
            try{
                CASResponse result = future.get();
                ///TODO manage errors
            }
            catch (InterruptedException e) {
                exceptions.add(new RuntimeException("Interrupted waiting for value", e));
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    exceptions.add((CancellationException) e.getCause());
                } else {
                    exceptions.add(new RuntimeException("Exception waiting for value", e));
                }
            }
        }
        return objs;
    }
    
    public Collection<T> getLinkObjBulk(Collection<CouchbaseDocumentLink<T>> links){
        Collection<T> results = new ArrayList<T>(links.size());
        
        Map<String,List<CouchbaseDocumentLink<T>>> linkedDocs = new HashMap<String,List<CouchbaseDocumentLink<T>>>(links.size());
        //Retrive Unique Key
        for(CouchbaseDocumentLink<T> link : links){
            if(link.getLinkedObject()!=null){ results.add(link.getLinkedObject()); continue; }
            if(linkedDocs.containsKey(link.getKey())){
                linkedDocs.get(link.getKey()).add(link);
            }
            else{
                List<CouchbaseDocumentLink<T>> linksPerKeyList = new ArrayList<CouchbaseDocumentLink<T>>();
                linksPerKeyList.add(link);
                linkedDocs.put(link.getKey(),linksPerKeyList);
            }
        }
        Collection<T> objs = bulkGet(linkedDocs.keySet());
        results.addAll(objs);
        for(T obj:objs){
            for(CouchbaseDocumentLink<T> link: linkedDocs.get(obj.getKey())){
                link.setLinkedObject(obj);
            }
        }
        return results;
    }
    
    public T getLinkObj(CouchbaseDocumentLink<T> link){
        if(link.getLinkedObject()!=null) { return link.getLinkedObject(); }
        T result = get(link.getKey());
        link.setLinkedObject(result);
        
        return result;
    }
    
}