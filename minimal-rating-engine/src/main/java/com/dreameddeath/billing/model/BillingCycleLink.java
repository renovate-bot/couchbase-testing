package com.dreameddeath.billing.model;

import java.util.Collection;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentLink;

import com.dreameddeath.rating.model.context.AbstractRatingContext;

public class BillingCycleLink extends CouchbaseDocumentLink<BillingCycle>{
    @JsonProperty("startDate")
    private DateTime _startDate;
	@JsonProperty("endDate")
    private DateTime _endDate;
	
    public DateTime getStartDate() { return _startDate; }
    public void setStartDate(DateTime startDate) { _startDate=startDate; }
    
    public DateTime getEndDate() { return _endDate; }
    public void setEndDate(DateTime endDate) { _endDate=endDate; }
    
    public BillingCycleLink(){}
    
    public BillingCycleLink(BillingCycle billCycle){
        super(billCycle);
        setStartDate(billCycle.getStartDate());
        setEndDate(billCycle.getEndDate());
    }
    
    public BillingCycleLink(BillingCycleLink srcLink){
        super(srcLink);
        setStartDate(srcLink.getStartDate());
        setEndDate(srcLink.getEndDate());
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,_startDate,_endDate);
    }
    
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="startDate : "+getStartDate()+",\n";
        result+="endDate : "+getEndDate()+",\n";
        result+="}\n";
        return result;
    }
    
}