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

package model;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.ParentEntity;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.business.model.BusinessDocument;

@DocumentDef(domain="test",name="daoProccessorChild",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "child/",idFormat = "%010d",idPattern = "\\d{10}")
@ParentEntity(c=TestGeneratedDao.class,keyPath = "parent.key",separator = "/")
@Counter(name="cnt",dbName = "cnt",isKeyGen = true)
public class TestGeneratedDaoChild extends BusinessDocument implements ITestDaoChild{
    @DocumentProperty("value")
    public String value;
    @DocumentProperty("parent")
    public TestGeneratedDaoLink parent;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public TestGeneratedDaoLink getParent() {
        return parent;
    }

    public void setParent(TestGeneratedDaoLink parent) {
        this.parent = parent;
    }

    @Override
    public void setParentObjDao(ITestDao parent){
        this.parent = new model.TestGeneratedDaoLink((TestGeneratedDao)parent);
    }
}