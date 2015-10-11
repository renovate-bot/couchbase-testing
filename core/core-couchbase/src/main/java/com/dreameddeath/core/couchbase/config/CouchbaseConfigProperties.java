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

package com.dreameddeath.core.couchbase.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;
import com.dreameddeath.core.config.impl.StringListConfigProperty;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
@ConfigPropertyPackage(name="couchbase",domain = "core",descr = "All common properties for couchbase classes")
public class CouchbaseConfigProperties {
    @ConfigPropertyDoc(
            name="core.couchbase.cluster.{clusterName}.addresses",
            descr = "defines the list of couchbase servers in the cluster (defined by an address)",
            examples = {"192.168.1.1,couchbase.test.com"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringListConfigProperty> COUCHBASE_CLUSTER_ADDRESSES = ConfigPropertyFactory.getTemplateNameConfigProperty(StringListConfigProperty.class,"core.couchbase.cluster.{clusterName}.addresses", (String) null);


    @ConfigPropertyDoc(
            name="core.couchbase.bucket.{bucketName}.cluster",
            descr = "defines the attached couchbase cluster for given bucket",
            examples = {"base_cluster_name","ha_bucket"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_BUCKET_CLUSTER_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"core.couchbase.bucket.{bucketName}.cluster", (String) null);

    @ConfigPropertyDoc(
            name="core.couchbase.bucket.{bucketName}.password",
            descr = "defines the password for given bucket",
            examples = {"pwd1","pass1"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_BUCKET_PASSWORD_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"core.couchbase.bucket.{bucketName}.password", (String) null);

}
