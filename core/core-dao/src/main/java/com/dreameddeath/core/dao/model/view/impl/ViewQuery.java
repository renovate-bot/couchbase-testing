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

package com.dreameddeath.core.dao.model.view.impl;

import com.couchbase.client.java.view.Stale;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 19/12/2014.
 */
public class ViewQuery<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQuery<TKEY,TVALUE,TDOC> {
    private CouchbaseViewDao<TKEY,TVALUE,TDOC> _dao;

    private TKEY _key;
    private Collection<TKEY> _keys;
    private TKEY _startKey;
    private TKEY _endKey;
    private boolean _isInclusive;
    private boolean _isDescending;
    private int _start=0;
    private int _limit=10;
    private boolean _syncWithDoc;


    public ViewQuery(CouchbaseViewDao<TKEY,TVALUE,TDOC> dao){
        _dao = dao;
    }

    public ViewQuery(ViewQuery<TKEY,TVALUE,TDOC> src, int offset){
        _dao = src._dao;
        _startKey= src._startKey;
        _endKey = src._endKey;
        _key = src._key;
        _keys = src._keys;
        _isInclusive = src._isInclusive;
        _isDescending = src._isDescending;
        _start = src._start+offset;
        _limit = offset;
        _syncWithDoc = src._syncWithDoc;
    }

    @Override
    public CouchbaseViewDao<TKEY,TVALUE,TDOC> getDao(){return _dao;}

    @Override
    public IViewQuery<TKEY, TVALUE, TDOC> next(int nb) {
        return new ViewQuery(this,nb);
    }


    @Override
    public com.couchbase.client.java.view.ViewQuery toCouchbaseQuery(){
        String designDoc = ICouchbaseBucket.Utils.buildDesignDoc(_dao.getClient().getPrefix(), _dao.getDesignDoc());
        com.couchbase.client.java.view.ViewQuery result = com.couchbase.client.java.view.ViewQuery.from(designDoc,_dao.getViewName());
        if(_key!=null) {
            _dao.getKeyTranscoder().key(result,_key);
        }
        else if(_keys!=null){
            _dao.getKeyTranscoder().keys(result,_keys);
        }
        else{
            _dao.getKeyTranscoder().startKey(result, _startKey);
            _dao.getKeyTranscoder().endKey(result,_endKey);
        }

        result.descending(_isDescending).
                inclusiveEnd(_isInclusive).
                skip(_start).
                limit(_limit);

        if(_syncWithDoc){
            result.stale(Stale.FALSE);
        }
        return result;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKey(TKEY key) {
        _key = key;
        _startKey = null;
        _endKey = null;
        _keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKeys(Collection<TKEY> keys) {
        _key = null;
        _startKey = null;
        _endKey = null;
        _keys = keys;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withStartKey(TKEY key) {
        _startKey = key;
        _key = null;
        _keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withEndKey(TKEY key, boolean isInclusive) {
        _key = null;
        _keys = null;
        _endKey = key;
        _isInclusive = isInclusive;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withDescending(boolean desc) {
        _isDescending = desc;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withOffset(int nb) {
        _start=nb;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withLimit(int nb) {
        _limit=nb;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> syncWithDoc(){
        _syncWithDoc = true;
        return this;
    }
}