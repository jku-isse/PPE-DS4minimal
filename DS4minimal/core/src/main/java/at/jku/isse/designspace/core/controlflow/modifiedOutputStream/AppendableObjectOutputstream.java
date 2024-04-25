package at.jku.isse.designspace.core.controlflow.modifiedOutputStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class AppendableObjectOutputstream extends ObjectOutputStream {

    public AppendableObjectOutputstream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // The ObjectOutputStream usually always calls this method when it is created.
        // But when the file already exists we do not want to create another header.
        // In order to avoid multiple headers, which will destroy the file we will
        // ignore the state of the header.
        reset();
    }

}
