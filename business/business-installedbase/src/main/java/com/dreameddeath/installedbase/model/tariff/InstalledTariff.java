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

package com.dreameddeath.installedbase.model.tariff;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledTariff extends InstalledItem<InstalledTariffRevision> {
    /**
     *  discounts : list of discounts attached to the tariff
     */
    @DocumentProperty("discounts")
    private ListProperty<InstalledDiscount> discounts = new ArrayListProperty<InstalledDiscount>(InstalledTariff.this);

    // Discounts Accessors
    public List<InstalledDiscount> getDiscounts() { return discounts.get(); }
    public void setDiscounts(Collection<InstalledDiscount> vals) { discounts.set(vals); }
    public boolean addDiscounts(InstalledDiscount val){ return discounts.add(val); }
    public boolean removeDiscounts(InstalledDiscount val){ return discounts.remove(val); }

}
