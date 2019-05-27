package com.epac.cap.common;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.handler.JobsByMachineOrderingComparator;
import com.epac.cap.handler.JobsByRollOrderingComparator;
import com.epac.cap.handler.LogsByChronologicalOrderingComparator;
import com.epac.cap.handler.RollsByMachineOrderingComparator;
import com.epac.cap.model.Job;
import com.epac.cap.model.Log;
import com.epac.cap.model.Roll;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class MachineSortedSetDeserializer extends JsonDeserializer<SortedSet<?>> {

		public MachineSortedSetDeserializer() {}
		
		
		@Override
		public SortedSet<?> deserialize(JsonParser parser, DeserializationContext ctx)
				throws IOException, JsonProcessingException {

			SortedSet collection = null;
			List<?> list = null;
			String attributeName = parser.getCurrentName() != null? parser.getCurrentName(): "";

			if(attributeName.endsWith("logs")){
				 list = parser.readValueAs(new TypeReference<List<Log>>() {});
				 collection = new  TreeSet<Log>(new LogsByChronologicalOrderingComparator());
			}else if(attributeName.endsWith("Rolls")){
				list = parser.readValueAs(new TypeReference<List<Roll>>() {});
				collection = new  TreeSet<Roll>(new RollsByMachineOrderingComparator());
			}else if(attributeName.endsWith("Jobs")){
				list = parser.readValueAs(new TypeReference<List<Job>>() {});
				collection = new  TreeSet<Job>(new JobsByMachineOrderingComparator());
			}else if(attributeName.endsWith("jobs")){
				list = parser.readValueAs(new TypeReference<List<Job>>() {});
				collection = new  TreeSet<Job>(new JobsByRollOrderingComparator());
			}
			

			for (Object object : list) {
				if(object instanceof Log && ((Log)object).getCreatedDate() == null)
					((Log)object).setCreatedDate(new Date());
				collection.add(object);
			}

			return collection;
		}

	}