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

package com.dreameddeath.core.dao;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;

import java.util.Arrays;
import java.util.List;

/**
 * Created by CEAJ8230 on 14/04/2015.
 */
@DaoForClass(TestDoc.class)
public class TestDao extends CouchbaseDocumentWithKeyPatternDao<TestDoc> {
    public static final String TEST_CNT_KEY = "test/cnt";
    public static final String TEST_CNT_KEY_PATTERN = "test/cnt";
    public static final String TEST_KEY_FMT = "test/%010d";
    public static final String TEST_KEY_PATTERN = "test/\\d{10}";

    @Override
    public String getKeyPattern() {
        return TEST_KEY_PATTERN;
    }

    public static class LocalBucketDocument extends BucketDocument<TestDoc> {
        public LocalBucketDocument(TestDoc obj) {
            super(obj);
        }
    }

    @Override
    public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public List<CouchbaseViewDao> generateViewDaos() {
        return Arrays.asList(
                new TestViewDao(this)
        );
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        return Arrays.asList(
                new CouchbaseCounterDao.Builder().withKeyPattern(TEST_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
        );
    }

    @Override
    public TestDoc buildKey(ICouchbaseSession session, TestDoc newObject) throws DaoException, StorageException {
        long result = session.incrCounter(TEST_CNT_KEY, 1);
        newObject.getBaseMeta().setKey(String.format(TEST_KEY_FMT, result));

        return newObject;
    }
}
