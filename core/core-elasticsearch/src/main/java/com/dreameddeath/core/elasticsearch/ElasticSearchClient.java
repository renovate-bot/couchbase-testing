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

package com.dreameddeath.core.elasticsearch;


import com.dreameddeath.core.elasticsearch.exception.JsonEncodingException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.node.NodeBuilder;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchClient {
    private Client _client;
    private ObjectMapper _objectMapper;

    public ElasticSearchClient(Client client,ObjectMapper mapper){
        _client = client;
        _objectMapper = mapper;
    }


    public ElasticSearchClient(String clusterName,ObjectMapper mapper){
        this(NodeBuilder.nodeBuilder().client(true).clusterName(clusterName).build().client(),mapper);
    }

    public Client getClient(){
        return _client;
    }

    public Observable<GetResponse> get(String indexName,String type,String key){
        ActionFuture<GetResponse> asyncRes = _client.prepareGet(indexName, type, key).execute();
        return Observable.from(asyncRes);//TODO mange common errors
    }


    public Observable<IndexResponse> create(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = _objectMapper.writeValueAsBytes(doc);
            ActionFuture<IndexResponse> asyncRes = _client.prepareIndex(indexName, type, doc.getBaseMeta().getKey()).setSource(encodedStr).setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL).execute();
            return Observable.from(asyncRes);//TODO mange common errors
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }


    public Observable<UpdateResponse> update(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = _objectMapper.writeValueAsBytes(doc);
            ActionFuture<UpdateResponse> asyncRes = _client.prepareUpdate(indexName, type, doc.getBaseMeta().getKey()).setDoc(encodedStr).setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL).execute();
            return Observable.from(asyncRes);//TODO mange common errors
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }

    public Observable<DeleteResponse> delete(String indexName,String type,String key){
        ActionFuture<DeleteResponse> asyncRes = _client.prepareDelete(indexName, type, key).execute();
        return Observable.from(asyncRes);//TODO mange common errors
    }


    public Observable<SearchResponse> search(String[] indexes,String[] types,String query){
        ActionFuture<SearchResponse> asyncRes = _client.prepareSearch(indexes).setTypes(types).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(query).execute();
        return Observable.from(asyncRes);//TODO mange common errors
    }

}