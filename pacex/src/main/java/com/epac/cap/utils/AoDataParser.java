package com.epac.cap.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.epac.cap.model.Pallette;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AoDataParser {
	
	
	
	public static AoData parse(String jsonString) {
		
		AoData aoDataObject = new AoData();		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			
			JsonNode json = mapper.readTree(jsonString);		
			JsonNode aoData = json.get("aoData");
			
			for (JsonNode fieldNode : aoData) {
		        String fieldName = fieldNode.get("name").asText();
		        
		        if(fieldName.equals("draw")) {
		        	int ajaxRequestId = fieldNode.get("value").asInt();
		        	aoDataObject.setAjaxRequestId(ajaxRequestId);
		        }
		        
		        if(fieldName.equals("start")) {
		        	int startIndex = fieldNode.get("value").asInt();
		        	aoDataObject.setStartIndex(startIndex);
		        }
		        
		        if(fieldName.equals("length")) {
		        	int pageLength = fieldNode.get("value").asInt();
		        	aoDataObject.setPageLength(pageLength);
		        }
		        
		        if(fieldName.equals("search")) {
		        	String generalFilter = fieldNode.get("value").get("value").asText();
		        	aoDataObject.setGeneralFilter(generalFilter);
		        }

		        
		        if(fieldName.equals("columns")) {
		        	JsonNode columns = fieldNode.get("value");
		        	for (JsonNode column : columns) {
		        		String columnName = column.get("data").asText();
		        		aoDataObject.getColumnsNames().add(columnName);
		        		
		        		String columnFilter =column.get("search").get("value").asText();
		        		if(columnFilter != null  && !columnFilter.isEmpty()) {
		        			aoDataObject.getColumnsFilters().put(columnName, columnFilter);
		        		}
		        	}
		        	
		        }
		        
				
		        if(fieldName.equals("order")) {
			        int sortingColumnIndex  = fieldNode.get("value").get(0).get("column").asInt();	
			        aoDataObject.setSortingColumnIndex(sortingColumnIndex);
					String sortingDirection = fieldNode.get("value").get(0).get("dir").asText();
					aoDataObject.setSortingDirection(sortingDirection);
		        }
		        

		        
		    }
			

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aoDataObject;
	}

	
	public static void main(String[] args) {
		String json ="{\"aoData\":[{\"name\":\"draw\",\"value\":18},{\"name\":\"columns\",\"value\":[{\"data\":\"expanded\",\"name\":\"expanded\",\"searchable\":false,\"orderable\":false,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"id\",\"name\":\"id\",\"searchable\":true,\"orderable\":true,\"search\":{\"value\":\"11547\",\"regex\":true}},{\"data\":\"blNumber\",\"name\":\"blNumber\",\"searchable\":true,\"orderable\":true,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"destination\",\"name\":\"destination\",\"searchable\":true,\"orderable\":true,\"search\":{\"value\":\"147745\",\"regex\":true}},{\"data\":\"qtyPcbInPallette\",\"name\":\"qtyPcbInPallette\",\"searchable\":true,\"orderable\":false,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"qtyBookInPallette\",\"name\":\"qtyBookInPallette\",\"searchable\":true,\"orderable\":false,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"statusPallette\",\"name\":\"statusPallette\",\"searchable\":true,\"orderable\":true,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"delivredDate\",\"name\":\"delivredDate\",\"searchable\":true,\"orderable\":true,\"search\":{\"value\":\"\",\"regex\":false}},{\"data\":\"id\",\"name\":\"actions\",\"searchable\":false,\"orderable\":false,\"search\":{\"value\":\"\",\"regex\":false}}]},{\"name\":\"order\",\"value\":[{\"column\":1,\"dir\":\"asc\"}]},{\"name\":\"start\",\"value\":0},{\"name\":\"length\",\"value\":10},{\"name\":\"search\",\"value\":{\"value\":\"allFilter\",\"regex\":false}},{\"name\":\"sRangeSeparator\",\"value\":\"~\"}]}";		
		AoData aoDataObject = parse(json);
		System.out.println(aoDataObject.getColumnsFilters());
		
	}
	
	
	public static Criterion fullSearchCriterionBuilder(String fieldName, String restriction, String convertToClasse, String query) {
		
		
		Criterion criterion = null;
		Object convertedQuery = null;
		
		
		if(convertToClasse.equals("integer")) {
			try { convertedQuery = Integer.valueOf(query); } catch (Exception e) {}
		}

		if(convertToClasse.equals("long")) {
			try { convertedQuery = Long.valueOf(query); } catch (Exception e) {}
		}

		if(convertToClasse.equals("float")) {
			try { convertedQuery = Float.valueOf(query); } catch (Exception e) {}
		}
		
		if(convertToClasse.equals("boolean")) {
			try { convertedQuery = Boolean.valueOf(query); } catch (Exception e) {}
		}

		if(convertToClasse.equals("date")) {
			SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
			try { convertedQuery = parser.parse(query); } catch (Exception e) {}
		}
		
		if(convertedQuery == null)return null;
		
		
		
		
		
		
		
		if(restriction.equals("eq")) {
			criterion = Restrictions.eq(fieldName, convertedQuery);
		}
		
		if(restriction.equals("ilike")) {
			criterion = Restrictions.ilike(fieldName, query, MatchMode.ANYWHERE);
		}		

		
		
		return criterion;
	}
	
}
