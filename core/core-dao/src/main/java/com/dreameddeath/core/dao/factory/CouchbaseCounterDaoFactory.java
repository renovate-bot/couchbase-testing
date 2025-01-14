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

package com.dreameddeath.core.dao.factory;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.model.counter.CouchbaseCounter;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.transcoder.impl.CounterTranscoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDaoFactory implements IDaoFactory{
    private Map<Pattern,CouchbaseCounterDao> patternsMap
            = new ConcurrentHashMap<Pattern,CouchbaseCounterDao>();

    private final IDocumentInfoMapper documentInfoMapper;

    public CouchbaseCounterDaoFactory(Builder builder){
        documentInfoMapper = builder.documentInfoMapper;
    }


    public void addDao(CouchbaseCounterDao dao){
        synchronized (documentInfoMapper){
            try {
                if (!documentInfoMapper.contains(CouchbaseCounter.class)) {
                    documentInfoMapper.addRawDocument(CouchbaseCounter.class);
                    documentInfoMapper.getMappingFromClass(CouchbaseCounter.class).attachObject(ITranscoder.class, new CounterTranscoder());
                }
                if(dao.getKeyPattern()!=null) {
                    documentInfoMapper.getMappingFromClass(CouchbaseCounter.class).attachObject(CouchbaseCounterDao.class, dao.getKeyPattern(), dao);
                    documentInfoMapper.addKeyPattern(CouchbaseCounter.class,dao.getKeyPattern());
                }
            }
            catch(DuplicateMappedEntryInfoException|MappingNotFoundException e){
                //Will never occur
            }
        }
    }

    public CouchbaseCounterDao getDaoForKey(String key) throws DaoNotFoundException {
        try {
            CouchbaseCounterDao res = documentInfoMapper.getMappingFromClass(CouchbaseCounter.class).getAttachedObject(CouchbaseCounterDao.class, key);
            if(res!=null){
                return res;
            }
        }
        catch(MappingNotFoundException e){
            throw new DaoNotFoundException(key,DaoNotFoundException.Type.COUNTER);
        }

        throw new DaoNotFoundException(key, DaoNotFoundException.Type.COUNTER);
    }

    @Override
    public synchronized void init() {
        //Nothing to do
    }

    @Override
    public synchronized void cleanup() {
        patternsMap.clear();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private IDocumentInfoMapper documentInfoMapper;

        public Builder withDocumentInfoMapper(IDocumentInfoMapper mapper){
            documentInfoMapper = mapper;
            return this;
        }

        public CouchbaseCounterDaoFactory build(){
            return new CouchbaseCounterDaoFactory(this);
        }
    }
}
