package com.epac.cap.common;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.handler.WFSDataSupportsByIdOrderingComparator;
import com.epac.cap.model.WFSDataSupport;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class WFSDataSupportComparableDeserializer extends JsonDeserializer<SortedSet<WFSDataSupport>> {

		public WFSDataSupportComparableDeserializer() {}
		
		
		@Override
		public SortedSet<WFSDataSupport> deserialize(JsonParser parser, DeserializationContext ctx)
				throws IOException, JsonProcessingException {

			SortedSet<WFSDataSupport> collection = new TreeSet<WFSDataSupport>(
					new WFSDataSupportsByIdOrderingComparator());

			List<WFSDataSupport> list = parser.readValueAs(new TypeReference<List<WFSDataSupport>>() {
			});

			for (WFSDataSupport t : list) {
				collection.add(t);
			}

			return collection;
		}

	}