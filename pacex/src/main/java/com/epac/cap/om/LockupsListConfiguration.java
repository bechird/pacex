package com.epac.cap.om;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.epac.cap.handler.LookupHandler;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.om.api.utils.LogUtils;

@Configuration
public class LockupsListConfiguration {    
	
	@Autowired
	private LookupHandler lookupHandler;
	
    @Bean
    public Map<Class<?>, List<? extends LookupItem>> lookupsList() {
    	Map<Class<?>, List<? extends LookupItem>> lookups = new HashMap<Class<?>, List<? extends LookupItem>>();
    	
		try {
			List<BindingType> bindingType = lookupHandler
					.readAll((Class<BindingType>) Class.forName("com.epac.cap.model.BindingType"));
			LogUtils.info("lookups has ("+(bindingType != null? bindingType.size(): 0)+") BindingType");
			
			lookups.put(BindingType.class, bindingType);

			List<Lamination> lamination = lookupHandler
					.readAll((Class<Lamination>) Class.forName("com.epac.cap.model.Lamination"));
			lookups.put(Lamination.class, lamination);
			LogUtils.info("lookups has ("+(lamination != null? lamination.size(): 0)+") Lamination");
			List<PaperType> paperType = lookupHandler
					.readAll((Class<PaperType>) Class.forName("com.epac.cap.model.PaperType"));
			lookups.put(PaperType.class, paperType);
			LogUtils.info("lookups has ("+(paperType != null? paperType.size(): 0)+") PaperType");
			List<WFSProductionStatus> wFSProductionStatus = lookupHandler
					.readAll((Class<WFSProductionStatus>) Class.forName("com.epac.cap.model.WFSProductionStatus"));
			lookups.put(WFSProductionStatus.class, wFSProductionStatus);
			LogUtils.info("lookups has ("+(wFSProductionStatus != null? wFSProductionStatus.size(): 0)+") WFSProductionStatus");
			List<PartCategory> partCategory = lookupHandler
					.readAll((Class<PartCategory>) Class.forName("com.epac.cap.model.PartCategory"));
			lookups.put(PartCategory.class, partCategory);
			LogUtils.info("lookups has ("+(partCategory != null? partCategory.size(): 0)+") PartCategory");
		} catch (Exception e) {
			LogUtils.error("Error occured while initializing lookups", e);
		}
		
        return lookups;
    }
    
    
	
	
	
}