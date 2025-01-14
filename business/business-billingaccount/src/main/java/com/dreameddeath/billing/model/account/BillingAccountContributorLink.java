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

package com.dreameddeath.billing.model.account;

import com.dreameddeath.billing.model.account.BillingAccountContributor.ContributorType;
import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.business.model.property.impl.SynchronizedLinkProperty;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 25/11/2014.
 */
public class BillingAccountContributorLink extends BusinessDocumentLink<BillingAccountContributor> {
    /**
     *  type : Type of contributor
     */
    @DocumentProperty("type")
    private Property<ContributorType> type = new SynchronizedLinkProperty<ContributorType,BillingAccountContributor>(BillingAccountContributorLink.this){
        @Override
        protected  ContributorType getRealValue(BillingAccountContributor doc){
            return doc.getContributorType();
        }
    };

    // type accessors
    public BillingAccountContributor.ContributorType getType() { return type.get(); }
    public void setType(BillingAccountContributor.ContributorType val) { type.set(val); }


    public BillingAccountContributorLink(){}
    public BillingAccountContributorLink (BillingAccountContributor contributor){
        super(contributor);
    }
    public BillingAccountContributorLink(BillingAccountContributorLink srcLink){
        super(srcLink);
    }

}
