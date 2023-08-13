package org.ww.ai.backup;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.List;



public class LocalStorageBackupWriter extends AbstractBackupWriter {

    @Override
    public boolean writeBackup(List<RenderResultLightWeight> renderResults) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        for (RenderResultLightWeight renderResultLw : renderResults) {
            String xml = xmlMapper.writeValueAsString(renderResultLw);
            Log.d("XML", "" + xml);
        }
        return true;
    }

    @Override
    public boolean init() {
        return false;
    }
}
