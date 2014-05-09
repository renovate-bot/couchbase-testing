package com.dreameddeath.common.model;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentLink<T extends CouchbaseDocument> extends CouchbaseDocumentElement{
    @DocumentProperty("key")
    private Property<String>    _key=new SynchronizedLinkProperty<String,T>(CouchbaseDocumentLink.this){
        @Override
        protected  String getRealValue(T doc){
            return doc.getKey();
        }
    };
    private Property<T>            _docObject=new ImmutableProperty<T>(null);


    public final String getKey(){ return _key.get();}
    public final void setKey(String key){ _key.set(key); }
    
    public T getLinkedObject(){
        return getLinkedObject(false);
    }
    
    public T getLinkedObject(boolean fromCache){
        if((_docObject.get()==null) && (!fromCache)){
            if(_key==null){
                ///TODO throw an error
            }
            else{
                CouchbaseDocument parentDoc = getParentDocument();
                if((parentDoc!=null) && parentDoc.getSession()!=null){
                    _docObject.set((T)parentDoc.getSession().get(_key.get()));
                }
            }
        }
        return _docObject.get();
    }
    
    public void setLinkedObject(T docObj){ 
        _docObject.set(docObj);
        docObj.addReverseLink(this);
    }
    
    
    public CouchbaseDocumentLink(){}
    public CouchbaseDocumentLink(T targetDoc){
        if(targetDoc.getKey()!=null){
            setKey(targetDoc.getKey());
        }
        setLinkedObject(targetDoc);
    }
    
    public CouchbaseDocumentLink(CouchbaseDocumentLink<T> srcLink){
        setKey(srcLink.getKey());
        setLinkedObject(srcLink.getLinkedObject());
    }
    
    
    @Override
    public boolean equals(Object target){
        if(target==null){
            return false;
        }
        else if(this == target){
            return true;
        }
        else if(target instanceof CouchbaseDocumentLink){
            CouchbaseDocumentLink targetLnk=(CouchbaseDocumentLink) target;
            if((_key!=null) && _key.equals(targetLnk._key)){
                return true;
            }
            else if((_docObject!=null)&& _docObject.equals(targetLnk._docObject)){
                return true;
            }
        }

        return false;
    }
    
    @Override
    public String toString(){
        return "key : "+getKey();
    }
    
    @Override
    public boolean validate(){
        boolean result = super.validate();
        if(_key.get()==null){
            result&=false;
        }
        return result;
    }
}
