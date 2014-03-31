package com.dreamddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.io.Externalizable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import net.spy.memcached.transcoders.Transcoder;


import com.dreameddeath.common.storage.BinarySerializer;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;
import com.dreameddeath.rating.storage.*;
import com.dreameddeath.rating.model.context.*;

public class CouchbaseConnection {
     public static class StringSerializer implements BinarySerializer<String>{
        public byte[] serialize(String str){ return str.getBytes(); }
        public String deserialize(byte[] input){ return new String(input); }
     }
     
     public static class StringCdr extends GenericCdr<String,String>{
        private static StringSerializer _serializer = new StringSerializer();
        
        public StringCdr(String uid){ super(uid); }
        protected BinarySerializer<String> getCdrDataSerializer(){ return _serializer; }
        protected BinarySerializer<String> getCdrRatingSerializer(){ return _serializer; }
     }
     
      public static class StringCdrBucket extends GenericCdrsBucket<StringCdr>{
        private static Transcoder<GenericCdrsBucket<StringCdr>> _tc = new StringCdrRatingTrancoder();
        
        @Override
        public Transcoder<GenericCdrsBucket<StringCdr>> getTranscoder(){ return _tc; }
        public StringCdrBucket(GenericCdrsBucket.DocumentType docType){ super(docType); }
        public StringCdrBucket(String key,Integer origDbSize,DocumentType documentType){ super(key,origDbSize,documentType); }
     }
     
     public static class StringCdrRatingTrancoder extends GenericCdrsBucketTranscoder<StringCdr>{
        @Override
        protected StringCdr genericCdrBuilder(String uid){ return new StringCdr(uid); }
        
        @Override
        protected StringCdrBucket genericCdrBucketBuilder(GenericCdrsBucket.DocumentType docType){ return new StringCdrBucket(docType); }
     }
     public static void main(String[] args) throws Exception {
        // (Subset) of nodes in the cluster to establish a connection
        List<URI> hosts = Arrays.asList(
          new URI("http://127.0.0.1:8091/pools")
        );
     
        // Name of the Bucket to connect to
        String bucket = "test";
     
        // Password of the bucket (empty) string if none
        String password = "adminuser";
        
        // Connect to the Cluster
        CouchbaseClientWrapper client = new CouchbaseClientWrapper(new CouchbaseClient(hosts, bucket, password));
            
        try{
            StandardRatingContext ratingCtxt = new StandardRatingContext();
            ratingCtxt.setUid("ratCxt/1");
            
            client.set(ratingCtxt);
            
            StringCdrBucket cdrsBucket = new StringCdrBucket(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL);
            cdrsBucket.setKey("my-first-document2");
            for(int i=0;i<5;++i){
                StringCdr cdr = new StringCdr("CDR_"+i);
                cdr.setCdrData("BaseCdrContent_"+i);
                cdrsBucket.addCdr(cdr);
            }
            
            // Store a Document
            client.set(cdrsBucket).get();
            GenericCdrsBucket<StringCdr> unpackedCdrsMap = client.get("my-first-document2",cdrsBucket.getTranscoder());
            System.out.println("Result :\n"+unpackedCdrsMap.toString());
            
            StringCdrBucket newCdrsBucket = new StringCdrBucket(unpackedCdrsMap.getKey(),unpackedCdrsMap.getDbDocSize(),GenericCdrsBucket.DocumentType.CDRS_BUCKET_PARTIAL_WITH_CHECKSUM);
            int pos=0;
            for(StringCdr cdr : unpackedCdrsMap.getCdrs()){
                if(pos%2==0){
                    StringCdr updatedCdr = new StringCdr(cdr.getUid());
                    updatedCdr.addRatingResult("RatingContext_"+cdr.getUid());
                    updatedCdr.addRatingResult("RatingContext2_"+cdr.getUid());
                    newCdrsBucket.addCdr(updatedCdr);
                }
                pos++;
            }
            
            client.append(newCdrsBucket).get();
            unpackedCdrsMap = client.get("my-first-document2", newCdrsBucket.getTranscoder());
            System.out.println("Result :\n"+unpackedCdrsMap.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        client.shutdown();
  }

}