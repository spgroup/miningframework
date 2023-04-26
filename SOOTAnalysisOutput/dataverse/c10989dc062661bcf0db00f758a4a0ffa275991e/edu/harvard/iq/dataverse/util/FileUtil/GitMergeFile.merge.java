package edu.harvard.iq.dataverse.util;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.DataFile.ChecksumType;
import edu.harvard.iq.dataverse.DataFileServiceBean;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.TermsOfUseAndAccess;
import edu.harvard.iq.dataverse.dataaccess.DataAccess;
import edu.harvard.iq.dataverse.dataaccess.ImageThumbConverter;
import edu.harvard.iq.dataverse.dataaccess.S3AccessIO;
import edu.harvard.iq.dataverse.dataset.DatasetThumbnail;
import edu.harvard.iq.dataverse.datasetutility.FileExceedsMaxSizeException;
import static edu.harvard.iq.dataverse.datasetutility.FileSizeChecker.bytesToHumanReadable;
import edu.harvard.iq.dataverse.ingest.IngestReport;
import edu.harvard.iq.dataverse.ingest.IngestServiceBean;
import edu.harvard.iq.dataverse.ingest.IngestServiceShapefileHelper;
import edu.harvard.iq.dataverse.ingest.IngestableDataChecker;
import edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatDoc;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.HTML_H1;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.HTML_TABLE_HDR;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatTitle;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatTable;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatTableCell;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatLink;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatTableCellAlignRight;
import static edu.harvard.iq.dataverse.util.xml.html.HtmlFormatUtil.formatTableRow;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.ejb.EJBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.FileUtils;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FilenameUtils;
import com.amazonaws.AmazonServiceException;
import edu.harvard.iq.dataverse.dataaccess.DataAccessOption;
import edu.harvard.iq.dataverse.dataaccess.StorageIO;
import edu.harvard.iq.dataverse.datasetutility.FileSizeChecker;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class FileUtil implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(FileUtil.class.getCanonicalName());

    private static final String[] TABULAR_DATA_FORMAT_SET = { "POR", "SAV", "DTA", "RDA" };

    private static Map<String, String> STATISTICAL_FILE_EXTENSION = new HashMap<String, String>();

    static {
        STATISTICAL_FILE_EXTENSION.put("do", "application/x-stata-syntax");
        STATISTICAL_FILE_EXTENSION.put("sas", "application/x-sas-syntax");
        STATISTICAL_FILE_EXTENSION.put("sps", "application/x-spss-syntax");
        STATISTICAL_FILE_EXTENSION.put("csv", "text/csv");
        STATISTICAL_FILE_EXTENSION.put("tsv", "text/tsv");
    }

    private static MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();

    public static final String MIME_TYPE_STATA = "application/x-stata";

    public static final String MIME_TYPE_STATA13 = "application/x-stata-13";

    public static final String MIME_TYPE_STATA14 = "application/x-stata-14";

    public static final String MIME_TYPE_STATA15 = "application/x-stata-15";

    public static final String MIME_TYPE_RDATA = "application/x-rlang-transport";

    public static final String MIME_TYPE_CSV = "text/csv";

    public static final String MIME_TYPE_CSV_ALT = "text/comma-separated-values";

    public static final String MIME_TYPE_TSV = "text/tsv";

    public static final String MIME_TYPE_TSV_ALT = "text/tab-separated-values";

    public static final String MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String MIME_TYPE_SPSS_SAV = "application/x-spss-sav";

    public static final String MIME_TYPE_SPSS_POR = "application/x-spss-por";

    public static final String MIME_TYPE_FITS = "application/fits";

    public static final String MIME_TYPE_ZIP = "application/zip";

    public static final String MIME_TYPE_FITSIMAGE = "image/fits";

    public static final String MIME_TYPE_GEO_SHAPE = "application/zipped-shapefile";

    public static final String MIME_TYPE_UNDETERMINED_DEFAULT = "application/octet-stream";

    public static final String MIME_TYPE_UNDETERMINED_BINARY = "application/binary";

    public static final String SAVED_ORIGINAL_FILENAME_EXTENSION = "orig";

    public static final String MIME_TYPE_INGESTED_FILE = "text/tab-separated-values";

    public static final String FILE_THUMBNAIL_CLASS_AUDIO = "audio";

    public static final String FILE_THUMBNAIL_CLASS_CODE = "code";

    public static final String FILE_THUMBNAIL_CLASS_DOCUMENT = "document";

    public static final String FILE_THUMBNAIL_CLASS_ASTRO = "astro";

    public static final String FILE_THUMBNAIL_CLASS_IMAGE = "image";

    public static final String FILE_THUMBNAIL_CLASS_NETWORK = "network";

    public static final String FILE_THUMBNAIL_CLASS_GEOSHAPE = "geodata";

    public static final String FILE_THUMBNAIL_CLASS_TABULAR = "tabular";

    public static final String FILE_THUMBNAIL_CLASS_VIDEO = "video";

    public static final String FILE_THUMBNAIL_CLASS_PACKAGE = "package";

    public static final String FILE_THUMBNAIL_CLASS_OTHER = "other";

    private static final String FILE_FACET_CLASS_ARCHIVE = "Archive";

    private static final String FILE_FACET_CLASS_AUDIO = "Audio";

    private static final String FILE_FACET_CLASS_CODE = "Code";

    private static final String FILE_FACET_CLASS_DATA = "Data";

    private static final String FILE_FACET_CLASS_DOCUMENT = "Document";

    private static final String FILE_FACET_CLASS_ASTRO = "FITS";

    private static final String FILE_FACET_CLASS_IMAGE = "Image";

    private static final String FILE_FACET_CLASS_NETWORK = "Network Data";

    private static final String FILE_FACET_CLASS_GEOSHAPE = "Shape";

    private static final String FILE_FACET_CLASS_TABULAR = "Tabular Data";

    private static final String FILE_FACET_CLASS_VIDEO = "Video";

    private static final String FILE_FACET_CLASS_TEXT = "Text";

    private static final String FILE_FACET_CLASS_OTHER = "Other";

    private static final String FILE_FACET_CLASS_UNKNOWN = "Unknown";

    public static Map<String, String> FILE_THUMBNAIL_CLASSES = new HashMap<String, String>();

    static {
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_VIDEO, FILE_THUMBNAIL_CLASS_VIDEO);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_AUDIO, FILE_THUMBNAIL_CLASS_AUDIO);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_CODE, FILE_THUMBNAIL_CLASS_CODE);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_DATA, FILE_THUMBNAIL_CLASS_TABULAR);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_NETWORK, FILE_THUMBNAIL_CLASS_NETWORK);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_ASTRO, FILE_THUMBNAIL_CLASS_ASTRO);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_IMAGE, FILE_THUMBNAIL_CLASS_IMAGE);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_DOCUMENT, FILE_THUMBNAIL_CLASS_DOCUMENT);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_GEOSHAPE, FILE_THUMBNAIL_CLASS_GEOSHAPE);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_TABULAR, FILE_THUMBNAIL_CLASS_TABULAR);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_TEXT, FILE_THUMBNAIL_CLASS_DOCUMENT);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_OTHER, FILE_THUMBNAIL_CLASS_OTHER);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_UNKNOWN, FILE_THUMBNAIL_CLASS_OTHER);
        FILE_THUMBNAIL_CLASSES.put(FILE_FACET_CLASS_ARCHIVE, FILE_THUMBNAIL_CLASS_PACKAGE);
    }

    private static final String FILE_LIST_DATE_FORMAT = "d-MMMM-yyyy HH:mm";

    public static String DATA_URI_SCHEME = "data:image/png;base64,";

    public FileUtil() {
    }

    public static void copyFile(File inputFile, File outputFile) throws IOException {
        FileChannel in = null;
        WritableByteChannel out = null;
        try {
            in = new FileInputStream(inputFile).getChannel();
            out = new FileOutputStream(outputFile).getChannel();
            long bytesPerIteration = 50000;
            long start = 0;
            while (start < in.size()) {
                in.transferTo(start, bytesPerIteration, out);
                start += bytesPerIteration;
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static String getFileExtension(String fileName) {
        String ext = null;
        if (fileName.lastIndexOf(".") != -1) {
            ext = (fileName.substring(fileName.lastIndexOf(".") + 1)).toLowerCase();
        }
        return ext;
    }

    public static String replaceExtension(String originalName) {
        return replaceExtension(originalName, "tab");
    }

    public static String replaceExtension(String originalName, String newExtension) {
        int extensionIndex = originalName.lastIndexOf(".");
        if (extensionIndex != -1) {
            return originalName.substring(0, extensionIndex) + "." + newExtension;
        } else {
            return originalName + "." + newExtension;
        }
    }

    public static String getUserFriendlyFileType(DataFile dataFile) {
        String fileType = dataFile.getContentType();
        if (fileType != null) {
            if (fileType.equalsIgnoreCase(ShapefileHandler.SHAPEFILE_FILE_TYPE)) {
                return ShapefileHandler.SHAPEFILE_FILE_TYPE_FRIENDLY_NAME;
            }
            if (fileType.contains(";")) {
                fileType = fileType.substring(0, fileType.indexOf(";"));
            }
            try {
                return BundleUtil.getStringFromPropertyFile(fileType, "MimeTypeDisplay");
            } catch (MissingResourceException e) {
                return fileType;
            }
        }
        return fileType;
    }

    public static String getIndexableFacetFileType(DataFile dataFile) {
        String fileType = getFileType(dataFile);
        try {
            return BundleUtil.getStringFromDefaultPropertyFile(fileType, "MimeTypeFacets");
        } catch (MissingResourceException ex) {
            if (!StringUtil.isEmpty(fileType)) {
                String typeClass = fileType.split("/")[0];
                if ("application".equalsIgnoreCase(typeClass)) {
                    return FILE_FACET_CLASS_OTHER;
                }
                return Character.toUpperCase(typeClass.charAt(0)) + typeClass.substring(1);
            } else {
                return null;
            }
        }
    }

    public static String getFileType(DataFile dataFile) {
        String fileType = dataFile.getContentType();
        if (!StringUtil.isEmpty(fileType)) {
            if (fileType.contains(";")) {
                fileType = fileType.substring(0, fileType.indexOf(";"));
            }
            return fileType;
        } else {
            return "application/octet-stream";
        }
    }

    public static String getFacetFileType(DataFile dataFile) {
        String fileType = getFileType(dataFile);
        try {
            return BundleUtil.getStringFromPropertyFile(fileType, "MimeTypeFacets");
        } catch (MissingResourceException ex) {
            if (!StringUtil.isEmpty(fileType)) {
                String typeClass = fileType.split("/")[0];
                if ("application".equalsIgnoreCase(typeClass)) {
                    return FILE_FACET_CLASS_OTHER;
                }
                return Character.toUpperCase(typeClass.charAt(0)) + typeClass.substring(1);
            } else {
                return null;
            }
        }
    }

    public static String getUserFriendlyOriginalType(DataFile dataFile) {
        if (!dataFile.isTabularData()) {
            return null;
        }
        String fileType = dataFile.getOriginalFileFormat();
        if (fileType != null && !fileType.equals("")) {
            if (fileType.contains(";")) {
                fileType = fileType.substring(0, fileType.indexOf(";"));
            }
            try {
                return BundleUtil.getStringFromPropertyFile(fileType, "MimeTypeDisplay");
            } catch (MissingResourceException e) {
                return fileType;
            }
        }
        return "UNKNOWN";
    }

    private static String determineContentType(File fileObject) {
        if (fileObject == null) {
            return null;
        }
        String contentType;
        try {
            contentType = determineFileType(fileObject, fileObject.getName());
        } catch (Exception ex) {
            logger.warning("FileUtil.determineFileType failed for file with name: " + fileObject.getName());
            contentType = null;
        }
        if ((contentType == null) || (contentType.equals(""))) {
            contentType = MIME_TYPE_UNDETERMINED_DEFAULT;
        }
        return contentType;
    }

    public static String retestIngestableFileType(File file, String fileType) {
        IngestableDataChecker tabChecker = new IngestableDataChecker(TABULAR_DATA_FORMAT_SET);
        String newType = tabChecker.detectTabularDataFormat(file);
        return newType != null ? newType : fileType;
    }

    public static String determineFileType(File f, String fileName) throws IOException {
        String fileType = null;
        String fileExtension = getFileExtension(fileName);
        logger.fine("Attempting to identify potential tabular data files;");
        IngestableDataChecker tabChk = new IngestableDataChecker(TABULAR_DATA_FORMAT_SET);
        fileType = tabChk.detectTabularDataFormat(f);
        logger.fine("determineFileType: tabular data checker found " + fileType);
        if (fileType == null) {
            if (isGraphMLFile(f)) {
                fileType = "text/xml-graphml";
            } else if (isFITSFile(f) || (fileExtension != null && fileExtension.equalsIgnoreCase("fits"))) {
                fileType = "application/fits";
            }
        }
        if (fileType == null) {
            JhoveFileType jw = new JhoveFileType();
            String mimeType = jw.getFileMimeType(f);
            if (mimeType != null) {
                fileType = mimeType;
            }
        }
        if (fileExtension != null) {
            logger.fine("fileExtension=" + fileExtension);
            if (fileType == null || fileType.startsWith("text/plain") || "application/octet-stream".equals(fileType)) {
                if (fileType != null && fileType.startsWith("text/plain") && STATISTICAL_FILE_EXTENSION.containsKey(fileExtension)) {
                    fileType = STATISTICAL_FILE_EXTENSION.get(fileExtension);
                } else {
                    fileType = determineFileTypeByExtension(fileName);
                }
                logger.fine("mime type recognized by extension: " + fileType);
            }
        } else {
            logger.fine("fileExtension is null");
        }
        if ("application/x-gzip".equals(fileType)) {
            logger.fine("we'll run additional checks on this gzipped file.");
            FileInputStream gzippedIn = new FileInputStream(f);
            InputStream uncompressedIn = null;
            try {
                uncompressedIn = new GZIPInputStream(gzippedIn);
                if (isFITSFile(uncompressedIn)) {
                    fileType = "application/fits-gzipped";
                }
            } catch (IOException ioex) {
                if (uncompressedIn != null) {
                    try {
                        uncompressedIn.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        if ("application/zip".equals(fileType)) {
            ShapefileHandler shp_handler = new ShapefileHandler(new FileInputStream(f));
            if (shp_handler.containsShapefile()) {
                fileType = ShapefileHandler.SHAPEFILE_FILE_TYPE;
            }
        }
        logger.fine("returning fileType " + fileType);
        return fileType;
    }

    public static String determineFileTypeByExtension(String fileName) {
        String mimetypesFileTypeMapResult = MIME_TYPE_MAP.getContentType(fileName);
        logger.fine("MimetypesFileTypeMap type by extension, for " + fileName + ": " + mimetypesFileTypeMapResult);
        if (mimetypesFileTypeMapResult != null) {
            if ("application/octet-stream".equals(mimetypesFileTypeMapResult)) {
                return lookupFileTypeFromPropertiesFile(fileName);
            } else {
                return mimetypesFileTypeMapResult;
            }
        } else {
            return null;
        }
    }

    public static String lookupFileTypeFromPropertiesFile(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        String propertyFileName = "MimeTypeDetectionByFileExtension";
        String propertyFileNameOnDisk = propertyFileName + ".properties";
        try {
            logger.fine("checking " + propertyFileNameOnDisk + " for file extension " + fileExtension);
            return BundleUtil.getStringFromPropertyFile(fileExtension, propertyFileName);
        } catch (MissingResourceException ex) {
            logger.info(fileExtension + " is a file extension Dataverse doesn't know about. Consider adding it to the " + propertyFileNameOnDisk + " file.");
            return null;
        }
    }

    private static boolean isFITSFile(File file) {
        BufferedInputStream ins = null;
        try {
            ins = new BufferedInputStream(new FileInputStream(file));
            return isFITSFile(ins);
        } catch (IOException ex) {
        }
        return false;
    }

    private static boolean isFITSFile(InputStream ins) {
        boolean isFITS = false;
        int magicWordLength = 6;
        String magicWord = "SIMPLE";
        try {
            byte[] b = new byte[magicWordLength];
            logger.fine("attempting to read " + magicWordLength + " bytes from the FITS format candidate stream.");
            if (ins.read(b, 0, magicWordLength) != magicWordLength) {
                throw new IOException();
            }
            if (magicWord.equals(new String(b))) {
                logger.fine("yes, this is FITS file!");
                isFITS = true;
            }
        } catch (IOException ex) {
            isFITS = false;
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (Exception e) {
                }
            }
        }
        return isFITS;
    }

    private static boolean isGraphMLFile(File file) {
        boolean isGraphML = false;
        logger.fine("begin isGraphMLFile()");
        try {
            FileReader fileReader = new FileReader(file);
            javax.xml.stream.XMLInputFactory xmlif = javax.xml.stream.XMLInputFactory.newInstance();
            xmlif.setProperty("javax.xml.stream.isCoalescing", java.lang.Boolean.TRUE);
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(fileReader);
            for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next()) {
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (xmlr.getLocalName().equals("graphml")) {
                        String schema = xmlr.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
                        logger.fine("schema = " + schema);
                        if (schema != null && schema.contains("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd")) {
                            logger.fine("graphML is true");
                            isGraphML = true;
                        }
                    }
                    break;
                }
            }
        } catch (XMLStreamException e) {
            logger.fine("XML error - this is not a valid graphML file.");
            isGraphML = false;
        } catch (IOException e) {
            throw new EJBException(e);
        }
        logger.fine("end isGraphML()");
        return isGraphML;
    }

    public static String calculateChecksum(String datafile, ChecksumType checksumType) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(datafile);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return FileUtil.calculateChecksum(fis, checksumType);
    }

    public static String calculateChecksum(InputStream in, ChecksumType checksumType) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(checksumType.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] dataBytes = new byte[1024];
        int nread;
        try {
            while ((nread = in.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return checksumDigestToString(md.digest());
    }

    public static String calculateChecksum(byte[] dataBytes, ChecksumType checksumType) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(checksumType.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(dataBytes);
        return checksumDigestToString(md.digest());
    }

    public static String checksumDigestToString(byte[] digestBytes) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String generateOriginalExtension(String fileType) {
        if (fileType.equalsIgnoreCase("application/x-spss-sav")) {
            return ".sav";
        } else if (fileType.equalsIgnoreCase("application/x-spss-por")) {
            return ".por";
        } else if (fileType.toLowerCase().startsWith("application/x-stata")) {
            return ".dta";
        } else if (fileType.equalsIgnoreCase("application/x-dvn-csvspss-zip")) {
            return ".zip";
        } else if (fileType.equalsIgnoreCase("application/x-dvn-tabddi-zip")) {
            return ".zip";
        } else if (fileType.equalsIgnoreCase("application/x-rlang-transport")) {
            return ".RData";
        } else if (fileType.equalsIgnoreCase("text/csv") || fileType.equalsIgnoreCase("text/comma-separated-values")) {
            return ".csv";
        } else if (fileType.equalsIgnoreCase("text/tsv") || fileType.equalsIgnoreCase("text/tab-separated-values")) {
            return ".tsv";
        } else if (fileType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return ".xlsx";
        }
        return "";
    }

    public static List<DataFile> createDataFiles(DatasetVersion version, InputStream inputStream, String fileName, String suppliedContentType, String newStorageIdentifier, String newCheckSum, SystemConfig systemConfig) throws IOException {
        ChecksumType checkSumType = DataFile.ChecksumType.MD5;
        if (newStorageIdentifier == null) {
            checkSumType = systemConfig.getFileFixityChecksumAlgorithm();
        }
        return createDataFiles(version, inputStream, fileName, suppliedContentType, newStorageIdentifier, newCheckSum, checkSumType, systemConfig);
    }

    public static List<DataFile> createDataFiles(DatasetVersion version, InputStream inputStream, String fileName, String suppliedContentType, String newStorageIdentifier, String newCheckSum, ChecksumType newCheckSumType, SystemConfig systemConfig) throws IOException {
        List<DataFile> datafiles = new ArrayList<>();
        if (newCheckSumType == null) {
            newCheckSumType = systemConfig.getFileFixityChecksumAlgorithm();
        }
        String warningMessage = null;
        Path tempFile = null;
        Long fileSizeLimit = systemConfig.getMaxFileUploadSizeForStore(version.getDataset().getEffectiveStorageDriverId());
        String finalType = null;
        if (newStorageIdentifier == null) {
            if (getFilesTempDirectory() != null) {
                tempFile = Files.createTempFile(Paths.get(getFilesTempDirectory()), "tmp", "upload");
                logger.fine("Will attempt to save the file as: " + tempFile.toString());
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Long fileSize = tempFile.toFile().length();
                if (fileSizeLimit != null && fileSize > fileSizeLimit) {
                    try {
                        tempFile.toFile().delete();
                    } catch (Exception ex) {
                    }
                    throw new IOException(MessageFormat.format(BundleUtil.getStringFromBundle("file.addreplace.error.file_exceeds_limit"), bytesToHumanReadable(fileSize), bytesToHumanReadable(fileSizeLimit)));
                }
            } else {
                throw new IOException("Temp directory is not configured.");
            }
            logger.fine("mime type supplied: " + suppliedContentType);
            String recognizedType = null;
            try {
                recognizedType = determineFileType(tempFile.toFile(), fileName);
                logger.fine("File utility recognized the file as " + recognizedType);
                if (recognizedType != null && !recognizedType.equals("")) {
                    if (useRecognizedType(suppliedContentType, recognizedType)) {
                        finalType = recognizedType;
                    }
                }
            } catch (Exception ex) {
                logger.warning("Failed to run the file utility mime type check on file " + fileName);
            }
            if (finalType == null) {
                finalType = (suppliedContentType == null || suppliedContentType.equals("")) ? MIME_TYPE_UNDETERMINED_DEFAULT : suppliedContentType;
            }
            if (finalType.equals("application/fits-gzipped")) {
                InputStream uncompressedIn = null;
                String finalFileName = fileName;
                if (fileName != null && fileName.matches(".*\\.gz$")) {
                    finalFileName = fileName.replaceAll("\\.gz$", "");
                }
                DataFile datafile = null;
                try {
                    uncompressedIn = new GZIPInputStream(new FileInputStream(tempFile.toFile()));
                    File unZippedTempFile = saveInputStreamInTempFile(uncompressedIn, fileSizeLimit);
                    datafile = createSingleDataFile(version, unZippedTempFile, finalFileName, MIME_TYPE_UNDETERMINED_DEFAULT, systemConfig.getFileFixityChecksumAlgorithm());
                } catch (IOException | FileExceedsMaxSizeException ioex) {
                    datafile = null;
                } finally {
                    if (uncompressedIn != null) {
                        try {
                            uncompressedIn.close();
                        } catch (IOException e) {
                        }
                    }
                }
                if (datafile != null) {
                    try {
                        tempFile.toFile().delete();
                    } catch (SecurityException ex) {
                        logger.warning("Failed to delete temporary file " + tempFile.toString());
                    }
                    datafiles.add(datafile);
                    return datafiles;
                }
            } else if (finalType.equals("application/zip")) {
                ZipInputStream unZippedIn = null;
                ZipEntry zipEntry = null;
                int fileNumberLimit = systemConfig.getZipUploadFilesLimit();
                try {
                    Charset charset = null;
                    if (charset != null) {
                        unZippedIn = new ZipInputStream(new FileInputStream(tempFile.toFile()), charset);
                    } else {
                        unZippedIn = new ZipInputStream(new FileInputStream(tempFile.toFile()));
                    }
                    while (true) {
                        try {
                            zipEntry = unZippedIn.getNextEntry();
                        } catch (IllegalArgumentException iaex) {
                            warningMessage = "Failed to unpack Zip file. (Unknown Character Set used in a file name?) Saving the file as is.";
                            logger.warning(warningMessage);
                            throw new IOException();
                        }
                        if (zipEntry == null) {
                            break;
                        }
                        if (!zipEntry.isDirectory()) {
                            if (datafiles.size() > fileNumberLimit) {
                                logger.warning("Zip upload - too many files.");
                                warningMessage = "The number of files in the zip archive is over the limit (" + fileNumberLimit + "); please upload a zip archive with fewer files, if you want them to be ingested " + "as individual DataFiles.";
                                throw new IOException();
                            }
                            String fileEntryName = zipEntry.getName();
                            logger.fine("ZipEntry, file: " + fileEntryName);
                            if (fileEntryName != null && !fileEntryName.equals("")) {
                                String shortName = fileEntryName.replaceFirst("^.*[\\/]", "");
                                if (!shortName.startsWith("._") && !shortName.startsWith(".DS_Store") && !"".equals(shortName)) {
                                    File unZippedTempFile = saveInputStreamInTempFile(unZippedIn, fileSizeLimit);
                                    DataFile datafile = createSingleDataFile(version, unZippedTempFile, null, shortName, MIME_TYPE_UNDETERMINED_DEFAULT, systemConfig.getFileFixityChecksumAlgorithm(), null, false);
                                    if (!fileEntryName.equals(shortName)) {
                                        String directoryName = fileEntryName.replaceFirst("[\\\\/][\\\\/]*[^\\\\/]*$", "");
                                        directoryName = StringUtil.sanitizeFileDirectory(directoryName, true);
                                        if (!StringUtil.isEmpty(directoryName)) {
                                            logger.fine("setting the directory label to " + directoryName);
                                            datafile.getFileMetadata().setDirectoryLabel(directoryName);
                                        }
                                    }
                                    if (datafile != null) {
                                        String tempFileName = getFilesTempDirectory() + "/" + datafile.getStorageIdentifier();
                                        try {
                                            recognizedType = determineFileType(new File(tempFileName), shortName);
                                            logger.fine("File utility recognized unzipped file as " + recognizedType);
                                            if (recognizedType != null && !recognizedType.equals("")) {
                                                datafile.setContentType(recognizedType);
                                            }
                                        } catch (Exception ex) {
                                            logger.warning("Failed to run the file utility mime type check on file " + fileName);
                                        }
                                        datafiles.add(datafile);
                                    }
                                }
                            }
                        }
                        unZippedIn.closeEntry();
                    }
                } catch (IOException ioex) {
                    logger.warning("Unzipping failed; rolling back to saving the file as is.");
                    if (warningMessage == null) {
                        warningMessage = BundleUtil.getStringFromBundle("file.addreplace.warning.unzip.failed");
                    }
                    datafiles.clear();
                } catch (FileExceedsMaxSizeException femsx) {
                    logger.warning("One of the unzipped files exceeds the size limit; resorting to saving the file as is. " + femsx.getMessage());
                    warningMessage = BundleUtil.getStringFromBundle("file.addreplace.warning.unzip.failed.size", Arrays.asList(FileSizeChecker.bytesToHumanReadable(fileSizeLimit)));
                    datafiles.clear();
                } finally {
                    if (unZippedIn != null) {
                        try {
                            unZippedIn.close();
                        } catch (Exception zEx) {
                        }
                    }
                }
                if (datafiles.size() > 0) {
                    try {
                        Files.delete(tempFile);
                    } catch (IOException ioex) {
                        logger.warning("Could not remove temp file " + tempFile.getFileName().toString());
                    }
                    return datafiles;
                }
            } else if (finalType.equalsIgnoreCase(ShapefileHandler.SHAPEFILE_FILE_TYPE)) {
                File rezipFolder = getShapefileUnzipTempDirectory();
                IngestServiceShapefileHelper shpIngestHelper;
                shpIngestHelper = new IngestServiceShapefileHelper(tempFile.toFile(), rezipFolder);
                boolean didProcessWork = shpIngestHelper.processFile();
                if (!(didProcessWork)) {
                    logger.severe("Processing of zipped shapefile failed.");
                    return null;
                }
                try {
                    for (File finalFile : shpIngestHelper.getFinalRezippedFiles()) {
                        FileInputStream finalFileInputStream = new FileInputStream(finalFile);
                        finalType = determineContentType(finalFile);
                        if (finalType == null) {
                            logger.warning("Content type is null; but should default to 'MIME_TYPE_UNDETERMINED_DEFAULT'");
                            continue;
                        }
                        File unZippedShapeTempFile = saveInputStreamInTempFile(finalFileInputStream, fileSizeLimit);
                        DataFile new_datafile = createSingleDataFile(version, unZippedShapeTempFile, finalFile.getName(), finalType, systemConfig.getFileFixityChecksumAlgorithm());
                        String directoryName = null;
                        String absolutePathName = finalFile.getParent();
                        if (absolutePathName != null) {
                            if (absolutePathName.length() > rezipFolder.toString().length()) {
                                directoryName = absolutePathName.substring(rezipFolder.toString().length() + 1);
                                if (!StringUtil.isEmpty(directoryName)) {
                                    new_datafile.getFileMetadata().setDirectoryLabel(directoryName);
                                }
                            }
                        }
                        if (new_datafile != null) {
                            datafiles.add(new_datafile);
                        } else {
                            logger.severe("Could not add part of rezipped shapefile. new_datafile was null: " + finalFile.getName());
                        }
                        finalFileInputStream.close();
                    }
                } catch (FileExceedsMaxSizeException femsx) {
                    logger.severe("One of the unzipped shape files exceeded the size limit; giving up. " + femsx.getMessage());
                    datafiles.clear();
                }
                try {
                    FileUtils.deleteDirectory(rezipFolder);
                } catch (IOException ioex) {
                    logger.warning("Could not remove temp folder, error message : " + ioex.getMessage());
                }
                if (datafiles.size() > 0) {
                    try {
                        Files.delete(tempFile);
                    } catch (IOException ioex) {
                        logger.warning("Could not remove temp file " + tempFile.getFileName().toString());
                    } catch (SecurityException se) {
                        logger.warning("Unable to delete: " + tempFile.toString() + "due to Security Exception: " + se.getMessage());
                    }
                    return datafiles;
                } else {
                    logger.severe("No files added from directory of rezipped shapefiles");
                }
                return null;
            }
        } else {
            finalType = StringUtils.isBlank(suppliedContentType) ? FileUtil.MIME_TYPE_UNDETERMINED_DEFAULT : suppliedContentType;
            String type = determineFileTypeByExtension(fileName);
            if (!StringUtils.isBlank(type)) {
                if (useRecognizedType(finalType, type)) {
                    finalType = type;
                }
                logger.fine("Supplied type: " + suppliedContentType + ", finalType: " + finalType);
            }
        }
        File newFile = null;
        if (tempFile != null) {
            newFile = tempFile.toFile();
        }
        DataFile datafile = createSingleDataFile(version, newFile, newStorageIdentifier, fileName, finalType, newCheckSumType, newCheckSum);
        File f = null;
        if (tempFile != null) {
            f = tempFile.toFile();
        }
        if (datafile != null && ((f != null) || (newStorageIdentifier != null))) {
            if (warningMessage != null) {
                createIngestFailureReport(datafile, warningMessage);
                datafile.SetIngestProblem();
            }
            datafiles.add(datafile);
            return datafiles;
        }
        return null;
    }

    private static boolean useRecognizedType(String suppliedContentType, String recognizedType) {
        if (suppliedContentType == null || suppliedContentType.equals("") || suppliedContentType.equalsIgnoreCase(MIME_TYPE_UNDETERMINED_DEFAULT) || suppliedContentType.equalsIgnoreCase(MIME_TYPE_UNDETERMINED_BINARY) || (canIngestAsTabular(suppliedContentType) && !suppliedContentType.equalsIgnoreCase(MIME_TYPE_CSV) && !suppliedContentType.equalsIgnoreCase(MIME_TYPE_CSV_ALT) && !suppliedContentType.equalsIgnoreCase(MIME_TYPE_XLSX)) || canIngestAsTabular(recognizedType) || recognizedType.equals("application/fits-gzipped") || recognizedType.equalsIgnoreCase(ShapefileHandler.SHAPEFILE_FILE_TYPE) || recognizedType.equals(MIME_TYPE_ZIP)) {
            return true;
        }
        return false;
    }

    private static File saveInputStreamInTempFile(InputStream inputStream, Long fileSizeLimit) throws IOException, FileExceedsMaxSizeException {
        Path tempFile = Files.createTempFile(Paths.get(getFilesTempDirectory()), "tmp", "upload");
        if (inputStream != null && tempFile != null) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Long fileSize = tempFile.toFile().length();
            if (fileSizeLimit != null && fileSize > fileSizeLimit) {
                try {
                    tempFile.toFile().delete();
                } catch (Exception ex) {
                }
                throw new FileExceedsMaxSizeException(MessageFormat.format(BundleUtil.getStringFromBundle("file.addreplace.error.file_exceeds_limit"), bytesToHumanReadable(fileSize), bytesToHumanReadable(fileSizeLimit)));
            }
            return tempFile.toFile();
        }
        throw new IOException("Failed to save uploaded file.");
    }

    private static DataFile createSingleDataFile(DatasetVersion version, File tempFile, String fileName, String contentType, DataFile.ChecksumType checksumType) {
        return createSingleDataFile(version, tempFile, null, fileName, contentType, checksumType, null, false);
    }

    private static DataFile createSingleDataFile(DatasetVersion version, File tempFile, String storageIdentifier, String fileName, String contentType, DataFile.ChecksumType checksumType, String checksum) {
        return createSingleDataFile(version, tempFile, storageIdentifier, fileName, contentType, checksumType, checksum, false);
    }

    private static DataFile createSingleDataFile(DatasetVersion version, File tempFile, String storageIdentifier, String fileName, String contentType, DataFile.ChecksumType checksumType, String checksum, boolean addToDataset) {
        if ((tempFile == null) && (storageIdentifier == null)) {
            return null;
        }
        DataFile datafile = new DataFile(contentType);
        datafile.setModificationTime(new Timestamp(new Date().getTime()));
        datafile.setPermissionModificationTime(new Timestamp(new Date().getTime()));
        FileMetadata fmd = new FileMetadata();
        fmd.setLabel(fileName);
        if (addToDataset) {
            datafile.setOwner(version.getDataset());
        }
        fmd.setDataFile(datafile);
        datafile.getFileMetadatas().add(fmd);
        if (addToDataset) {
            if (version.getFileMetadatas() == null) {
                version.setFileMetadatas(new ArrayList<>());
            }
            version.getFileMetadatas().add(fmd);
            fmd.setDatasetVersion(version);
            version.getDataset().getFiles().add(datafile);
        }
        if (storageIdentifier == null) {
            generateStorageIdentifier(datafile);
            if (!tempFile.renameTo(new File(getFilesTempDirectory() + "/" + datafile.getStorageIdentifier()))) {
                return null;
            }
        } else {
            datafile.setStorageIdentifier(storageIdentifier);
        }
        if ((checksum != null) && (!checksum.isEmpty())) {
            datafile.setChecksumType(checksumType);
            datafile.setChecksumValue(checksum);
        } else {
            try {
                datafile.setChecksumType(checksumType);
                datafile.setChecksumValue(calculateChecksum(getFilesTempDirectory() + "/" + datafile.getStorageIdentifier(), datafile.getChecksumType()));
            } catch (Exception cksumEx) {
                logger.warning("Could not calculate " + checksumType + " signature for the new file " + fileName);
            }
        }
        return datafile;
    }

    private static File getShapefileUnzipTempDirectory() {
        String tempDirectory = getFilesTempDirectory();
        if (tempDirectory == null) {
            logger.severe("Failed to retrieve tempDirectory, null was returned");
            return null;
        }
        String datestampedFileName = "shp_" + new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS").format(new Date());
        String datestampedFolderName = tempDirectory + "/" + datestampedFileName;
        File datestampedFolder = new File(datestampedFolderName);
        if (!datestampedFolder.isDirectory()) {
            try {
                Files.createDirectories(Paths.get(datestampedFolderName));
            } catch (IOException ex) {
                logger.severe("Failed to create temp. directory to unzip shapefile: " + datestampedFolderName);
                return null;
            }
        }
        return datestampedFolder;
    }

    public static boolean canIngestAsTabular(DataFile dataFile) {
        String mimeType = dataFile.getContentType();
        return canIngestAsTabular(mimeType);
    }

    public static boolean canIngestAsTabular(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        switch(mimeType) {
            case MIME_TYPE_STATA:
            case MIME_TYPE_STATA13:
            case MIME_TYPE_STATA14:
            case MIME_TYPE_STATA15:
            case MIME_TYPE_RDATA:
            case MIME_TYPE_CSV:
            case MIME_TYPE_CSV_ALT:
            case MIME_TYPE_TSV:
            case MIME_TYPE_XLSX:
            case MIME_TYPE_SPSS_SAV:
            case MIME_TYPE_SPSS_POR:
                return true;
            default:
                return false;
        }
    }

    public static String getFilesTempDirectory() {
        String filesRootDirectory = System.getProperty("dataverse.files.directory");
        if (filesRootDirectory == null || filesRootDirectory.equals("")) {
            filesRootDirectory = "/tmp/files";
        }
        String filesTempDirectory = filesRootDirectory + "/temp";
        if (!Files.exists(Paths.get(filesTempDirectory))) {
            try {
                Files.createDirectories(Paths.get(filesTempDirectory));
            } catch (IOException ex) {
                logger.severe("Failed to create filesTempDirectory: " + filesTempDirectory);
                return null;
            }
        }
        return filesTempDirectory;
    }

    public static void generateS3PackageStorageIdentifier(DataFile dataFile) {
        String driverId = dataFile.getOwner().getEffectiveStorageDriverId();
        String bucketName = System.getProperty("dataverse.files." + driverId + ".bucket-name");
        String storageId = driverId + "://" + bucketName + ":" + dataFile.getFileMetadata().getLabel();
        dataFile.setStorageIdentifier(storageId);
    }

    public static void generateStorageIdentifier(DataFile dataFile) {
        dataFile.setStorageIdentifier(generateStorageIdentifier());
    }

    public static String generateStorageIdentifier() {
        UUID uid = UUID.randomUUID();
        logger.log(Level.FINE, "UUID value: {0}", uid.toString());
        String hexRandom = uid.toString().substring(24);
        logger.log(Level.FINE, "UUID (last 6 bytes, 12 hex digits): {0}", hexRandom);
        String hexTimestamp = Long.toHexString(new Date().getTime());
        logger.log(Level.FINE, "(not UUID) timestamp in hex: {0}", hexTimestamp);
        String storageIdentifier = hexTimestamp + "-" + hexRandom;
        logger.log(Level.FINE, "timestamp/UUID hybrid: {0}", storageIdentifier);
        return storageIdentifier;
    }

    public static void createIngestFailureReport(DataFile dataFile, String message) {
        createIngestReport(dataFile, IngestReport.INGEST_STATUS_FAILURE, message);
    }

    private static void createIngestReport(DataFile dataFile, int status, String message) {
        IngestReport errorReport = new IngestReport();
        if (status == IngestReport.INGEST_STATUS_FAILURE) {
            errorReport.setFailure();
            errorReport.setReport(message);
            errorReport.setDataFile(dataFile);
            dataFile.setIngestReport(errorReport);
        }
    }

    public enum FileCitationExtension {

        ENDNOTE("-endnote.xml"), RIS(".ris"), BIBTEX(".bib");

        private final String text;

        private FileCitationExtension(final String text) {
            this.text = text;
        }
    }

    public static String getCiteDataFileFilename(String fileTitle, FileCitationExtension fileCitationExtension) {
        if ((fileTitle == null) || (fileCitationExtension == null)) {
            return null;
        }
        if (fileTitle.endsWith("tab")) {
            return fileTitle.replaceAll("\\.tab$", fileCitationExtension.text);
        } else {
            return fileTitle + fileCitationExtension.text;
        }
    }

    public static boolean isDownloadPopupRequired(DatasetVersion datasetVersion) {
        if (datasetVersion == null) {
            logger.fine("Download popup required because datasetVersion is null.");
            return false;
        }
        if (!datasetVersion.isReleased()) {
            logger.fine("Download popup required because datasetVersion has not been released.");
            return false;
        }
        if (datasetVersion.getTermsOfUseAndAccess() != null) {
            if (!TermsOfUseAndAccess.License.CC0.equals(datasetVersion.getTermsOfUseAndAccess().getLicense()) && !(datasetVersion.getTermsOfUseAndAccess().getTermsOfUse() == null || datasetVersion.getTermsOfUseAndAccess().getTermsOfUse().equals(""))) {
                logger.fine("Download popup required because of license or terms of use.");
                return true;
            }
            if (!(datasetVersion.getTermsOfUseAndAccess().getTermsOfAccess() == null) && !datasetVersion.getTermsOfUseAndAccess().getTermsOfAccess().equals("")) {
                logger.fine("Download popup required because of terms of access.");
                return true;
            }
        }
        if (datasetVersion.getDataset() != null && datasetVersion.getDataset().getGuestbook() != null && datasetVersion.getDataset().getGuestbook().isEnabled() && datasetVersion.getDataset().getGuestbook().getDataverse() != null) {
            logger.fine("Download popup required because of guestbook.");
            return true;
        }
        logger.fine("Download popup is not required.");
        return false;
    }

    public static boolean isRequestAccessPopupRequired(DatasetVersion datasetVersion) {
        if (datasetVersion == null) {
            logger.fine("Download popup required because datasetVersion is null.");
            return false;
        }
        if (!datasetVersion.isReleased()) {
            logger.fine("Download popup required because datasetVersion has not been released.");
            return false;
        }
        if (datasetVersion.getTermsOfUseAndAccess() != null) {
            if (!TermsOfUseAndAccess.License.CC0.equals(datasetVersion.getTermsOfUseAndAccess().getLicense()) && !(datasetVersion.getTermsOfUseAndAccess().getTermsOfUse() == null || datasetVersion.getTermsOfUseAndAccess().getTermsOfUse().equals(""))) {
                logger.fine("Download popup required because of license or terms of use.");
                return true;
            }
            if (!(datasetVersion.getTermsOfUseAndAccess().getTermsOfAccess() == null) && !datasetVersion.getTermsOfUseAndAccess().getTermsOfAccess().equals("")) {
                logger.fine("Download popup required because of terms of access.");
                return true;
            }
        }
        logger.fine("Download popup is not required.");
        return false;
    }

    public static boolean isPubliclyDownloadable(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            return false;
        }
        if (fileMetadata.isRestricted()) {
            String msg = "Not publicly downloadable because the file is restricted.";
            logger.fine(msg);
            return false;
        }
        boolean popupReasons = isDownloadPopupRequired(fileMetadata.getDatasetVersion());
        if (popupReasons == true) {
            return false;
        }
        return true;
    }

    public static boolean isPreviewAllowed(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            return false;
        }
        if (fileMetadata.isRestricted()) {
            return false;
        }
        return true;
    }

    public static String getPublicDownloadUrl(String dataverseSiteUrl, String persistentId, Long fileId) {
        String path = null;
        if (persistentId != null) {
            path = dataverseSiteUrl + "/api/access/datafile/:persistentId?persistentId=" + persistentId;
        } else if (fileId != null) {
            path = dataverseSiteUrl + "/api/access/datafile/" + fileId;
        } else {
            logger.info("In getPublicDownloadUrl but persistentId & fileId are both null!");
        }
        return path;
    }

    public static String getFileDownloadUrlPath(String downloadType, Long fileId, boolean gbRecordsWritten, Long fileMetadataId) {
        String fileDownloadUrl = "/api/access/datafile/" + fileId;
        if (downloadType != null && downloadType.equals("bundle")) {
            if (fileMetadataId == null) {
                fileDownloadUrl = "/api/access/datafile/bundle/" + fileId;
            } else {
                fileDownloadUrl = "/api/access/datafile/bundle/" + fileId + "?fileMetadataId=" + fileMetadataId;
            }
        }
        if (downloadType != null && downloadType.equals("original")) {
            fileDownloadUrl = "/api/access/datafile/" + fileId + "?format=original";
        }
        if (downloadType != null && downloadType.equals("RData")) {
            fileDownloadUrl = "/api/access/datafile/" + fileId + "?format=RData";
        }
        if (downloadType != null && downloadType.equals("var")) {
            if (fileMetadataId == null) {
                fileDownloadUrl = "/api/access/datafile/" + fileId + "/metadata";
            } else {
                fileDownloadUrl = "/api/access/datafile/" + fileId + "/metadata?fileMetadataId=" + fileMetadataId;
            }
        }
        if (downloadType != null && downloadType.equals("tab")) {
            fileDownloadUrl = "/api/access/datafile/" + fileId + "?format=tab";
        }
        if (gbRecordsWritten) {
            if (downloadType != null && ((downloadType.equals("original") || downloadType.equals("RData") || downloadType.equals("tab")) || ((downloadType.equals("var") || downloadType.equals("bundle")) && fileMetadataId != null))) {
                fileDownloadUrl += "&gbrecs=true";
            } else {
                fileDownloadUrl += "?gbrecs=true";
            }
        }
        logger.fine("Returning file download url: " + fileDownloadUrl);
        return fileDownloadUrl;
    }

    public static File inputStreamToFile(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            logger.info("In inputStreamToFile but inputStream was null! Returning null rather than a File.");
            return null;
        }
        File file = File.createTempFile(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        try (OutputStream outputStream = new FileOutputStream(file)) {
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            return file;
        }
    }

    public static boolean isThumbnailSupported(DataFile file) {
        if (file == null) {
            return false;
        }
        if (file.isHarvested() || "".equals(file.getStorageIdentifier())) {
            return false;
        }
        String contentType = file.getContentType();
        if (MIME_TYPE_FITSIMAGE.equalsIgnoreCase(contentType)) {
            return false;
        }
        return (contentType != null && (contentType.startsWith("image/") || contentType.equalsIgnoreCase("application/pdf") || (file.isTabularData() && file.hasGeospatialTag()) || contentType.equalsIgnoreCase(MIME_TYPE_GEO_SHAPE)));
    }

    public static DatasetThumbnail getThumbnail(DataFile file) {
        String imageSourceBase64 = ImageThumbConverter.getImageThumbnailAsBase64(file, ImageThumbConverter.DEFAULT_THUMBNAIL_SIZE);
        DatasetThumbnail defaultDatasetThumbnail = new DatasetThumbnail(imageSourceBase64, file);
        return defaultDatasetThumbnail;
    }

    public static boolean isPackageFile(DataFile dataFile) {
        return DataFileServiceBean.MIME_TYPE_PACKAGE_FILE.equalsIgnoreCase(dataFile.getContentType());
    }

    public static S3AccessIO getS3AccessForDirectUpload(Dataset dataset) {
        String driverId = dataset.getEffectiveStorageDriverId();
        boolean directEnabled = Boolean.getBoolean("dataverse.files." + driverId + ".upload-redirect");
        if (!directEnabled) {
            logger.warning("Direct upload not supported for files in this dataset: " + dataset.getId());
            return null;
        }
        S3AccessIO<DataFile> s3io = null;
        String bucket = System.getProperty("dataverse.files." + driverId + ".bucket-name") + "/";
        String sid = null;
        int i = 0;
        while (s3io == null && i < 5) {
            sid = bucket + dataset.getAuthorityForFileStorage() + "/" + dataset.getIdentifierForFileStorage() + "/" + FileUtil.generateStorageIdentifier();
            try {
                s3io = new S3AccessIO<DataFile>(sid, driverId);
                if (s3io.exists()) {
                    s3io = null;
                    i = i + 1;
                }
            } catch (Exception e) {
                i = i + 1;
            }
        }
        return s3io;
    }

    public static void validateDataFileChecksum(DataFile dataFile) throws IOException {
        String recalculatedChecksum = null;
        DataFile.ChecksumType checksumType = dataFile.getChecksumType();
        logger.info(checksumType.toString());
        if (checksumType == null) {
            String info = BundleUtil.getStringFromBundle("dataset.publish.file.validation.error.noChecksumType", Arrays.asList(dataFile.getId().toString()));
            logger.log(Level.INFO, info);
            throw new IOException(info);
        }
        StorageIO<DataFile> storage = dataFile.getStorageIO();
        InputStream in = null;
        try {
            storage.open(DataAccessOption.READ_ACCESS);
            if (!dataFile.isTabularData()) {
                logger.info("It is not tabular");
                in = storage.getInputStream();
            } else {
                in = storage.getAuxFileAsInputStream(FileUtil.SAVED_ORIGINAL_FILENAME_EXTENSION);
            }
        } catch (IOException ioex) {
            in = null;
        }
        if (in == null) {
            String info = BundleUtil.getStringFromBundle("dataset.publish.file.validation.error.failRead", Arrays.asList(dataFile.getId().toString()));
            logger.log(Level.INFO, info);
            throw new IOException(info);
        }
        try {
            logger.info("Before calculating checksum");
            recalculatedChecksum = FileUtil.calculateChecksum(in, checksumType);
            logger.info("Checksum:" + recalculatedChecksum);
        } catch (RuntimeException rte) {
            recalculatedChecksum = null;
        } finally {
            IOUtils.closeQuietly(in);
        }
        if (recalculatedChecksum == null) {
            String info = BundleUtil.getStringFromBundle("dataset.publish.file.validation.error.failCalculateChecksum", Arrays.asList(dataFile.getId().toString()));
            logger.log(Level.INFO, info);
            throw new IOException(info);
        }
        if (!recalculatedChecksum.equals(dataFile.getChecksumValue())) {
            logger.info(dataFile.getChecksumValue());
            logger.info(recalculatedChecksum);
            logger.info("Checksums are not equal");
            boolean fixed = false;
            if (!dataFile.isTabularData() && dataFile.getIngestReport() != null) {
                try {
                    in = storage.getAuxFileAsInputStream(FileUtil.SAVED_ORIGINAL_FILENAME_EXTENSION);
                } catch (IOException ioex) {
                    in = null;
                }
                if (in != null) {
                    try {
                        recalculatedChecksum = FileUtil.calculateChecksum(in, checksumType);
                    } catch (RuntimeException rte) {
                        recalculatedChecksum = null;
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                    if (recalculatedChecksum.equals(dataFile.getChecksumValue())) {
                        fixed = true;
                        try {
                            storage.revertBackupAsAux(FileUtil.SAVED_ORIGINAL_FILENAME_EXTENSION);
                        } catch (IOException ioex) {
                            fixed = false;
                        }
                    }
                }
            }
            if (!fixed) {
                logger.info("checksum cannot be fixed");
                String info = BundleUtil.getStringFromBundle("dataset.publish.file.validation.error.wrongChecksumValue", Arrays.asList(dataFile.getId().toString()));
                logger.log(Level.INFO, info);
                throw new IOException(info);
            }
        }
        logger.log(Level.INFO, "successfully validated DataFile {0}; checksum {1}", new Object[] { dataFile.getId(), recalculatedChecksum });
    }

    public static String getStorageIdentifierFromLocation(String location) {
        int driverEnd = location.indexOf("://") + 3;
        int bucketEnd = driverEnd + location.substring(driverEnd).indexOf("/");
        return location.substring(0, bucketEnd) + ":" + location.substring(location.lastIndexOf("/") + 1);
    }

    public static void deleteTempFile(DataFile dataFile, Dataset dataset, IngestServiceBean ingestService) {
        logger.info("Deleting " + dataFile.getStorageIdentifier());
        try {
            List<Path> generatedTempFiles = ingestService.listGeneratedTempFiles(Paths.get(getFilesTempDirectory()), dataFile.getStorageIdentifier());
            if (generatedTempFiles != null) {
                for (Path generated : generatedTempFiles) {
                    logger.fine("(Deleting generated thumbnail file " + generated.toString() + ")");
                    try {
                        Files.delete(generated);
                    } catch (IOException ioex) {
                        logger.warning("Failed to delete generated file " + generated.toString());
                    }
                }
            }
            String si = dataFile.getStorageIdentifier();
            if (si.contains("://")) {
                if (dataFile.getOwner() != null) {
                    logger.warning("Datafile owner was not null as expected");
                }
                dataFile.setOwner(dataset);
                String sl = DataAccess.getStorageIO(dataFile).getStorageLocation();
                DataAccess.getDirectStorageIO(sl).delete();
            } else {
                Files.delete(Paths.get(FileUtil.getFilesTempDirectory() + "/" + dataFile.getStorageIdentifier()));
            }
        } catch (IOException ioEx) {
            logger.warning(ioEx.getMessage());
            if (dataFile.getStorageIdentifier().contains("://")) {
                logger.warning("Failed to delete temporary file " + dataFile.getStorageIdentifier());
            } else {
                logger.warning("Failed to delete temporary file " + FileUtil.getFilesTempDirectory() + "/" + dataFile.getStorageIdentifier());
            }
        } finally {
            dataFile.setOwner(null);
        }
    }

    public static boolean isFileAlreadyUploaded(DataFile dataFile, Map checksumMapNew, Map fileAlreadyExists) {
        if (checksumMapNew == null) {
            checksumMapNew = new HashMap<>();
        }
        if (fileAlreadyExists == null) {
            fileAlreadyExists = new HashMap<>();
        }
        String chksum = dataFile.getChecksumValue();
        if (chksum == null) {
            return false;
        }
        if (checksumMapNew.get(chksum) != null) {
            fileAlreadyExists.put(dataFile, checksumMapNew.get(chksum));
            return true;
        }
        checksumMapNew.put(chksum, dataFile);
        return false;
    }

    public static String formatFolderListingHtml(String folderName, DatasetVersion version, String apiLocation, boolean originals) {
        String title = formatTitle("Index of folder /" + folderName);
        List<FileMetadata> fileMetadatas = version.getFileMetadatasFolderListing(folderName);
        if (fileMetadatas == null || fileMetadatas.isEmpty()) {
            return "";
        }
        String persistentId = version.getDataset().getGlobalId().asString();
        StringBuilder sb = new StringBuilder();
        String versionTag = version.getFriendlyVersionNumber();
        versionTag = "DRAFT".equals(versionTag) ? "Draft Version" : "v. " + versionTag;
        sb.append(HtmlFormatUtil.formatTag("Index of folder /" + folderName + " in dataset " + persistentId + " (" + versionTag + ")", HTML_H1));
        sb.append("\n");
        sb.append(formatFolderListingTableHtml(folderName, fileMetadatas, apiLocation, originals));
        String body = sb.toString();
        return formatDoc(title, body);
    }

    private static String formatFolderListingTableHtml(String folderName, List<FileMetadata> fileMetadatas, String apiLocation, boolean originals) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatFolderListingTableHeaderHtml());
        for (FileMetadata fileMetadata : fileMetadatas) {
            String localFolder = fileMetadata.getDirectoryLabel() == null ? "" : fileMetadata.getDirectoryLabel();
            if (folderName.equals(localFolder)) {
                String accessUrl = getFileAccessUrl(fileMetadata, apiLocation, originals);
                sb.append(formatFileListEntryHtml(fileMetadata, accessUrl));
                sb.append("\n");
            } else if (localFolder.startsWith(folderName)) {
                String subFolder = "".equals(folderName) ? localFolder : localFolder.substring(folderName.length() + 1);
                if (subFolder.indexOf('/') > 0) {
                    subFolder = subFolder.substring(0, subFolder.indexOf('/'));
                }
                String folderAccessUrl = getFolderAccessUrl(fileMetadata.getDatasetVersion(), folderName, subFolder, apiLocation, originals);
                sb.append(formatFileListFolderHtml(subFolder, folderAccessUrl));
                sb.append("\n");
            }
        }
        return formatTable(sb.toString());
    }

    private static String formatFolderListingTableHeaderHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append(HtmlFormatUtil.formatTag("Name", HTML_TABLE_HDR));
        sb.append(HtmlFormatUtil.formatTag("Last Modified", HTML_TABLE_HDR));
        sb.append(HtmlFormatUtil.formatTag("Size", HTML_TABLE_HDR));
        sb.append(HtmlFormatUtil.formatTag("Description", HTML_TABLE_HDR));
        String hdr = formatTableRow(sb.toString());
        return hdr.concat(formatTableRow(HtmlFormatUtil.formatTag("<hr>", HTML_TABLE_HDR, "colspan=\"4\"")));
    }

    private static String formatFileListEntryHtml(FileMetadata fileMetadata, String accessUrl) {
        StringBuilder sb = new StringBuilder();
        String fileName = fileMetadata.getLabel();
        String dateString = new SimpleDateFormat(FILE_LIST_DATE_FORMAT).format(fileMetadata.getDataFile().getCreateDate());
        String sizeString = fileMetadata.getDataFile().getFriendlySize();
        sb.append(formatTableCell(formatLink(fileName, accessUrl)));
        sb.append(formatTableCellAlignRight(dateString));
        sb.append(formatTableCellAlignRight(sizeString));
        sb.append(formatTableCellAlignRight("&nbsp;"));
        return formatTableRow(sb.toString());
    }

    private static String formatFileListFolderHtml(String folderName, String listApiUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatTableCell(formatLink(folderName + "/", listApiUrl)));
        sb.append(formatTableCellAlignRight(" - "));
        sb.append(formatTableCellAlignRight(" - "));
        sb.append(formatTableCellAlignRight("&nbsp;"));
        return formatTableRow(sb.toString());
    }

    private static String getFileAccessUrl(FileMetadata fileMetadata, String apiLocation, boolean original) {
        String fileId = fileMetadata.getDataFile().getId().toString();
        if (StringUtil.nonEmpty(fileMetadata.getDirectoryLabel())) {
            fileId = fileMetadata.getDirectoryLabel().concat("/").concat(fileId);
        }
        String formatArg = fileMetadata.getDataFile().isTabularData() && original ? "?format=original" : "";
        return apiLocation + "/api/access/datafile/" + fileId + formatArg;
    }

    private static String getFolderAccessUrl(DatasetVersion version, String currentFolder, String subFolder, String apiLocation, boolean originals) {
        String datasetId = version.getDataset().getId().toString();
        String versionTag = version.getFriendlyVersionNumber();
        versionTag = versionTag.replace("DRAFT", ":draft");
        if (!"".equals(currentFolder)) {
            subFolder = currentFolder + "/" + subFolder;
        }
        return apiLocation + "/api/datasets/" + datasetId + "/dirindex/?version=" + versionTag + "&" + "folder=" + subFolder + (originals ? "&original=true" : "");
    }
}
