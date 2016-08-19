package Patching.Model;

/**
 * @author Robin Duda
 */
public class PatchFile {
    private String path;
    private Long modified;
    private Long size;
    private byte[] bytes;

    public PatchFile(String path, long size, long modified, byte[] bytes) {
        this.path = path;
        this.size = size;
        this.modified = modified;
        this.bytes = bytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
