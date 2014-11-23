package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by ceaj8230 on 13/09/2014.
 */
public class DocumentNotFoundException extends StorageException {
    private RawCouchbaseDocument _doc;
    private String _key;
    public DocumentNotFoundException(String key,String message){
        super(message);
        _key=key;
    }
    public DocumentNotFoundException(RawCouchbaseDocument doc, String message){
        super(message);
        _doc=doc;
    }

    @Override
    public String getMessage(){
        StringBuilder builder = new StringBuilder(super.getMessage());
        if(_doc!=null){ builder.append(" The doc was <").append(_doc).append(">");}
        if(_key!=null){ builder.append(" The key was <").append(_key).append(">");}
        return builder.toString();
    }
}