/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.openide.util.Exceptions;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.imageExtractor.ImageExtractorIngestModule.SupportedFormats;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.ModuleContentEvent;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;
import org.sleuthkit.datamodel.TskCoreException;

public class ImageExtractor {

    private Case currentCase;
    private FileManager fileManager;
    private IngestServices services;
    private String moduleDirRelative; //relative to the case, to store in db
    private String moduleDirAbsolute; //absolute, to extract to
    private static final Logger logger = Logger.getLogger(ImageExtractor.class.getName());

    ImageExtractor() {

        this.currentCase = Case.getCurrentCase();
        this.fileManager = Case.getCurrentCase().getServices().getFileManager();
        this.services = IngestServices.getInstance();
        this.moduleDirRelative = Case.getModulesOutputDirRelPath() + File.separator + ImageExtractorModuleFactory.getModuleName();
        this.moduleDirAbsolute = currentCase.getModulesOutputDirAbsPath() + File.separator + ImageExtractorModuleFactory.getModuleName();
        File extractionDirectory = new File(moduleDirAbsolute);
        if (!extractionDirectory.exists()) {
            try {
                extractionDirectory.mkdirs();
            } catch (SecurityException ex) {
                logger.log(Level.SEVERE, "Error initializing output dir: " + moduleDirAbsolute, ex); //NON-NLS
                services.postMessage(IngestMessage.createErrorMessage(ImageExtractorModuleFactory.getModuleName(), "Error initializing", "Error initializing output dir: " + moduleDirAbsolute));
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * This method selects the appropriate process of extracting images from
     * files using POI classes. Once the images have been extracted, the method
     * adds them to the DB and fires a ModuleContentEvent. ModuleContent Event
     * is not fired if the no images were extracted from the processed file.
     *
     * @param supportedFormat The format of the abstractFile as determined by
     * tika.detect()
     * @param abstractFile The abstract file to be processed.
     * @return The number of images extracted from the processed abstractFile.
     */
    protected int extractImage(SupportedFormats supportedFormat, AbstractFile abstractFile) throws Exception {
        // switchcase for different supported formats
        // process abstractFile according to the format by calling appropriate methods.

        List<ExtractedImage> extractedImages;
        switch (supportedFormat) {
            case doc:
                extractedImages = extractImagesFromDoc(abstractFile);
                break;
            case docx:
                extractedImages = extractImagesFromDocx(abstractFile);
                break;
            default:
                logger.log(Level.WARNING, "No image extraction attempted for {0}", abstractFile.getName());
                throw new Exception("No image extraction attempted for " + abstractFile.getName());
        }

        // the common task of adding abstractFile to derivedfiles is performed.
        for (ExtractedImage extractedImage : extractedImages) {
            try {
                fileManager.addDerivedFile(extractedImage.getFileName(), extractedImage.getLocalPath(), extractedImage.getSize(),
                        extractedImage.getCtime(), extractedImage.getCrtime(), extractedImage.getAtime(), extractedImage.getAtime(),
                        true, abstractFile, null, ImageExtractorModuleFactory.getModuleName(), null, null);
            } catch (TskCoreException ex) {
                logger.log(Level.WARNING, "Error adding derived files to the DB", ex);
                throw new Exception("Error adding derived files to the DB");
            }
        }
        if (!extractedImages.isEmpty()) {
            services.fireModuleContentEvent(new ModuleContentEvent(abstractFile));
        }
        return extractedImages.size();
    }

    private List<ExtractedImage> extractImagesFromDoc(AbstractFile af) {
        // TODO check for BBArtifact ENCRYPTION_DETECTED? Might be detected elsewhere...?
        List<ExtractedImage> listOfExtractedImages = new ArrayList<ExtractedImage>();
        String parentFileName = getUniqueName(af);
        HWPFDocument docA = null;
        try {
            docA = new HWPFDocument(new ReadContentInputStream(af));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "HWPFDocument container could not be instantiated while reading " + af.getName(), ex);
            return null;
        }
        PicturesTable pictureTable = docA.getPicturesTable();
        List<org.apache.poi.hwpf.usermodel.Picture> listOfAllPictures = pictureTable.getAllPictures();
        for (org.apache.poi.hwpf.usermodel.Picture picture : listOfAllPictures) {
            FileOutputStream fos = null;
            String fileName = picture.suggestFullFileName();
            try {
                String outputPath = moduleDirAbsolute + File.separator + parentFileName;
                File outputFilePath = new File(outputPath);
                if (!outputFilePath.exists()) {
                    try {
                        outputFilePath.mkdirs();
                    } catch (SecurityException ex) {
                        logger.log(Level.WARNING, "Unable to create the output path to write the extracted image",ex);
                    }
                }
                fos = new FileOutputStream(moduleDirAbsolute + File.separator + parentFileName + File.separator + fileName);
            } catch (FileNotFoundException ex) {
                logger.log(Level.WARNING, "Invalid path provided for image extraction", ex);
                continue;
            }
            try {
                fos.write(picture.getContent());
                fos.close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not write to the provided location", ex);
                continue;
            }
            // TODO Extract more info from the Picture viz ctime, crtime, atime, mtime
//            String fileRelativePath = "/" + moduleDirRelative + "/" + parentFileName + "/" + fileName;
            String fileRelativePath = File.separator + moduleDirRelative + File.separator + parentFileName + File.separator + fileName;
            long size = picture.getSize();
            ExtractedImage extractedimage = new ExtractedImage(fileName, fileRelativePath, size, af);
            listOfExtractedImages.add(extractedimage);
        }

        return listOfExtractedImages;
    }

    private List<ExtractedImage> extractImagesFromDocx(AbstractFile af) {
        // check for BBArtifact ENCRYPTION_DETECTED? Might be detected elsewhere...?
        // TODO check for BBArtifact ENCRYPTION_DETECTED? Might be detected elsewhere...?
        List<ExtractedImage> listOfExtractedImages = new ArrayList<ExtractedImage>();
        String parentFileName = getUniqueName(af);
        XWPFDocument docxA = null;
        try {
            docxA = new XWPFDocument(new ReadContentInputStream(af));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "XWPFDocument container could not be instantiated while reading " + af.getName(), ex);
            return null;
        }
        List<XWPFPictureData> listOfAllPictures = docxA.getAllPictures();
        for (XWPFPictureData xwpfPicture : listOfAllPictures) {
            String fileName = xwpfPicture.getFileName();
            FileOutputStream fos = null;
            try {
                String outputPath = moduleDirAbsolute + File.separator + parentFileName;
                File outputFilePath = new File(outputPath);
                if (!outputFilePath.exists()) {
                    try {
                        outputFilePath.mkdirs();
                    } catch (SecurityException ex) {
                        logger.log(Level.WARNING, "Unable to create the output path to write the extracted image",ex);
                    }
                }
                fos = new FileOutputStream(moduleDirAbsolute + File.separator + parentFileName + File.separator + fileName);
            } catch (FileNotFoundException ex) {
                logger.log(Level.WARNING, "Invalid path provided for image extraction", ex);
                continue;
            }
            try {
                fos.write(xwpfPicture.getData());
                fos.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            String fileRelativePath = File.separator + moduleDirRelative + File.separator + parentFileName + File.separator + fileName;
            long size = xwpfPicture.getData().length;
            ExtractedImage extractedimage = new ExtractedImage(fileName, fileRelativePath, size, af);
            listOfExtractedImages.add(extractedimage);
        }
        return listOfExtractedImages;
    }

    private String getUniqueName(AbstractFile af) {
        return af.getName() + "_" + af.getId();
    }

    private static class ExtractedImage {
        //String fileName, String localPath, long size, long ctime, long crtime, 
        //long atime, long mtime, boolean isFile, AbstractFile parentFile, String rederiveDetails, String toolName, String toolVersion, String otherDetails

        private String fileName;
        private String localPath;
        private long size;
        private long ctime;
        private long crtime;
        private long atime;
        private long mtime;
        private AbstractFile parentFile;

        ExtractedImage(String fileName, String localPath, long size, AbstractFile parentPath) {
            this.fileName = fileName;
            this.localPath = localPath;
            this.size = size;
            this.parentFile = parentPath;
            this.ctime = 0;
            this.crtime = 0;
            this.atime = 0;
            this.mtime = 0;
        }

        ExtractedImage(String fileName, String localPath, long size, long ctime, long crtime, long atime, long mtime, AbstractFile parentPath) {
            this.fileName = fileName;
            this.localPath = localPath;
            this.size = size;
            this.ctime = ctime;
            this.crtime = crtime;
            this.atime = atime;
            this.mtime = mtime;
            this.parentFile = parentPath;
        }

        /**
         * @return the fileName
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * @return the localPath
         */
        public String getLocalPath() {
            return localPath;
        }

        /**
         * @return the size
         */
        public long getSize() {
            return size;
        }

        /**
         * @return the ctime
         */
        public long getCtime() {
            return ctime;
        }

        /**
         * @return the crtime
         */
        public long getCrtime() {
            return crtime;
        }

        /**
         * @return the atime
         */
        public long getAtime() {
            return atime;
        }

        /**
         * @return the mtime
         */
        public long getMtime() {
            return mtime;
        }

        /**
         * @return the parentFile
         */
        public AbstractFile getParentFile() {
            return parentFile;
        }
    }
}
