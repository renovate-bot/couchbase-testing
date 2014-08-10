package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask{
    @DocumentProperty("docKey")
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocId(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocId(String docKey){_docKey.set(docKey); return this;}
    public T getDocument(){
        return (T)this.getParentJob().getSession().get(_docKey.get());
    }


    @Override
    public final boolean cleanup(){
        getDocument().cleanupAttachedTaskRef(this);
        getDocument().save();
        return false;
    }

    @Override
    public final boolean process(){
        CouchbaseDocumentAttachedTaskRef reference = getDocument().getAttachedTaskRef(this);
        if(reference==null){
            processDocument();
            CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
            attachedTaskRef.setJobKey(getParentJob().getKey());
            attachedTaskRef.setTaskId(this.getUid());
            getDocument().addAttachedTaskRef(attachedTaskRef);
            getDocument().save();
            return true;
        }
        return false;
    }

    protected abstract void processDocument();
}