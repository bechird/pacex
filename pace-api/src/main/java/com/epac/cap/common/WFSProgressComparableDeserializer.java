package com.epac.cap.common;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.handler.ProgressesBySequenceRankingComparator;
import com.epac.cap.model.WFSProgress;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class WFSProgressComparableDeserializer extends JsonDeserializer<SortedSet<WFSProgress>> {

	public WFSProgressComparableDeserializer() {
	}

	@Override
	public SortedSet<WFSProgress> deserialize(JsonParser parser, DeserializationContext ctx)
			throws IOException, JsonProcessingException {

		SortedSet<WFSProgress> collection = new TreeSet<WFSProgress>(new ProgressesBySequenceRankingComparator());

		List<WFSProgress> list = parser.readValueAs(new TypeReference<List<WFSProgress>>() {
		});

		for (WFSProgress t : list) {
			collection.add(t);
		}

		return collection;
	}

}
