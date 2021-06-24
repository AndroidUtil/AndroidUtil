/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidutil.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * </p>
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible. Relying on the platform default means that the
 * code is Locale-dependent. Only use the default if the files are known to always use the platform default.
 * </p>
 * <p>
 * {@link SecurityException} are not documented in the Javadoc.
 * </p>
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 * </p>
 */
public class FileUtils {
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * The number of bytes in a gigabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    /**
     * The number of bytes in a terabyte.
     */
    public static final long ONE_TB = ONE_KB * ONE_GB;

    /**
     * The number of bytes in a terabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    /**
     * The number of bytes in a petabyte.
     */
    public static final long ONE_PB = ONE_KB * ONE_TB;

    /**
     * The number of bytes in a petabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    /**
     * The number of bytes in an exabyte.
     */
    public static final long ONE_EB = ONE_KB * ONE_PB;

    /**
     * The number of bytes in an exabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

    /**
     * The number of bytes in a zettabyte.
     */
    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

    /**
     * The number of bytes in a yottabyte.
     */
    public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

    /**
     * An empty array of type {@code File}.
     */
    public static final File[] EMPTY_FILE_ARRAY = {};


    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @throws NullPointerException if the given {@code BigInteger} is {@code null}.
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     * @since 2.4
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(final BigInteger size) {
        Objects.requireNonNull(size, "size");
        final String displaySize;

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_EB_BI) + " EB";
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_PB_BI) + " PB";
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_TB_BI) + " TB";
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_GB_BI) + " GB";
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_MB_BI) + " MB";
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_KB_BI) + " KB";
        } else {
            displaySize = size + " bytes";
        }
        return displaySize;
    }

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(final long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }

    /**
     * Compares the contents of two files to determine if they are equal or not.
     * <p>
     * This method checks to see if the two files point to the same file,
     * before resorting to line-by-line comparison of the contents.
     * </p>
     *
     * @param file1       the first file
     * @param file2       the second file
     * @param charsetName the name of the requested charset.
     *                    May be null, in which case the platform default is used
     * @return true if the content of the files are equal or neither exists,
     * false otherwise
     * @throws IllegalArgumentException when an input is not a file.
     * @throws IOException in case of an I/O error.
     * @throws UnsupportedCharsetException If the named charset is unavailable (unchecked exception).
     * @see IOUtils#contentEqualsIgnoreEOL(Reader, Reader)
     * @since 2.2
     */
    public static boolean contentEqualsIgnoreEOL(final File file1, final File file2, final String charsetName)
            throws IOException {
        if (file1 == null && file2 == null) {
            return true;
        }
        if (file1 == null || file2 == null) {
            return false;
        }
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        requireFile(file1, "file1");
        requireFile(file2, "file2");

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        final Charset charset = Charsets.toCharset(charsetName);
        try (Reader input1 = new InputStreamReader(new FileInputStream(file1), charset);
             Reader input2 = new InputStreamReader(new FileInputStream(file2), charset)) {
            return IOUtils.contentEqualsIgnoreEOL(input1, input2);
        }
    }


    /**
     * Copies bytes from a {@code File} to an {@code OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     * </p>
     *
     * @param input  the {@code File} to read.
     * @param output the {@code OutputStream} to write.
     * @return the number of bytes copied
     * @throws NullPointerException if the File is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.1
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (FileInputStream fis = new FileInputStream(input)) {
            return IOUtils.copyLarge(fis, output);
        }
    }

    /**
     * Copies bytes from an {@link InputStream} {@code source} to a file
     * {@code destination}. The directories up to {@code destination}
     * will be created if they don't already exist. {@code destination}
     * will be overwritten if it already exists.
     * <p>
     * <em>The {@code source} stream is closed.</em>
     * </p>
     * <p>
     * See {@link #copyToFile(InputStream, File)} for a method that does not close the input stream.
     * </p>
     *
     * @param source      the {@code InputStream} to copy bytes from, must not be {@code null}, will be closed
     * @param destination the non-directory {@code File} to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream inputStream = source) {
            copyToFile(inputStream, destination);
        }
    }


    /**
     * Copies bytes from an {@link InputStream} source to a {@link File} destination. The directories
     * up to {@code destination} will be created if they don't already exist. {@code destination} will be
     * overwritten if it already exists. The {@code source} stream is left open, e.g. for use with
     * {@link java.util.zip.ZipInputStream ZipInputStream}. See {@link #copyInputStreamToFile(InputStream, File)} for a
     * method that closes the input stream.
     *
     * @param inputStream the {@code InputStream} to copy bytes from, must not be {@code null}
     * @param file the non-directory {@code File} to write bytes to (possibly overwriting), must not be
     *        {@code null}
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the File is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory.
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
     * @throws IOException if an IO error occurs during copying.
     * @since 2.5
     */
    public static void copyToFile(final InputStream inputStream, final File file) throws IOException {
        try (OutputStream out = openOutputStream(file)) {
            IOUtils.copy(inputStream, out);
        }
    }

    /**
     * Copies bytes from the URL {@code source} to a file
     * {@code destination}. The directories up to {@code destination}
     * will be created if they don't already exist. {@code destination}
     * will be overwritten if it already exists.
     * <p>
     * Warning: this method does not set a connection or read timeout and thus
     * might block forever. Use {@link #copyURLToFile(URL, File, int, int)}
     * with reasonable timeouts to prevent this.
     * </p>
     *
     * @param source      the {@code URL} to copy bytes from, must not be {@code null}
     * @param destination the non-directory {@code File} to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if {@code source} URL cannot be opened
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyURLToFile(final URL source, final File destination) throws IOException {
        try (final InputStream stream = source.openStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }

    /**
     * Copies bytes from the URL {@code source} to a file {@code destination}. The directories up to
     * {@code destination} will be created if they don't already exist. {@code destination} will be
     * overwritten if it already exists.
     *
     * @param source the {@code URL} to copy bytes from, must not be {@code null}
     * @param destination the non-directory {@code File} to write bytes to (possibly overwriting), must not be
     *        {@code null}
     * @param connectionTimeoutMillis the number of milliseconds until this method will timeout if no connection could
     *        be established to the {@code source}
     * @param readTimeoutMillis the number of milliseconds until this method will timeout if no data could be read from
     *        the {@code source}
     * @throws IOException if {@code source} URL cannot be opened
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyURLToFile(final URL source, final File destination,
        final int connectionTimeoutMillis, final int readTimeoutMillis) throws IOException {
        final URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        try (final InputStream stream = connection.getInputStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }


    /**
     * Creates all parent directories for a File object.
     *
     * @param file the File that may need parents, may be null.
     * @return The parent directory, or {@code null} if the given file does not name a parent
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not null and not a directory.
     * @since 2.9.0
     */
    public static File createParentDirectories(final File file) throws IOException {
        return mkdirs(getParentFile(file));
    }

    /**
     * Decodes the specified URL as per RFC 3986, i.e. transforms
     * percent-encoded octets to characters by decoding with the UTF-8 character
     * set. This function is primarily intended for usage with
     * {@link URL} which unfortunately does not enforce proper URLs. As
     * such, this method will leniently accept invalid characters or malformed
     * percent-encoded octets and simply pass them literally through to the
     * result string. Except for rare edge cases, this will make unencoded URLs
     * pass through unaltered.
     *
     * @param url The URL to decode, may be {@code null}.
     * @return The decoded URL or {@code null} if the input was
     * {@code null}.
     */
    static String decodeUrl(final String url) {
        String decoded = url;
        if (url != null && url.indexOf('%') >= 0) {
            final int n = url.length();
            final StringBuilder buffer = new StringBuilder();
            final ByteBuffer bytes = ByteBuffer.allocate(n);
            for (int i = 0; i < n; ) {
                if (url.charAt(i) == '%') {
                    try {
                        do {
                            final byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
                            bytes.put(octet);
                            i += 3;
                        } while (i < n && url.charAt(i) == '%');
                        continue;
                    } catch (final RuntimeException e) {
                        // malformed percent-encoded octet, fall through and
                        // append characters literally
                    } finally {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                            bytes.clear();
                        }
                    }
                }
                buffer.append(url.charAt(i++));
            }
            decoded = buffer.toString();
        }
        return decoded;
    }


    /**
     * Makes a directory, including any necessary but nonexistent parent
     * directories. If a file already exists with specified name but it is
     * not a directory then an IOException is thrown.
     * If the directory cannot be created (or the file already exists but is not a directory)
     * then an IOException is thrown.
     *
     * @param directory directory to create, must not be {@code null}.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     */
    public static void forceMkdir(final File directory) throws IOException {
        mkdirs(directory);
    }

    /**
     * Makes any necessary but nonexistent parent directories for a given File. If the parent directory cannot be
     * created then an IOException is thrown.
     *
     * @param file file with parent to create, must not be {@code null}.
     * @throws NullPointerException if the file is {@code null}.
     * @throws IOException          if the parent directory cannot be created.
     * @since 2.5
     */
    public static void forceMkdirParent(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        final File parent = getParentFile(file);
        if (parent == null) {
            return;
        }
        forceMkdir(parent);
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory.
     * @param names the name elements.
     * @return the new file.
     * @since 2.1
     */
    public static File getFile(final File directory, final String... names) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(names, "names");
        File file = directory;
        for (final String name : names) {
            file = new File(file, name);
        }
        return file;
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param names the name elements.
     * @return the file.
     * @since 2.1
     */
    public static File getFile(final String... names) {
        Objects.requireNonNull(names, "names");
        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    /**
     * Gets the parent of the given file. The given file may be bull and a file's parent may as well be null.
     *
     * @param file The file to query.
     * @return The parent file or {@code null}.
     */
    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }


    /**
     * Lists files in a directory, asserting that the supplied directory exists and is a directory.
     *
     * @param directory The directory to list
     * @param fileFilter Optional file filter, may be null.
     * @return The files in the directory, never {@code null}.
     * @throws NullPointerException if directory is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     */
    private static File[] listFiles(final File directory, final FileFilter fileFilter) throws IOException {
        requireDirectoryExists(directory, "directory");
        final File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
        if (files == null) {
            // null if the directory does not denote a directory, or if an I/O error occurs.
            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
        }
        return files;
    }


    /**
     * Calls {@link File#mkdirs()} and throws an exception on failure.
     *
     * @param directory the receiver for {@code mkdirs()}, may be null.
     * @return the given file, may be null.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see File#mkdirs()
     */
    private static File mkdirs(final File directory) throws IOException {
        if ((directory != null) && (!directory.mkdirs() && !directory.isDirectory())) {
            throw new IOException("Cannot create directory '" + directory + "'.");
        }
        return directory;
    }


    /**
     * Opens a {@link FileInputStream} for the specified file, providing better error messages than simply calling
     * {@code new FileInputStream(file)}.
     * <p>
     * At the end of the method either the stream will be successfully opened, or an exception will have been thrown.
     * </p>
     * <p>
     * An exception is thrown if the file does not exist. An exception is thrown if the file object exists but is a
     * directory. An exception is thrown if the file exists but cannot be read.
     * </p>
     *
     * @param file the file to open for input, must not be {@code null}
     * @return a new {@link FileInputStream} for the specified file
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException See FileNotFoundException above, FileNotFoundException is a subclass of IOException.
     * @since 1.3
     */
    public static FileInputStream openInputStream(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return new FileInputStream(file);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * </p>
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     * </p>
     *
     * @param file the file to open for output, must not be {@code null}
     * @return a new {@link FileOutputStream} for the specified file
     * @throws NullPointerException if the file object is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
     * @since 1.3
     */
    public static FileOutputStream openOutputStream(final File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * </p>
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     * </p>
     *
     * @param file   the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws NullPointerException if the file object is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.exists()) {
            requireFile(file, "file");
            requireCanWrite(file, "file");
        } else {
            createParentDirectories(file);
        }
        return new FileOutputStream(file, append);
    }

    /**
     * Reads the named file, translating {@link IOException} to a
     * {@link RuntimeException} of some sort.
     *
     * @param fileName {@code non-null;} name of the file to read
     * @return {@code non-null;} contents of the file
     */
    public static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        return readFile(file);
    }

    /**
     * Reads the given file, translating {@link IOException} to a
     * {@link RuntimeException} of some sort.
     *
     * @param file {@code non-null;} the file to read
     * @return {@code non-null;} contents of the file
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException(file + ": file not found");
        }

        if (!file.isFile()) {
            throw new RuntimeException(file + ": not a file");
        }

        if (!file.canRead()) {
            throw new RuntimeException(file + ": file not readable");
        }

        long longLength = file.length();
        int length = (int) longLength;
        if (length != longLength) {
            throw new RuntimeException(file + ": file too long");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // ignored.
                }
            }
        }

        return baos.toByteArray();
    }


    /**
     * Reads the contents of a file line by line to a List of Strings using the default encoding for the VM.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.3
     * @deprecated 2.5 use {@link #readLines(File, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static List<String> readLines(final File file) throws IOException {
        return readLines(file, Charset.defaultCharset());
    }

    /**
     * Reads the contents of a file line by line to a List of Strings.
     * The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charset the charset to use, {@code null} means platform default
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 2.3
     */
    public static List<String> readLines(final File file, final Charset charset) throws IOException {
        try (InputStream inputStream = openInputStream(file)) {
            return IOUtils.readLines(inputStream, Charsets.toCharset(charset));
        }
    }

    /**
     * Reads the contents of a file line by line to a List of Strings. The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @throws UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the named charset is unavailable.
     * @since 1.1
     */
    public static List<String> readLines(final File file, final String charsetName) throws IOException {
        return readLines(file, Charsets.toCharset(charsetName));
    }


    /**
     * Throws IllegalArgumentException if the given files' canonical representations are equal.
     *
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @throws IllegalArgumentException if the given files' canonical representations are equal.
     */
    private static void requireCanonicalPathsNotEquals(final File file1, final File file2) throws IOException {
        final String canonicalPath = file1.getCanonicalPath();
        if (canonicalPath.equals(file2.getCanonicalPath())) {
            throw new IllegalArgumentException(String
                .format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the file is not writable. This provides a more precise exception
     * message than a plain access denied.
     *
     * @param file The file to test.
     * @param name The parameter name to use in the exception message.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the file is not writable.
     */
    private static void requireCanWrite(final File file, final String name) {
        Objects.requireNonNull(file, "file");
        if (!file.canWrite()) {
            throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
        }
    }

    /**
     * Requires that the given {@code File} is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input or if the file is not a directory.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectory(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    /**
     * Requires that the given {@code File} exists and is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectoryExists(final File directory, final String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    /**
     * Requires that the given {@code File} is a directory if it exists.
     *
     * @param directory The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} exists but is not a directory.
     */
    private static File requireDirectoryIfExists(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (directory.exists()) {
            requireDirectory(directory, name);
        }
        return directory;
    }

    /**
     * Requires that two file lengths are equal.
     *
     * @param srcFile Source file.
     * @param destFile Destination file.
     * @param srcLen Source file length.
     * @param dstLen Destination file length
     * @throws IOException Thrown when the given sizes are not equal.
     */
    private static void requireEqualSizes(final File srcFile, final File destFile, final long srcLen, final long dstLen)
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link IllegalArgumentException} if it doesn't.
     *
     * @param file The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist.
     */
    private static File requireExists(final File file, final String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException(
                "File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link FileNotFoundException} if it doesn't.
     *
     * @param file The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws FileNotFoundException if the given {@code File} does not exist.
     */
    private static File requireExistsChecked(final File file, final String fileParamName) throws FileNotFoundException {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new FileNotFoundException(
                "File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    /**
     * Requires that the given {@code File} is a file.
     *
     * @param file The {@code File} to check.
     * @param name The parameter name to use in the exception message.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireFile(final File file, final String name) {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
        }
        return file;
    }

    /**
     * Requires parameter attributes for a file copy operation.
     *
     * @param source the source file
     * @param destination the destination
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileNotFoundException if the source does not exist.
     */
    private static void requireFileCopy(final File source, final File destination) throws FileNotFoundException {
        requireExistsChecked(source, "source");
        Objects.requireNonNull(destination, "destination");
    }

    /**
     * Requires that the given {@code File} is a file if it exists.
     *
     * @param file The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does exists but is not a directory.
     */
    private static File requireFileIfExists(final File file, final String name) {
        Objects.requireNonNull(file, name);
        return file.exists() ? requireFile(file, name) : file;
    }


    /**
     * Sets the given {@code targetFile}'s last modified date to the given value.
     *
     * @param file The source file to query.
     * @param timeMillis The new last-modified time, measured in milliseconds since the epoch 01-01-1970 GMT.
     * @throws NullPointerException if file is {@code null}.
     * @throws IOException if setting the last-modified time failed.
     */
    private static void setLastModified(final File file, final long timeMillis) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.setLastModified(timeMillis)) {
            throw new IOException(String.format("Failed setLastModified(%s) on '%s'", timeMillis, file));
        }
    }


    /**
     * Converts from a {@code URL} to a {@code File}.
     * <p>
     * From version 1.1 this method will decode the URL.
     * Syntax such as {@code file:///my%20docs/file.txt} will be
     * correctly decoded to {@code /my docs/file.txt}. Starting with version
     * 1.5, this method uses UTF-8 to decode percent-encoded octets to characters.
     * Additionally, malformed percent-encoded octets are handled leniently by
     * passing them through literally.
     * </p>
     *
     * @param url the file URL to convert, {@code null} returns {@code null}
     * @return the equivalent {@code File} object, or {@code null}
     * if the URL's protocol is not {@code file}
     */
    public static File toFile(final URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        final String filename = url.getFile().replace('/', File.separatorChar);
        return new File(decodeUrl(filename));
    }

    /**
     * Converts each of an array of {@code URL} to a {@code File}.
     * <p>
     * Returns an array of the same size as the input.
     * If the input is {@code null}, an empty array is returned.
     * If the input contains {@code null}, the output array contains {@code null} at the same
     * index.
     * </p>
     * <p>
     * This method will decode the URL.
     * Syntax such as {@code file:///my%20docs/file.txt} will be
     * correctly decoded to {@code /my docs/file.txt}.
     * </p>
     *
     * @param urls the file URLs to convert, {@code null} returns empty array
     * @return a non-{@code null} array of Files matching the input, with a {@code null} item
     * if there was a {@code null} at that index in the input array
     * @throws IllegalArgumentException if any file is not a URL file
     * @throws IllegalArgumentException if any file is incorrectly encoded
     * @since 1.1
     */
    public static File[] toFiles(final URL... urls) {
        if (IOUtils.length(urls) == 0) {
            return EMPTY_FILE_ARRAY;
        }
        final File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            final URL url = urls[i];
            if (url != null) {
                if (!"file".equalsIgnoreCase(url.getProtocol())) {
                    throw new IllegalArgumentException("Can only convert file URL to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    /**
     * Converts whether or not to recurse into a recursion max depth.
     *
     * @param recursive whether or not to recurse
     * @return the recursion depth
     */
    private static int toMaxDepth(final boolean recursive) {
        return recursive ? Integer.MAX_VALUE : 1;
    }

    /**
     * Converts an array of file extensions to suffixes.
     *
     * @param extensions an array of extensions. Format: {"java", "xml"}
     * @return an array of suffixes. Format: {".java", ".xml"}
     * @throws NullPointerException if the parameter is null
     */
    private static String[] toSuffixes(final String... extensions) {
        Objects.requireNonNull(extensions, "extensions");
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    /**
     * Implements the same behavior as the "touch" utility on Unix. It creates
     * a new file with size 0 or, if the file exists already, it is opened and
     * closed without modifying it, but updating the file date and time.
     * <p>
     * NOTE: As from v1.3, this method throws an IOException if the last
     * modified date of the file cannot be set. Also, as from v1.3 this method
     * creates parent directories if they do not exist.
     * </p>
     *
     * @param file the File to touch.
     * @throws IOException if an I/O problem occurs.
     * @throws IOException if setting the last-modified time failed.
     */
    public static void touch(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            openOutputStream(file).close();
        }
        setLastModified(file, System.currentTimeMillis());
    }

    /**
     * Converts each of an array of {@code File} to a {@code URL}.
     * <p>
     * Returns an array of the same size as the input.
     * </p>
     *
     * @param files the files to convert, must not be {@code null}
     * @return an array of URLs matching the input
     * @throws IOException          if a file cannot be converted
     * @throws NullPointerException if the parameter is null
     */
    public static URL[] toURLs(final File... files) throws IOException {
        Objects.requireNonNull(files, "files");
        final URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    /**
     * Validates the given arguments.
     * <ul>
     * <li>Throws {@link NullPointerException} if {@code source} is null</li>
     * <li>Throws {@link NullPointerException} if {@code destination} is null</li>
     * <li>Throws {@link FileNotFoundException} if {@code source} does not exist</li>
     * </ul>
     *
     * @param source      the file or directory to be moved
     * @param destination the destination file or directory
     * @throws FileNotFoundException if {@code source} file does not exist
     */
    private static void validateMoveParameters(final File source, final File destination) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }
    }

    /**
     * Waits for NFS to propagate a file creation, imposing a timeout.
     * <p>
     * This method repeatedly tests {@link File#exists()} until it returns
     * true up to the maximum time specified in seconds.
     * </p>
     *
     * @param file    the file to check, must not be {@code null}
     * @param seconds the maximum time in seconds to wait
     * @return true if file exists
     * @throws NullPointerException if the file is {@code null}
     */
    public static boolean waitFor(final File file, final int seconds) {
        Objects.requireNonNull(file, "file");
        final long finishAtMillis = System.currentTimeMillis() + (seconds * 1000L);
        boolean wasInterrupted = false;
        try {
            while (!file.exists()) {
                final long remainingMillis = finishAtMillis -  System.currentTimeMillis();
                if (remainingMillis < 0){
                    return false;
                }
                try {
                    Thread.sleep(Math.min(100, remainingMillis));
                } catch (final InterruptedException ignore) {
                    wasInterrupted = true;
                } catch (final Exception ex) {
                    break;
                }
            }
        } finally {
            if (wasInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return true;
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 2.0
     * @deprecated 2.5 use {@link #write(File, CharSequence, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void write(final File file, final CharSequence data) throws IOException {
        write(file, data, Charset.defaultCharset(), false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file   the file to write
     * @param data   the content to write to the file
     * @param append if {@code true}, then the data will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     * @deprecated 2.5 use {@link #write(File, CharSequence, Charset, boolean)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void write(final File file, final CharSequence data, final boolean append) throws IOException {
        write(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the name of the requested charset, {@code null} means platform default
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void write(final File file, final CharSequence data, final Charset charset) throws IOException {
        write(file, data, charset, false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append   if {@code true}, then the data will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void write(final File file, final CharSequence data, final Charset charset, final boolean append)
            throws IOException {
        writeStringToFile(file, Objects.toString(data, null), charset, append);
    }

    // Private method, must be invoked will a directory parameter

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.0
     */
    public static void write(final File file, final CharSequence data, final String charsetName) throws IOException {
        write(file, data, charsetName, false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append   if {@code true}, then the data will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                 in case of an I/O error
     * @throws UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void write(final File file, final CharSequence data, final String charsetName, final boolean append)
            throws IOException {
        write(file, data, Charsets.toCharset(charsetName), append);
    }

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 1.1
     */
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    // Must be called with a directory

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append)
            throws IOException {
        writeByteArrayToFile(file, data, 0, data.length, append);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting
     * at offset {@code off} to a file, creating the file if it does
     * not exist.
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @param off  the start offset in the data
     * @param len  the number of bytes to write
     * @throws IOException in case of an I/O error
     * @since 2.5
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len)
            throws IOException {
        writeByteArrayToFile(file, data, off, len, false);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting
     * at offset {@code off} to a file, creating the file if it does
     * not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param off    the start offset in the data
     * @param len    the number of bytes to write
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.5
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len,
                                            final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data, off, len);
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file  the file to write to
     * @param lines the lines to write, {@code null} entries produce blank lines
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file   the file to write to
     * @param lines  the lines to write, {@code null} entries produce blank lines
     * @param append if {@code true}, then the lines will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final boolean append) throws IOException {
        writeLines(file, null, lines, null, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding)
            throws IOException {
        writeLines(file, null, lines, lineEnding, false);
    }


    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @param append     if {@code true}, then the lines will be added to the
     *                   end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding,
                                  final boolean append) throws IOException {
        writeLines(file, null, lines, lineEnding, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the default line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines    the lines to write, {@code null} entries produce blank lines
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines)
            throws IOException {
        writeLines(file, charsetName, lines, null, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line, optionally appending.
     * The specified character encoding and the default line ending will be used.
     *
     * @param file     the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines    the lines to write, {@code null} entries produce blank lines
     * @param append   if {@code true}, then the lines will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final boolean append) throws IOException {
        writeLines(file, charsetName, lines, null, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file       the file to write to
     * @param charsetName   the name of the requested charset, {@code null} means platform default
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final String lineEnding) throws IOException {
        writeLines(file, charsetName, lines, lineEnding, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the line ending will be used.
     *
     * @param file       the file to write to
     * @param charsetName   the name of the requested charset, {@code null} means platform default
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @param append     if {@code true}, then the lines will be added to the
     *                   end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final String lineEnding, final boolean append) throws IOException {
        try (OutputStream out = new BufferedOutputStream(openOutputStream(file, append))) {
            IOUtils.writeLines(lines, lineEnding, out, charsetName);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @deprecated 2.5 use {@link #writeStringToFile(File, String, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void writeStringToFile(final File file, final String data) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file   the file to write
     * @param data   the content to write to the file
     * @param append if {@code true}, then the String will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     * @deprecated 2.5 use {@link #writeStringToFile(File, String, Charset, boolean)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void writeStringToFile(final File file, final String data, final boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.4
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset)
            throws IOException {
        writeStringToFile(file, data, charset, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset,
                                         final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            IOUtils.write(data, out, charset);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeStringToFile(final File file, final String data, final String charsetName) throws IOException {
        writeStringToFile(file, data, charsetName, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                 in case of an I/O error
     * @throws UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeStringToFile(final File file, final String data, final String charsetName,
                                         final boolean append) throws IOException {
        writeStringToFile(file, data, Charsets.toCharset(charsetName), append);
    }

    /**
     * Instances should NOT be constructed in standard programming.
     * @deprecated Will be private in 3.0.
     */
    @Deprecated
    public FileUtils() { //NOSONAR

    }
}
