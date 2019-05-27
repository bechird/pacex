package com.epac.cap.common;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.handler.PNLTemplateLinesPerOrderingComparator;
import com.epac.cap.model.PNLTemplateLine;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PNLTemplateLineComparableDeserializer extends JsonDeserializer<SortedSet<PNLTemplateLine>> {

		public PNLTemplateLineComparableDeserializer() {}
		
		
		@Override
		public SortedSet<PNLTemplateLine> deserialize(JsonParser parser, DeserializationContext ctx)
				throws IOException, JsonProcessingException {

			SortedSet<PNLTemplateLine> collection = new TreeSet<PNLTemplateLine>(
					new PNLTemplateLinesPerOrderingComparator());

			List<PNLTemplateLine> list = parser.readValueAs(new TypeReference<List<PNLTemplateLine>>() {
			});

			for (PNLTemplateLine t : list) {
				collection.add(t);
			}

			return collection;
		}

	}