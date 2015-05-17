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

package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseItemFee extends BillingInstalledBaseItem {
    /**
     *  tariffId : Instance of the tariff of the installed base corresponding to the billingItem
     */
    @DocumentProperty("tariffId")
    private Property<String> _tariffId = new StandardProperty<String>(BillingInstalledBaseItemFee.this);
    /**
     *  discountsIds : List of discount items applicable
     */
    @DocumentProperty("discountsIds")
    private ListProperty<Long> _discountsIds = new ArrayListProperty<Long>(BillingInstalledBaseItemFee.this);


    // tariffId accessors
    public String getTariffId() { return _tariffId.get(); }
    public void setTariffId(String val) { _tariffId.set(val); }

    // DiscountsIds Accessors
    public List<Long> getDiscountsIds() { return _discountsIds.get(); }
    public void setDiscountsIds(Collection<Long> vals) { _discountsIds.set(vals); }
    public boolean addDiscountsIds(Long val){ return _discountsIds.add(val); }
    public boolean removeDiscountsIds(Long val){ return _discountsIds.remove(val); }
}
