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

package com.dreameddeath.core.dao.exception;

/**
 * Created by Christophe Jeunesse on 21/09/2014.
 */
public class DocumentAccessException extends DaoException {
    private String key;
    public DocumentAccessException(String key){this.key=key;}
    public DocumentAccessException(String key,Throwable e){
        super(e);
        this.key=key;
    }
    public DocumentAccessException(String key,String message, Throwable e){
        super(message,e);
        this.key=key;
    }
    public DocumentAccessException(String key,String message){
        super(message);
        this.key = key;
    }

    public String getKey(){ return key;}

    @Override
    public String getMessage(){
        StringBuilder builder=new StringBuilder(super.getMessage());
        return builder.append("\nThe key was <").append(key).append(">").toString();
    }
}
