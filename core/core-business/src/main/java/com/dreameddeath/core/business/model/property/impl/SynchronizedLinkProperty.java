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

package com.dreameddeath.core.business.model.property.impl;

import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.impl.StandardProperty;

public abstract class SynchronizedLinkProperty<T,TDOC extends CouchbaseDocument> extends StandardProperty<T> {
    BusinessDocumentLink<TDOC> parentLink;

    public SynchronizedLinkProperty(BusinessDocumentLink<TDOC> parentLink){
        super(parentLink);
        parentLink.addChildSynchronizedProperty(this);
        this.parentLink=parentLink;
    }

    protected abstract T getRealValue(TDOC doc);

    @Override
    public final T get(){
        if(parentLink.getLinkedObjectFromCache()!=null){
            set(getRealValue(parentLink.getLinkedObjectFromCache()));
        }
        return super.get();
    }

    public void sync(){
        get();
    }
}
