package com.epac.cap.common;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.handler.BatchesByRollOrderingComparator;
import com.epac.cap.handler.JobsByBatchOrderingComparator;
import com.epac.cap.handler.JobsByMachineOrderingComparator;
import com.epac.cap.handler.JobsByRollOrderingComparator;
import com.epac.cap.handler.LogsByChronologicalOrderingComparator;
import com.epac.cap.handler.RollsByMachineOrderingComparator;
import com.epac.cap.handler.SectionsByMachineOrderingComparator;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverBatchJob;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Job;
import com.epac.cap.model.Log;
import com.epac.cap.model.Roll;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class BatchSortedSetDeserializer extends JsonDeserializer<SortedSet<?>> {

		public BatchSortedSetDeserializer() {}
		
		
		@Override
		public SortedSet<?> deserialize(JsonParser parser, DeserializationContext ctx)
				throws IOException, JsonProcessingException {

			SortedSet collection = null;
			List<?> list = null;
			String attributeName = parser.getCurrentName() != null? parser.getCurrentName(): "";

			if(attributeName.endsWith("jobs")){
				list = parser.readValueAs(new TypeReference<List<CoverBatchJob>>() {});
				collection = new  TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());
			}else if(attributeName.endsWith("CoverBatchJob")){
				list = parser.readValueAs(new TypeReference<List<CoverBatchJob>>() {});
				collection = new  TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());
			}else if(attributeName.endsWith("Sections")){
				list = parser.readValueAs(new TypeReference<List<CoverSection>>() {});
				collection = new  TreeSet<CoverSection>(new SectionsByMachineOrderingComparator());
			}
			

			for (Object object : list) {
				if(object instanceof Log && ((Log)object).getCreatedDate() == null)
					((Log)object).setCreatedDate(new Date());
				collection.add(object);
			}

			return collection;
		}

	}