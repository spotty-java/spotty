package spotty.server.compress;

import spotty.common.exception.SpottyException;
import spotty.common.http.ContentEncoding;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static spotty.common.validation.Validation.notNull;

public final class Compressor {

    public byte[] compress(ContentEncoding encoding, byte[] body) throws Exception {
        notNull("encoding", encoding);

        final ByteArrayOutputStream out = new ByteArrayOutputStream(body.length);
        final OutputStream compressor;
        switch (encoding) {
            case GZIP:
                compressor = new GZIPOutputStream(out);
                break;
            case DEFLATE:
                compressor = new DeflaterOutputStream(out);
                break;
            default:
                throw new SpottyException(encoding + " unsupported compression algorithm");
        }

        try {
            compressor.write(body);
        } finally {
            compressor.close();
        }

        return out.toByteArray();
    }

}
