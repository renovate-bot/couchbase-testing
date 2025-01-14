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

import com.dreameddeath.core.couchbase.exception.TranscoderNotFoundException;
import com.dreameddeath.core.couchbase.utils.CouchbaseUtils;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
public class CouchbaseUniqueKeyDaoFactory implements IDaoFactory{
    private IDocumentInfoMapper documentInfoMapper;
    private Map<String, CouchbaseUniqueKeyDao> daosMap  = new ConcurrentHashMap<String, CouchbaseUniqueKeyDao>();

    public CouchbaseUniqueKeyDaoFactory(Builder builder){
        documentInfoMapper = builder.documentInfoMapper;
    }


    public void addDaoFor(String nameSpace,CouchbaseUniqueKeyDao dao){
        daosMap.put(nameSpace,dao);
    }

    public CouchbaseUniqueKeyDao getDaoFor(String nameSpace) throws DaoNotFoundException{
        CouchbaseUniqueKeyDao result =daosMap.get(nameSpace);
        if(result==null){
            throw new DaoNotFoundException(nameSpace, DaoNotFoundException.Type.KEY);
        }
        return daosMap.get(nameSpace);
    }

    public CouchbaseUniqueKeyDao getDaoForInternalKey(String key) throws DaoNotFoundException{
        for(Map.Entry<String,CouchbaseUniqueKeyDao> entry:daosMap.entrySet()){
            String nameSpace = entry.getValue().extractNameSpace(key);
            if(entry.getKey().equals(nameSpace)){
                return entry.getValue();
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.KEY);
    }

    @Override
    public synchronized void cleanup() {
        daosMap.clear();
    }

    @Override
    public synchronized void init() {
        try {
            documentInfoMapper.addDocument(CouchbaseUniqueKey.class, CouchbaseUniqueKeyDao.UNIQ_KEY_PATTERN);
            documentInfoMapper.getMappingFromClass(CouchbaseUniqueKey.class).attachObject(ITranscoder.class, CouchbaseUtils.resolveTranscoderForClass(CouchbaseUniqueKey.class));
        }
        catch(DuplicateMappedEntryInfoException|MappingNotFoundException e){
            //ignore error
        }
        catch(TranscoderNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public Map<CouchbaseUniqueKeyDao,List<String>> mapRemovedUniqueKeys(CouchbaseDocument doc) throws DaoNotFoundException{
        Map<CouchbaseUniqueKeyDao, List<String>> mapKeys = new HashMap<CouchbaseUniqueKeyDao, List<String>>();

        if(doc instanceof IHasUniqueKeysRef) {
            Set<String> removedKeys = ((IHasUniqueKeysRef)doc).getRemovedUniqueKeys();
            for (String key : removedKeys) {
                CouchbaseUniqueKeyDao dao = getDaoForInternalKey(key);
                if (!mapKeys.containsKey(dao)) {
                    mapKeys.put(dao, new ArrayList<String>());
                }
                mapKeys.get(dao).add(key);
            }

        }
        return mapKeys;
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

        public CouchbaseUniqueKeyDaoFactory build(){
            return new CouchbaseUniqueKeyDaoFactory(this);
        }

    }

}
