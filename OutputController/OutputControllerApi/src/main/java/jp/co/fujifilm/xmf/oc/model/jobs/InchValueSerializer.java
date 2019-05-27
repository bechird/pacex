package jp.co.fujifilm.xmf.oc.model.jobs;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class InchValueSerializer extends JsonSerializer<Double>{

	private static final double POINT_TO_MM_INDEX = 2.8343;

	@Override
	public void serialize(Double inchBValue, JsonGenerator gen, SerializerProvider provider) throws IOException {
		double value = BigDecimal.valueOf(inchBValue / POINT_TO_MM_INDEX)
	    .setScale(2, RoundingMode.HALF_UP)
	    .doubleValue();
		
		gen.writeNumber(value);
	}
	 
		
}
