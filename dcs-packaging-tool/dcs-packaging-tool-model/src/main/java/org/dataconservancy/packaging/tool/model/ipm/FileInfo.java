package org.dataconservancy.packaging.tool.model.ipm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.model.DetectedFormat;
import org.dataconservancy.dcs.util.ChecksumGeneratorVerifier;
import org.dataconservancy.dcs.util.ContentDetectionService;

/**
 * Information about a file or directory.
 */
public class FileInfo {

    public enum Algorithm {
        SHA1,
        MD5
    }

    private URI location;
    private String name;
    private List<String> formats;
    private Map<Algorithm, String> checksums;
    private long size;
    private FileTime creationTime;
    private FileTime lastModifiedTime;
    private boolean isRegularFile;

    /**
     * Default constructor that should be used in most cases. Will read the file at the path location and load the necessary file attributes.
     * @param path The path to the file.
     */
    public FileInfo(Path path) {
        location = path.toUri();
        name = path.getFileName().toString();

        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            isRegularFile = fileAttributes.isRegularFile();
            size = fileAttributes.size();
            creationTime = fileAttributes.creationTime();
            lastModifiedTime = fileAttributes.lastModifiedTime();

            if (fileAttributes.isRegularFile()) {
                checksums = new HashMap<>();
                formats = new ArrayList<>();

                InputStream md5Fis = Files.newInputStream(path);
                String md5Checksum = ChecksumGeneratorVerifier.generateMD5checksum(md5Fis);
                checksums.put(Algorithm.MD5, md5Checksum);
                md5Fis.close();

                InputStream sha1Fis = Files.newInputStream(path);
                String sha1Checksum = ChecksumGeneratorVerifier.generateSHA1checksum(sha1Fis);
                checksums.put(Algorithm.SHA1, sha1Checksum);
                sha1Fis.close();

                List<DetectedFormat> fileFormats = ContentDetectionService.getInstance().detectFormats(path.toFile());
                for (DetectedFormat format : fileFormats) {
                    formats.add(createFormatURIString(format));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor to use when loading existing file information.
     * @param path The path of the File info
     * @param fileAttributes The basic file attibutes of the file.
     */
    public FileInfo(Path path, BasicFileAttributes fileAttributes, List<String> formats, Map<Algorithm, String> checksums) {
        location = path.toUri();
        name = path.getFileName().toString();

        isRegularFile = fileAttributes.isRegularFile();
        size = fileAttributes.size();
        creationTime = fileAttributes.creationTime();
        lastModifiedTime = fileAttributes.lastModifiedTime();

        this.formats = formats;
        this.checksums = checksums;
    }

    /**
     * @return Location of file or directory.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * @param algorithm The algorithm of the checksum either MD5 or SHA1
     * @return Checksum in known format of the file or null if directory.
     */
    public String getChecksum(Algorithm algorithm) {
        String value = null;
        if (checksums != null) {
            value = checksums.get(algorithm);
        }
        return value;
    }

    /**
     * @return Name of the file.
     */
    public String getName() {
        return name;
    }

    /**
     * @return List of formats for the file.
     */
    public List<String> getFormats() {
        return formats;
    }

    /**
     * @return Size of the file or -1 if directory.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return Whether or not a file is being described.
     */
    public boolean isFile() {
        return isRegularFile;
    }

    /**
     * @return Whether or not a directory is being described.
     */
    public boolean isDirectory() {
        return !isRegularFile;
    }

    /**
     * @return Creation time of file.
     */
    public FileTime getCreationTime() {
        return creationTime;
    }

    /**
     * @return Last modification time of file.
     */
    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Converts format id from the DcsFormat objects into formatURI string with qualifying namespace. Only applicable
     * to pronom format identifier at this point.
     * @param format the DetectedFormat object
     * @return a formatURI string with qualifying namespace
     */
    private String createFormatURIString(DetectedFormat format) {
        String formatString = "";
        if (format.getId() != null && !format.getId().isEmpty()) {
            formatString = "info:pronom/" + format.getId();
        } else if (format.getMimeType() != null && !format.getMimeType().isEmpty()) {
            formatString = format.getMimeType();
        }

        return formatString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileInfo)) {
            return false;
        }

        FileInfo fileInfo = (FileInfo) o;

        if (size != fileInfo.size) {
            return false;
        }
        if (isRegularFile != fileInfo.isRegularFile) {
            return false;
        }
        if (location != null ? !location.equals(fileInfo.location) :
            fileInfo.location != null) {
            return false;
        }
        if (name != null ? !name.equals(fileInfo.name) :
            fileInfo.name != null) {
            return false;
        }
        if (formats != null ? !formats.equals(fileInfo.formats) :
            fileInfo.formats != null) {
            return false;
        }
        if (checksums != null ? !checksums.equals(fileInfo.checksums) :
            fileInfo.checksums != null) {
            return false;
        }
        if (creationTime != null ? !creationTime.equals(fileInfo.creationTime) :
            fileInfo.creationTime != null) {
            return false;
        }
        return !(lastModifiedTime !=
                     null ? !lastModifiedTime.equals(fileInfo.lastModifiedTime) :
                     fileInfo.lastModifiedTime != null);

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (formats != null ? formats.hashCode() : 0);
        result = 31 * result + (checksums != null ? checksums.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result =
            31 * result + (creationTime != null ? creationTime.hashCode() : 0);
        result = 31 * result +
            (lastModifiedTime != null ? lastModifiedTime.hashCode() : 0);
        result = 31 * result + (isRegularFile ? 1 : 0);
        return result;
    }
}
