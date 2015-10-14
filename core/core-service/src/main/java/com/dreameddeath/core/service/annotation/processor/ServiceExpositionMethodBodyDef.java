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

package com.dreameddeath.core.service.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;

/**
 * Created by Christophe Jeunesse on 09/04/2015.
 */
public class ServiceExpositionMethodBodyDef {
    private Type bodyType;
    private String name;
    private ServiceExpositionParamInfo paramInfo;

    //Init from
    public ServiceExpositionMethodBodyDef(String paramName,MethodInfo info){
        bodyType = Type.INPUT_PARAM;
        name = "bodyEntity";
        paramInfo = new ServiceExpositionParamInfo(name,paramName,info);
    }

    public Type getBodyType() {
        return bodyType;
    }

    public String getName() {
        return name;
    }

    public String getImportName() {
        return paramInfo.getImportName();
    }

    public String getClassName(){
        return paramInfo.getClassName();
    }

    public String getGetterString(){
        return paramInfo.getGetterString();
    }

    public String getSetterString(){ return paramInfo.getSetterString();}

    public enum Type{
        INPUT_PARAM,
        GENERATED_INPUT
    }
}

