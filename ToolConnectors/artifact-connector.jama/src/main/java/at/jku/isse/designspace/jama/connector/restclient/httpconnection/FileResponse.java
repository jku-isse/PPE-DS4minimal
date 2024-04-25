package at.jku.isse.designspace.jama.connector.restclient.httpconnection;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileResponse extends Response {
    private byte[] fileBytes;

    public FileResponse(int statusCode, InputStream inputStream) throws IOException {
        super(statusCode, "File response");
        this.fileBytes = IOUtils.toByteArray(inputStream);
    }

    public byte[] getFileData() {
        return fileBytes;
    }
}
