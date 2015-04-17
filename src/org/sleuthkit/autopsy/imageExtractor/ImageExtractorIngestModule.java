/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import java.io.IOException;
import java.util.logging.Level;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 *
 * @author sidhesh
 */
public class ImageExtractorIngestModule implements FileIngestModule {

    private static final Logger logger = Logger.getLogger(ImageExtractorIngestModule.class.getName());
    private ImageExtractor imageExtractor;
    private Tika tika;
    private SupportedFormats abstractFileFormat;

    ImageExtractorIngestModule() {
        // currently, settings are not considered.
        // Both doc as well as docx files are processed.
    }

    protected enum SupportedFormats {
        doc,
        docx;
        // TODO Expand to support more formats
    }

    @Override
    public void startUp(IngestJobContext ijc) throws IngestModuleException {
        this.imageExtractor = new ImageExtractor();
        this.tika = new Tika();
    }

    @Override
    public ProcessResult process(AbstractFile abstractFile) {
        // CONFIMRED that POI 3.11 (the one distributed with tika-1.7) does not have any know vulnerabilities.

        if (abstractFile.getType().equals(TskData.TSK_DB_FILES_TYPE_ENUM.UNALLOC_BLOCKS)) {
            return ProcessResult.OK;
        }

        if (abstractFile.getKnown().equals(TskData.FileKnown.KNOWN)) {
            return ProcessResult.OK;
        }

        if (abstractFile.isFile() == false || !isSupported(abstractFile)) {
            return ProcessResult.OK;
        }
        try {
            if (abstractFile.hasChildren()) {
                logger.log(Level.INFO, "File already has been processed, skipping: {0}", abstractFile.getName()); //NON-NLS
                return ProcessResult.OK;
            }
        } catch (TskCoreException ex) {
            logger.log(Level.INFO, "File already has been processed, skipping: {0}", abstractFile.getName()); //NON-NLS
            return ProcessResult.OK;
        }

        logger.log(Level.INFO, "Processing with archive extractor: {0}", abstractFile.getName()); //NON-NLS
        try {
            imageExtractor.extractImage(abstractFileFormat, abstractFile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Image extraction failed for the file " + abstractFile.getName(), ex);
        }

        return ProcessResult.OK;
    }

    @Override
    public void shutDown() {
        
    }

    boolean isSupported(AbstractFile abstractFile) {
        String format;
        try {
            format = tika.detect(new ReadContentInputStream(abstractFile));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Tika is unable to detect the format of the file " + abstractFile.getName(), ex);
            return false;
        }
        // CHECK Should have been "application/msword"
        // format = (String) "application/octet-stream"	 when IS is wrapped with TikaFormatStream.cast(IS)
//        if (format.equals("application/x-tika-msoffice")) {
        if (format.equals("application/x-tika-msoffice")) {
            abstractFileFormat = SupportedFormats.doc;
            return true;
        }
        // CHECK Should have been "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        // format = (String) "application/octet-stream"	 when IS is wrapped with TikaFormatStream
//        if (format.equals("application/zip")) {
        if (format.equals("application/zip")) {
            abstractFileFormat = SupportedFormats.docx;
            return true;
        }
        return false;
    }

}
