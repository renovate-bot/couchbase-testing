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

package com.dreameddeath.party.model.base;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class PartyRole extends BaseCouchbaseDocumentElement {
    @DocumentProperty("uid")
    private ImmutableProperty<String> uid=new ImmutableProperty<String>(PartyRole.this);
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<DateTime>(PartyRole.this);
    @DocumentProperty("endDate")
    private Property<DateTime> endDate= new StandardProperty<DateTime>(PartyRole.this);


    public String getUid() { return uid.get(); }
    public void setUid(String uid) { uid.set(uid); }

    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime startDate) { startDate.set(startDate); }

    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime endDate) { endDate.set(endDate); }


}
