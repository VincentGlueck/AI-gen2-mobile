package org.ww.ai.data;

import java.util.Map;

public interface AdditionalSettingsIF {

	Map<String, String> getAdditonalSettingsMap();
	
	void addAdditionalSetting(String key, String value);
	
}
