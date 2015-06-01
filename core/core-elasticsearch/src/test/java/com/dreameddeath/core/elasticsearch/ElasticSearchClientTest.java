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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.dreameddeath.testing.elasticsearch.ElasticSearchServer;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchClientTest {
    public static final String CLUSTER_NAME = "testEsClient";
    public static final String INDEX_NAME = "test";
    ElasticSearchServer _server;

    public static class TestAddress extends CouchbaseDocumentElement {
        @DocumentProperty("road")
        public String road;

        @DocumentProperty("postalCode")
        public Integer postalCode;

        @DocumentProperty("city")
        public String city;


        public static TestAddress newAddress(String road,Integer postalCode,String city){
            TestAddress result = new TestAddress();
            result.road = road;
            result.postalCode = postalCode;
            result.city = city;
            return result;
        }
    }


    public static class TestDoc extends CouchbaseDocument {
        @DocumentProperty("lastName")
        public String lastName;

        @DocumentProperty("firstName")
        public String firstName;

        @DocumentProperty("addresses")
        public List<TestAddress> addresses=new ArrayList<>();


    }


    @Before
    public void initServer()throws Exception{
        _server = new ElasticSearchServer(CLUSTER_NAME);
        _server.start();
        _server.createAndInitIndex(INDEX_NAME);
    }




    @Test
    public void testIndexingDocument() throws Exception{
        ElasticSearchClient client = new ElasticSearchClient(_server.getClient(), GenericJacksonTranscoder.MAPPER);
        TestDoc doc = new TestDoc();
        doc.firstName = "firstName1";
        doc.lastName = "lastName1";
        doc.addresses.add(TestAddress.newAddress("road 1", 12345, "City1"));
        doc.addresses.add(TestAddress.newAddress("road 2", 67890, "City2"));
        doc.getBaseMeta().setCas(1);
        doc.getBaseMeta().setKey("/test/1");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }
        doc.firstName="firstName2";
        doc.getBaseMeta().setKey("/test/2");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }
        doc.firstName="firstName3 firstName2";
        doc.lastName="lastName2";
        doc.addresses.remove(1);
        doc.getBaseMeta().setKey("/test/3");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }

        //Wait for indexing
        Thread.sleep(2000);

        SearchResponse searchResponse = client.getClient().prepareSearch(INDEX_NAME).setTypes("testDoc").setQuery(QueryBuilders.matchQuery("lastName", "lastName1")).execute().actionGet();
        assertEquals(2, searchResponse.getHits().getTotalHits());

        SearchResponse searchFirstNameResponse = client.getClient().prepareSearch(INDEX_NAME).setTypes("testDoc").setQuery(QueryBuilders.matchQuery("firstName", "firstName2")).execute().actionGet();
        assertEquals(2,searchFirstNameResponse.getHits().getTotalHits());

        SearchResponse searchAddressesResponse = client.getClient().prepareSearch(INDEX_NAME).setTypes("testDoc").setQuery(QueryBuilders.matchQuery("addresses.postalCode", 67890)).execute().actionGet();
        assertEquals(2,searchAddressesResponse.getHits().getTotalHits());

    }

    @After
    public void closeServer(){
        _server.stop();
    }
}