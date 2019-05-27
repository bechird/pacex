
package com.epac.cap.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryBook implements Serializable{
        
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        private static long SEQUENCE_NUMBER_REF;
        
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
        private long sequence;
        private long updated;
        
        @OneToOne
        private CoverBatchJob job;

        private String isbn;
        
        
        
        @Enumerated(EnumType.STRING)
        private DeliveryStatus status;
        
        public DeliveryBook() {
        }
        
        @PrePersist
        public void setSequence(){
                sequence = ++SEQUENCE_NUMBER_REF;
                Logger.getLogger(this.getClass()).debug("PrePersist: assigned sequence number: "+sequence);
        }
        public long getId() {
                return id;
        }
        public void setId(long id) {
                this.id = id;
        }
        public long getSequence() {
                return sequence;
        }
        public void setSequence(long sequence) {
                this.sequence = sequence;
        }
       
        
       
        
        public CoverBatchJob getJob() {
			return job;
		}

		public void setJob(CoverBatchJob job) {
			this.job = job;
			this.isbn = job.getJob().getPartIsbn();
		}

		public DeliveryStatus getStatus() {
                return status;
        }
        
        public void setStatus(DeliveryStatus status) {
                this.status = status;
                this.updated = System.currentTimeMillis();
        }
        
        public String getIsbn() {
                return isbn;
        }
        
        public long getUpdated() {
                return updated;
        }
        
        public static void setSequenceRef(long sequence){
                // sequence number has format yyyymmddxxxx
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String today = format.format(new Date());
                long newSequence = Long.parseLong(today);
                newSequence = newSequence * 10000;
                if(sequence < newSequence){
                        sequence = newSequence;
                }
                
                SEQUENCE_NUMBER_REF = sequence;
        }
        
}

 