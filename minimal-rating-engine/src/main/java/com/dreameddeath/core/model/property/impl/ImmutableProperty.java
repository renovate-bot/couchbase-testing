package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

public class ImmutableProperty<T> extends AbstractProperty<T> {

    public ImmutableProperty(BaseCouchbaseDocumentElement parentElement){
        super(parentElement);
    }
    public ImmutableProperty(BaseCouchbaseDocumentElement parentElement, T defaultValue){
        super(parentElement,defaultValue);
    }


    @Override
    public final boolean set(T value) {
        if(!equalsValue(value) && (_value!=null)){
            throw new UnsupportedOperationException("Cannot reassign value <"+_value+"> with newValue <"+_value+">");
        }
        return super.set(value);
    }
}