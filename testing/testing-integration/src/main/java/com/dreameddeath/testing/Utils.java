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

package com.dreameddeath.testing;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.dcp.CouchbaseDCPConnector;
import com.dreameddeath.core.couchbase.dcp.ICouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.dcp.impl.DefaultCouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketWrapper;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.elasticsearch.ElasticSearchClient;
import com.dreameddeath.core.elasticsearch.IElasticSearchMapper;
import com.dreameddeath.core.elasticsearch.dao.ElasticSearchDao;
import com.dreameddeath.core.elasticsearch.dcp.ElasticSearchDcpFlowHandler;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.session.impl.ElasticSearchSessionFactory;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.couchbase.dcp.CouchbaseDCPConnectorSimulator;
import com.dreameddeath.testing.elasticsearch.ElasticSearchServer;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 18/12/2014.
 */
public class Utils {

    public static final String COUCHBASE_BUCKET_NAME_ENV_VAR = "COUCHBASE_BUCKET_NAME";
    public static final String COUCHBASE_DB_URL_ENV_VAR = "COUCHBASE_DB_URL";
    public static final String COUCHBASE_BUCKET_PASSWORD_ENV_VAR = "COUCHBASE_BUCKET_PASSWORD";
    public static final String TESTING_DESIGN_DOC = "TESTING_TOOL";
    public static final String VIEW_PER_PREFIX = "perPrefixView";

    public static class TestEnvironment{
        private CouchbaseSessionFactory _sessionFactory;
        private ICouchbaseBucket _client;

        private ElasticSearchServer _esServer=null;
        private ElasticSearchSessionFactory _esSessionFactory=null;
        private IElasticSearchMapper _esMapper=null;
        private ElasticSearchClient _esClient=null;
        private CouchbaseDCPConnector _connector=null;


        public static class TestElasticSearchMapper implements IElasticSearchMapper{
            private IDocumentInfoMapper _documentInfoMapper;
            public TestElasticSearchMapper(IDocumentInfoMapper mapper){
                _documentInfoMapper = mapper;
            }

            @Override
            public String documentIndexBuilder(String bucketName, String key) {
                return bucketName.toLowerCase();
            }

            @Override
            public String documentIndexBuilder(String bucketName, Class<? extends CouchbaseDocument> clazz) {
                return bucketName.toLowerCase();
            }

