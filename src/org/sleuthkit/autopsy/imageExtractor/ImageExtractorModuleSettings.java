/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import java.util.LinkedHashMap;
import java.util.Map;
import org.sleuthkit.autopsy.imageExtractor.ImageExtractorIngestModule.SupportedFormats;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

public class ImageExtractorModuleSettings implements IngestModuleIngestJobSettings {

    private static final long serialVersionUID = 1L;
    SupportedFormats supportedFormats;
    Map<String, Boolean> supportedFormatsState = new LinkedHashMap<String, Boolean>();
    
    /**
     * This constructor initializes the Map supportedFormatsState with available SupportedFormats.
     * By default, all supportedFormats are enabled.
     */
    ImageExtractorModuleSettings() {
        for (ImageExtractorIngestModule.SupportedFormats supportedFormat : ImageExtractorIngestModule.SupportedFormats.values()) {
            supportedFormatsState.put(supportedFormat.toString(), Boolean.TRUE);
        }
    }
    
    void setSupportedFormatState(String name, boolean flag) {
        supportedFormatsState.put(name, flag);
    }
    
    boolean getSupportedFormatState(String name) {
        return supportedFormatsState.get(name);
    }
    
    @Override
    public long getVersionNumber() {
        return serialVersionUID;
    }
    
}
