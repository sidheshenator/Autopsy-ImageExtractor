/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import java.util.logging.Level;
import org.apache.tika.Tika;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.FileIngestModule;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
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
    private static final int BUFFER_SIZE = 64 * 1024;
    private final byte buffer[] = new byte[BUFFER_SIZE];

    ImageExtractorIngestModule() {
        // currently, settings are not considered.
        // Both doc as well as docx files are processed.
    }

    protected enum SupportedFormats {

        doc,
        docx,
        ppt,
        pptx,
        xls,
        xlsx;
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

    /**
     * This method returns true if the file format is currently supported. Else
     * it returns false.
     *
     * @param abstractFile abstract files which need to be checked if supported.
     * @return This method returns true if the file format is currently
     * supported. Else it returns false.
     */
    private boolean isSupported(AbstractFile abstractFile) {
        try {
            byte buf[];
            int len = abstractFile.read(buffer, 0, BUFFER_SIZE);
            if (len < BUFFER_SIZE) {
                buf = new byte[len];
                System.arraycopy(buffer, 0, buf, 0, len);
            } else {
                buf = buffer;
            }

            String mimetype = tika.detect(buf, abstractFile.getName());

            if (mimetype.equals("application/msword")) {
                abstractFileFormat = SupportedFormats.doc;
                return true;
            }
            if (mimetype.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                abstractFileFormat = SupportedFormats.docx;
                return true;
            }
            if (mimetype.equals("application/vnd.ms-powerpoint")) {
                abstractFileFormat = SupportedFormats.ppt;
                return true;
            }
            if (mimetype.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                abstractFileFormat = SupportedFormats.pptx;
                return true;
            }
            if (mimetype.equals("application/vnd.ms-excel")) {
                abstractFileFormat = SupportedFormats.xls;
                return true;
            }
            if (mimetype.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                abstractFileFormat = SupportedFormats.xlsx;
                return true;
            }

        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Could not read the file header for " + abstractFile.getName(), ex);
        }
        return false;
    }

}
