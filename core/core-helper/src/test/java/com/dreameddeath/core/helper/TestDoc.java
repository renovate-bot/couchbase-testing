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

package com.dreameddeath.core.helper;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
@DocumentDef(domain = "testDao")
public class TestDoc extends CouchbaseDocument {
    @DocumentProperty("strVal")
    public String strVal;

    @DocumentProperty("intVal")
    public Integer intVal;

    @DocumentProperty("longVal")
    public Long longVal;

    @DocumentProperty("doubleVal")
    public Double doubleVal;

    @DocumentProperty("boolVal")
    public Boolean boolVal;

    @DocumentProperty("arrayVal")
    public List<TestDocSubElem> arrayVal;

    public static class TestDocSubElem {
        @DocumentProperty("longVal")
        public Long longVal;
    }

}
