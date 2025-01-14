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

package com.dreameddeath.core.process.exception;


import com.dreameddeath.core.process.model.AbstractJob;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class JobExecutionException extends Exception {
    AbstractJob.State state;
    AbstractJob job;

    public JobExecutionException(AbstractJob job, AbstractJob.State state, String message) {
        super(message);
        this.job = job;
        this.state = state;
    }

    public JobExecutionException(AbstractJob job, AbstractJob.State state, String message, Throwable e) {
        super(message, e);
        this.job = job;
        this.state = state;
    }

    public JobExecutionException(AbstractJob job, AbstractJob.State state, Throwable e) {
        super(e);
        this.job = job;
        this.state = state;
    }

    public AbstractJob getJob(){ return job;}
    public AbstractJob.State getState(){ return state;}

}
