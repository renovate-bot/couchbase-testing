package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.annotation.Validate;
import com.dreameddeath.core.exception.DuplicateTaskException;
import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
@JsonTypeInfo(use=Id.MINIMAL_CLASS,include=As.PROPERTY, property="@c")
public abstract class AbstractJob<TREQ extends RawCouchbaseDocumentElement,TRES extends RawCouchbaseDocumentElement> extends CouchbaseDocument {
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractJob.this,UUID.randomUUID());
    @DocumentProperty(value = "state",getter = "getJobState",setter = "setJobState")
    private Property<State> _state=new StandardProperty<State>(AbstractJob.this, State.NEW);
    @DocumentProperty("tasks") @Validate
    private ListProperty<AbstractTask> _taskList = new ArrayListProperty<AbstractTask>(AbstractJob.this);
    @DocumentProperty("lastRunError")
    private Property<String> _errorName=new StandardProperty<String>(AbstractJob.this);
    /**
     *  request : The request content for this job
     */
    @DocumentProperty("request") @NotNull @Validate
    private Property<TREQ> _request = new StandardProperty<TREQ>(AbstractJob.this);
    /**
     *  result : The result content for this job
     */
    @DocumentProperty("result")
    private Property<TRES> _result = new StandardProperty<TRES>(AbstractJob.this);

    public AbstractJob(TREQ request,TRES result){
        _request.set(request);
        _result.set(result);
    }

    public AbstractJob(TREQ request){
        _request.set(request);
        _result.set(newResult());
    }
    public AbstractJob(){
        _request.set(newRequest());
        _result.set(newResult());
    }

    public abstract TREQ newRequest();
    public abstract TRES newResult();

    public String getLastRunError(){return _errorName.get();}
    public void setLastRunError(String errorName){_errorName.set(errorName);}

    public State getJobState() { return _state.get(); }
    public void setJobState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }
    // uid accessors
    public UUID getUid() { return _uid.get(); }
    public void setUid(UUID uid) { _uid.set(uid); }
    // result accessors
    public TRES getResult() { return _result.get(); }
    public void setResult(TRES val) { _result.set(val); }

    // request accessors
    public TREQ getRequest() { return _request.get(); }
    public void setRequest(TREQ val) { _request.set(val); }

    public List<AbstractTask> getTasks() { return _taskList.get();}
    //should not be used direclty (only used for Jackson)
    public void setTasks(Collection<AbstractTask> tasks) {
        _taskList.set(tasks);
    }

    public AbstractTask getTask(String id) {
        for(AbstractTask task :_taskList){
            if(id.equals(task.getUid())){ return task; }
        }
        return null;
    }

    public AbstractTask getTask(Integer pos) {
        return _taskList.get(pos);
    }
    public <T extends AbstractTask> T getTask(Integer pos,Class<T> clazz) {return (T)getTask(pos);}
    public <T extends AbstractTask> T getTask(String id, Class<T>clazz) {
        return (T)getTask(id);
    }


    public <T extends AbstractTask> T addTask(T task,Collection<String> prerequisiteTasks) throws DuplicateTaskException{
        if(task.getUid()==null){
            task.setUid(buildTaskId(task));
        }
        if(getTask(task.getUid())!=null){
            if(task!=getTask(task.getUid())) {
                throw new DuplicateTaskException(task,this);
            }
            else{
                for(String preReq:prerequisiteTasks){
                    task.addDependency(preReq);
                }
            }
        }
        else {
            task.setDependency(prerequisiteTasks);
            _taskList.add(task);
        }
        return task;
    }

    public <T extends AbstractTask> T addTask(T task) throws DuplicateTaskException{
        return addTask(task,(Collection<String>)Collections.EMPTY_LIST);
    }

    public <T extends AbstractTask> T addTask(T task,String preRequisiteTaskUid) throws DuplicateTaskException{
        List<String> preRequisiteTaskList= new ArrayList<String>(1);
        preRequisiteTaskList.add(preRequisiteTaskUid);
        return addTask(task,preRequisiteTaskList);
    }

    public <T extends AbstractTask> T  addTask(T task,AbstractTask preRequisiteTask) throws DuplicateTaskException{
        return addTask(task,preRequisiteTask.getUid());
    }

    public String buildTaskId(AbstractTask task){
        return String.format("%010d", _taskList.size());
    }

    public List<AbstractTask> getPendingTasks() {
        List<AbstractTask> pendingTasks=new ArrayList<AbstractTask>();
        for(AbstractTask task:_taskList){
            if(! task.isDone()){
                pendingTasks.add(task);
            }
        }
        return pendingTasks;
    }

    public AbstractTask getNextExecutableTask(){
        for(AbstractTask task:_taskList){
            if(! task.isDone()){
                List<String> preRequisiteList = new ArrayList<String>();
                for(String uid:task.getDependency()){
                    if(!getTask(uid).isDone()){
                        preRequisiteList.add(uid);
                    }
                }
                if(preRequisiteList.size()==0){
                    //TODO maybe log warning/debug message
                    return task;
                }
            }
        }
        return null;
    }

    public static enum State{
        UNKNOWN,
        NEW, //Just created
        INITIALIZED, //Init done
        PREPROCESSED,    //Preparation done
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE//Cleaning done
    }


}