/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.coreutils.Version;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;

/**
 *
 * @author sidhesh
 */
@ServiceProvider(service = IngestModuleFactory.class)
public class ImageExtractorModuleFactory extends IngestModuleFactoryAdapter {

    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }
    /**
     * Gets the module display name.
     *
     * @return The name string.
     */
    static String getModuleName() {
        return NbBundle.getMessage(ImageExtractorIngestModule.class,
                "ImageExtractorIngestModule.moduleName.text");
    }

    @Override
    public String getModuleDescription() {
        return NbBundle.getMessage(ImageExtractorIngestModule.class,
                "ImageExtractorIngestModule.moduleDesc.text");
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public String getModuleVersionNumber() {
        return Version.getVersion();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new ImageExtractorModuleSettings();
    }
    
    @Override
    public boolean hasIngestJobSettingsPanel() {
        return true;
    }
    
    
    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        assert settings instanceof ImageExtractorModuleSettings;
        if (!(settings instanceof ImageExtractorModuleSettings)) {
            throw new IllegalArgumentException("");
        }
        return new ImageExtractorIngestJobSettingsPanel((ImageExtractorModuleSettings) settings);
        
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean isFileIngestModuleFactory() {
        return true;
    }
    
    @Override
    public FileIngestModule createFileIngestModule(IngestModuleIngestJobSettings settings) {
        assert settings instanceof ImageExtractorModuleSettings;
        if (!(settings instanceof ImageExtractorModuleSettings)) {
            throw new IllegalArgumentException("");
        }
        return new ImageExtractorIngestModule((ImageExtractorModuleSettings) settings);
    }
}