            @Override
            public String documentTypeBuilder(String bucketName, String key) {
                try {
                    return documentTypeBuilder(bucketName, _documentInfoMapper.getMappingFromKey(key).classMappingInfo().classInfo());
                }
                catch(MappingNotFoundException e){
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String documentTypeBuilder(String bucketName, Class<? extends CouchbaseDocument> clazz) {
                DocumentDef annot = clazz.getAnnotation(DocumentDef.class);
                if(annot!=null){
                    return annot.domain()+"#"+annot.name()+"#"+annot.version().toLowerCase();
                }
                else{
                    return clazz.getSimpleName().toLowerCase();
                }
            }

            @Override
            public String documentMappingsBuilder(Class<? extends CouchbaseDocument> clazz) {
                //TODO improve for index / type mapping
                return null;
            }
        }


        public enum TestEnvType{
            COUCHBASE,
            COUCHBASE_ELASTICSEARCH;

            public boolean hasElasticSearch(){
                return this == COUCHBASE_ELASTICSEARCH;
            }
        }

        public TestEnvironment(String prefix,TestEnvType type) throws Exception{
            List<String> couchbaseConnectionList;
            String bucketName;
            String bucketPassword;
            if (
                    (System.getenv(COUCHBASE_BUCKET_NAME_ENV_VAR) != null) &&
                            (System.getenv(COUCHBASE_BUCKET_PASSWORD_ENV_VAR) != null) &&
                            (System.getenv(COUCHBASE_DB_URL_ENV_VAR) != null)
                    ) {
                _client = new CouchbaseBucketWrapper(CouchbaseCluster.create(System.getenv(COUCHBASE_DB_URL_ENV_VAR)), System.getenv(COUCHBASE_BUCKET_NAME_ENV_VAR), System.getenv(COUCHBASE_BUCKET_PASSWORD_ENV_VAR), prefix);
                couchbaseConnectionList = Arrays.asList(System.getenv(COUCHBASE_DB_URL_ENV_VAR).split(","));
                bucketName = System.getenv(COUCHBASE_BUCKET_NAME_ENV_VAR);
                bucketPassword = System.getenv(COUCHBASE_BUCKET_PASSWORD_ENV_VAR);
            }
            else {
                _client = new CouchbaseBucketSimulator(prefix+"test", prefix);
                couchbaseConnectionList = Arrays.asList("localhost:8091");
                bucketName = _client.getBucketName();
                bucketPassword = "dummy";

            }
            CouchbaseSessionFactory.Builder sessionBuilder = new CouchbaseSessionFactory.Builder();
            sessionBuilder.getDocumentDaoFactoryBuilder().getUniqueKeyDaoFactoryBuilder().withDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
            _sessionFactory = sessionBuilder.build();

            if(type.hasElasticSearch()){
                _esServer = new ElasticSearchServer(prefix+"ES");
                _esSessionFactory = ElasticSearchSessionFactory.builder().withDocumentInfoMappper(sessionBuilder.getDocumentDaoFactoryBuilder().getDocumentInfoMapper()).build();
                _esMapper = new TestElasticSearchMapper(sessionBuilder.getDocumentDaoFactoryBuilder().getDocumentInfoMapper());
                _esClient = new ElasticSearchClient(_esServer.getClient(),GenericJacksonTranscoder.MAPPER);
                ICouchbaseDCPEnvironment env = DefaultCouchbaseDCPEnvironment.builder().streamName(UUID.randomUUID().toString()).threadPoolSize(1).build();
                ElasticSearchDcpFlowHandler dcpFlowHandler = new ElasticSearchDcpFlowHandler(
                        _esClient,
                        _esMapper,
                        sessionBuilder.getDocumentDaoFactoryBuilder().getDocumentInfoMapper(),
                        true);
                if(_client instanceof CouchbaseBucketSimulator) {
                    _connector = new CouchbaseDCPConnectorSimulator(env,couchbaseConnectionList ,bucketName, bucketPassword, dcpFlowHandler, (CouchbaseBucketSimulator)_client);
                }
                else{
                    _connector = new CouchbaseDCPConnector(env,couchbaseConnectionList,bucketName,bucketPassword,dcpFlowHandler);
                }
            }
        }

        public <TOBJ extends CouchbaseDocument> void addDocumentDao(CouchbaseDocumentDao dao,Class<TOBJ> objClass) throws DuplicateMappedEntryInfoException{
            _sessionFactory.getDocumentDaoFactory().addDao(dao.setClient(_client), new GenericJacksonTranscoder<>(objClass));
            if(_esSessionFactory!=null){
                ElasticSearchDao<TOBJ> elasticSearchDao = new ElasticSearchDao<>(_client.getBucketName(),_esClient ,_esMapper,new GenericJacksonTranscoder<>(objClass));
                _esSessionFactory.getElasticSearchDaoFactory().addDaoForClass(objClass,elasticSearchDao);
            }

        }

        public void addDocumentDao(CouchbaseDocumentDao dao) throws DuplicateMappedEntryInfoException{
            Class<? extends CouchbaseDocument> clazz = dao.getClass().getAnnotation(DaoForClass.class).value();
            addDocumentDao(dao, clazz);
        }

        public Map<String,String> testingUtilsViews(){
            Map<String, String> listViews = new HashMap<>();
            listViews.put(VIEW_PER_PREFIX,
                    "function(doc,meta){\n" +
                            "   var prefix = /^(\\w+)\\$/.exec(meta.id);\n" +
                            "   if(prefix!==null){\n" +
                            "      emit(prefix[1],null);\n" +
                            "    }\n" +
                            "}"
            );
            return listViews;
        }

        public void start() throws StorageException {
            _client.start();
            if(_client.getClass().equals(CouchbaseBucketWrapper.class)){
                Bucket bucket=((CouchbaseBucketWrapper) _client).getBucket();
                DesignDocument designDocument = bucket.bucketManager().getDesignDocument(TESTING_DESIGN_DOC);
                Map<String,String> referenceMap=testingUtilsViews();
                boolean toRebuild = false;
                if((designDocument ==null) || (referenceMap.values().size()!=designDocument.views().size())){
                    toRebuild = true;
                }
                else for(View view:designDocument.views()){
                    if(!referenceMap.containsKey(view.name())){
                        toRebuild = true;
                    }
                    else if(!referenceMap.get(view.name()).equals(view.map())){
                        toRebuild = true;
                    }
                    if(toRebuild) break;
                }

                if(toRebuild){
                    List<View> listView = new ArrayList<>();
                    for(Map.Entry<String,String> entry:referenceMap.entrySet()){
                        listView.add(DefaultView.create(entry.getKey(),entry.getValue()));
                    }
                    DesignDocument newDesignDocument = DesignDocument.create(TESTING_DESIGN_DOC,listView);

                    bucket.bucketManager().upsertDesignDocument(newDesignDocument);
                }
            }
            _sessionFactory.getDocumentDaoFactory().getViewDaoFactory().initAllViews();
            if(_esServer!=null){
                _esServer.start();
                _connector.run();
            }
        }

        public CouchbaseSessionFactory getSessionFactory(){return _sessionFactory;}

        public void shutdown(boolean cleanUp){
            if(cleanUp && _client.getClass().equals(CouchbaseBucketWrapper.class)){
                ViewQuery listQuery = ViewQuery.from(TESTING_DESIGN_DOC,VIEW_PER_PREFIX).key(_client.getPrefix());
                listQuery.stale(Stale.FALSE);
                final Bucket bucket=((CouchbaseBucketWrapper) _client).getBucket();
                bucket.async().query(listQuery).
                        flatMap(
                                AsyncViewResult::rows
                        ).
                        flatMap(
                                aViewRow ->
                                bucket.async().remove(aViewRow.id())
                        ).toBlocking().last();
            }
            _client.shutdown();
            if(_esServer!=null){
                _connector.stop();
                _esServer.stop();
            }
        }

        public ElasticSearchServer getEsServer(){
            return _esServer;
        }

        public ElasticSearchClient getEsClient(){
            return _esClient;
        }
        public ElasticSearchSessionFactory getEsSessionFactory() {
            return _esSessionFactory;
        }

        public void fullEsSync(){
            if(_connector!=null){
                _connector.stop();
                _esClient.syncIndexes();
                _connector.run();
            }
        }
    }
}
