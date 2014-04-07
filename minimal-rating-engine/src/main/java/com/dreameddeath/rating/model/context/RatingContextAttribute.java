package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RatingContextAttribute{
    private String _code;
    private List<RatingContextAttributeValue> _values=new ArrayList<RatingContextAttributeValue>();
    
    @JsonProperty("code")
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
 
    @JsonProperty("values")
    public List<RatingContextAttributeValue> getValues(){return _values;}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }

 
}