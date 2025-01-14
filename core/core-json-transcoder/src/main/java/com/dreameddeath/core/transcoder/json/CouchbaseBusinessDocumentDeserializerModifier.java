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

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

/**
 * Created by Christophe Jeunesse on 28/11/2014.
 */
public class CouchbaseBusinessDocumentDeserializerModifier extends BeanDeserializerModifier
{
    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                  BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if ((CouchbaseDocument.class.isAssignableFrom(beanDesc.getBeanClass()) ||
                CouchbaseDocumentElement.class.isAssignableFrom(beanDesc.getBeanClass())) &&
                (deserializer instanceof  BeanDeserializer)
                )
        {
            return new CouchbaseBusinessDocumentDeserializer((BeanDeserializer)deserializer);
        }

        return super.modifyDeserializer(config, beanDesc, deserializer);
    }
}
