package committee.nova.mods.avaritia.util.io;

import javax.annotation.WillNotClose;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
/**
 * IOUtil
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/7 上午2:34
 */
public class IOUtil {
    //32k buffer.
    private static final ThreadLocal<byte[]> arrayCache = ThreadLocal.withInitial(() -> new byte[32 * 1024]);
    private static final ThreadLocal<ByteBuffer> directBufferCache = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(16 * 1024));
    private static final Map<String, String> jfsArgsCreate = Collections.singletonMap("create", "true");

    /**
     * Returns a static per-thread cached 32k buffer for IO operations.
     *
     * @return The buffer.
     */
    public static byte[] getCachedBuffer() {
        return arrayCache.get();
    }

    /**
     * Copies the content of an {@link InputStream} to an {@link OutputStream}.
     *
     * @param is The {@link InputStream}.
     * @param os The {@link OutputStream}.
     * @throws IOException If something is bork.
     */
    public static void copy(@WillNotClose InputStream is, @WillNotClose OutputStream os) throws IOException {
        byte[] buffer = arrayCache.get();
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    /**
     * Copies the content of an {@link ReadableByteChannel} into an {@link WritableByteChannel}.
     * <p>
     * This method makes use of direct {@link ByteBuffer} instances.
     *
     * @param rc The {@link ReadableByteChannel} to copy from.
     * @param wc The {@link WritableByteChannel} to copy to.
     * @throws IOException If an IO Error occurred whilst copying.
     */
    public static void copy(@WillNotClose ReadableByteChannel rc, @WillNotClose WritableByteChannel wc) throws IOException {
        ByteBuffer buffer = directBufferCache.get();
        buffer.clear();
        while (rc.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                wc.write(buffer);
            }
            buffer.clear();
        }
    }

    /**
     * Reads {@code buffer.remaining()} bytes from the channel into
     * the buffer.
     * <p>
     * This method blocks until the requested bytes are read.
     * <p>
     * If the stream ends prior to the requested number of bytes an {@link EOFException}
     * is thrown.
     *
     * @param channel The channel to read from.
     * @param buffer  The buffer to fill.
     */
    public static void fill(@WillNotClose ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
        int toRead = buffer.remaining();
        int read = 0;
        int len;
        while (buffer.hasRemaining() && (len = channel.read(buffer)) != -1) {
            read += len;
        }
        if (read < toRead) {
            throw new EOFException("Expected " + toRead + " Got " + read);
        }
    }

    /**
     * Reads an {@link InputStream} to a byte array.
     *
     * @param is The InputStream.
     * @return The bytes.
     * @throws IOException If something is bork.
     */
    public static byte[] toBytes(@WillNotClose InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os);
        return os.toByteArray();
    }

    /**
     * Reads the provided array of bytes into a List of UTF-8 Strings.
     *
     * @param bytes The bytes of the strings.
     * @return The lines.
     * @throws IOException Any exception thrown reading the bytes.
     */
    public static List<String> readAll(byte[] bytes) throws IOException {
        return readAll(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads the provided array of bytes into a List of Strings
     * in the given {@link Charset}.
     *
     * @param bytes The bytes of the strings.
     * @return The lines.
     * @throws IOException Any exception thrown reading the bytes.
     */
    public static List<String> readAll(byte[] bytes, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    /**
     * Read the entire content of the provided {@link Path} into a
     * UTF-8 {@link String}.
     *
     * @param path The path to read.
     * @return The {@link String}.
     * @throws IOException If there was an error reading the file.
     */
    public static String readAll(Path path) throws IOException {
        return readAll(path, StandardCharsets.UTF_8);
    }

    /**
     * Read the entire content of the provided {@link Path} into a
     * {@link String} with the given charset.
     *
     * @param path    The path to read.
     * @param charset The {@link Charset}.
     * @return The {@link String}.
     * @throws IOException If there was an error reading the file.
     */
    public static String readAll(Path path, Charset charset) throws IOException {
        return new String(Files.readAllBytes(path), charset);
    }

    /**
     * Creates the parent directories of the given path if they don't exist.
     *
     * @param path The path.
     * @return The same path.
     * @throws IOException If an error occurs creating the directories.
     */
    public static Path makeParents(Path path) throws IOException {
        path = path.toAbsolutePath();
        Path parent = path.getParent();
        if (Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
        return path;
    }

    /**
     * Creates a new {@link FileSystem} for the given jar path.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param path   The file to open the jar for.
     * @param create If the file system should attempt to be created if it does not exist.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getJarFileSystem(Path path, boolean create) throws IOException {
        return getJarFileSystem(path.toUri(), create);
    }

    /**
     * Creates a new {@link FileSystem} for the given uri path.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param path   The uri to open the jar for.
     * @param create If the file system should attempt to be created if it does not exist.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getJarFileSystem(URI path, boolean create) throws IOException {
        URI jarURI;
        try {
            jarURI = new URI("jar:file", null, path.getPath(), "");
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return getFileSystem(jarURI, create ? jfsArgsCreate : Collections.emptyMap());
    }

    /**
     * Attempts to get an already existing {@link FileSystem}.
     * <p>
     * This method will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param uri The uri to open the jar for.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getFileSystem(URI uri) throws IOException {
        return getFileSystem(uri, Collections.emptyMap());
    }

    /**
     * Attempts to get or create a {@link FileSystem} for the given uri, with additional arguments for FS creation.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param uri The uri to open the jar for.
     * @param env Any additional arguments to provide when creating the {@link FileSystem}.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getFileSystem(URI uri, Map<String, ?> env) throws IOException {
        FileSystem fs;
        boolean owner = true;
        try {
            fs = FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException e) {
            fs = FileSystems.getFileSystem(uri);
            owner = false;
        }
        return owner ? fs : protectClose(fs);
    }

    /**
     * wraps the given {@link FileSystem} protecting it against {@link FileSystem#close()} operations.
     *
     * @param fs The {@link FileSystem} to wrap.
     * @return The wrapped {@link FileSystem}.
     */
    public static FileSystem protectClose(FileSystem fs) {
        return new DelegateFileSystem(fs) {
            @Override
            public void close() {
            }
        };
    }

    /**
     * wraps the given {@link InputStream} protecting it against {@link InputStream#close()} operations.
     *
     * @param is The {@link InputStream} to wrap.
     * @return The wrapped {@link InputStream}.
     */
    public static InputStream protectClose(InputStream is) {
        return new FilterInputStream(is) {
            @Override
            public void close() {
            }
        };
    }

    /**
     * wraps the given {@link OutputStream} protecting it against {@link OutputStream#close()} operations.
     *
     * @param os The {@link OutputStream} to wrap.
     * @return The wrapped {@link OutputStream}.
     */
    public static OutputStream protectClose(OutputStream os) {
        return new FilterOutputStream(os) {
            @Override
            public void close() {
            }
        };
    }

    /**
     * Copes every element in a Jar file from the input to the Jar file output, where every
     * element must match the provided {@link Predicate}.
     *
     * @param input     The Input Path. This should exist.
     * @param output    The Output Path. This should not exists.
     * @param predicate The {@link Predicate} that each Entry must match.
     * @throws IOException If something went wrong during execution.
     */
    public static void stripJar(Path input, Path output, Predicate<Path> predicate) throws IOException {
        if (Files.notExists(input)) throw new FileNotFoundException("Input not found. " + input);
        if (Files.exists(output)) throw new IOException("Output already exists. " + output);

        try (FileSystem inFs = getJarFileSystem(input, true);
             FileSystem outFs = getJarFileSystem(output, true)
        ) {
            Path inRoot = inFs.getPath("/");
            Path outRoot = outFs.getPath("/");
            Files.walkFileTree(inRoot, new CopyingFileVisitor(inRoot, outRoot, predicate));
        }
    }

    /**
     * Converts a Posix 'file mode' into a set of {@link PosixFilePermission}s.
     * <p>
     * Thanks: https://stackoverflow.com/a/54422530/11313544
     *
     * @param mode The file mode.
     * @return The parsed permissions.
     */
    public static Set<PosixFilePermission> parseMode(int mode) {
        Set<PosixFilePermission> perms = new HashSet<>();
        char[] ds = Integer.toString(mode).toCharArray();
        for (int i = ds.length - 1; i >= 0; i--) {
            int n = ds[i] - '0';
            if (i == ds.length - 1) {
                if ((n & 1) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);
                if ((n & 2) != 0) perms.add(PosixFilePermission.OTHERS_WRITE);
                if ((n & 4) != 0) perms.add(PosixFilePermission.OTHERS_READ);
            } else if (i == ds.length - 2) {
                if ((n & 1) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE);
                if ((n & 2) != 0) perms.add(PosixFilePermission.GROUP_WRITE);
                if ((n & 4) != 0) perms.add(PosixFilePermission.GROUP_READ);
            } else if (i == ds.length - 3) {
                if ((n & 1) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE);
                if ((n & 2) != 0) perms.add(PosixFilePermission.OWNER_WRITE);
                if ((n & 4) != 0) perms.add(PosixFilePermission.OWNER_READ);
            }
        }
        return perms;
    }

    /**
     * Writes a set of {@link PosixFilePermission}s to a Posix 'file mode' integer.
     *
     * @param perms The permissions.
     * @return The 'file mode'.
     */
    public static int writeMode(Set<PosixFilePermission> perms) {
        int[] segs = new int[3];
        if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) segs[2] |= 1;
        if (perms.contains(PosixFilePermission.OTHERS_WRITE)) segs[2] |= 2;
        if (perms.contains(PosixFilePermission.OTHERS_READ)) segs[2] |= 4;
        if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) segs[1] |= 1;
        if (perms.contains(PosixFilePermission.GROUP_WRITE)) segs[1] |= 2;
        if (perms.contains(PosixFilePermission.GROUP_READ)) segs[1] |= 4;
        if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) segs[0] |= 1;
        if (perms.contains(PosixFilePermission.OWNER_WRITE)) segs[0] |= 2;
        if (perms.contains(PosixFilePermission.OWNER_READ)) segs[0] |= 4;
        return Integer.parseInt(String.valueOf(segs[0]) + segs[1] + segs[2]);
    }
}
