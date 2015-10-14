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

package com.dreameddeath.core.couchbase.exception;

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;


/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DocumentStorageException extends StorageException {
    BaseCouchbaseDocument doc;

    public DocumentStorageException(BaseCouchbaseDocument doc,String message) {
        super(message);
        this.doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc,String message,Throwable e) {
        super(message,e);
        this.doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc,Throwable e) {
        super(e);
        this.doc = doc;
    }

    public DocumentStorageException(BaseCouchbaseDocument doc) {
        this.doc = doc;
    }

    @Override
    public String getMessage(){
        return super.getMessage() + "\n The doc was " + doc;
    }
}
