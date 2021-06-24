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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * General IO stream manipulation utilities.
 * <p>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li><b>[Deprecated]</b> closeQuietly - these methods close a stream ignoring nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p>
 * The byte-to-char methods and char-to-byte methods involve a conversion step.
 * Two methods are provided in each case, one that uses the platform default
 * encoding and the other which allows you to specify an encoding. You are
 * encouraged to always specify an encoding because relying on the platform
 * default can lead to unexpected results, for example when moving from
 * development to production.
 * <p>
 * All the methods in this class that read a stream are buffered internally.
 * This means that there is no cause to use a {@code BufferedInputStream}
 * or {@code BufferedReader}. The default buffer size of 4K has been shown
 * to be efficient in tests.
 * <p>
 * The various copy methods all delegate the actual copying to one of the following methods:

 * For example, {@link #copy(InputStream, OutputStream)} calls {@link #copyLarge(InputStream, OutputStream)}
 * which calls {@link #copy(InputStream, OutputStream, int)} which creates the buffer and calls
 * {@link #copyLarge(InputStream, OutputStream, byte[])}.
 * <p>
 * Applications can re-use buffers by using the underlying methods directly.
 * This may improve performance for applications that need to do a lot of copying.
 * <p>
 * Wherever possible, the methods in this class do <em>not</em> flush or close
 * the stream. This is to avoid making non-portable assumptions about the
 * streams' origin and further use. Thus the caller is still responsible for
 * closing streams after use.
 * <p>
 * Origin of code: Excalibur.
 */
public class IOUtils {
    // NOTE: This class is focused on InputStream, OutputStream, Reader and
    // Writer. Each method should take at least one of these as a parameter,
    // or return one of them.

    /**
     * CR char.
     *
     * @since 2.9.0
     */
    public static final int CR = '\r';

    /**
     * The default buffer size ({@value}) to use in copy methods.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The system directory separator character.
     */
    public static final char DIR_SEPARATOR = File.separatorChar;

    /**
     * The Unix directory separator character.
     */
    public static final char DIR_SEPARATOR_UNIX = '/';

    /**
     * The Windows directory separator character.
     */
    public static final char DIR_SEPARATOR_WINDOWS = '\\';

    /**
     * A singleton empty byte array.
     *
     *  @since 2.9.0
     */
    public static final byte[] EMPTY_BYTE_ARRAY = {};

    /**
     * Represents the end-of-file (or stream).
     * @since 2.5 (made public)
     */
    public static final int EOF = -1;

    /**
     * LF char.
     *
     * @since 2.9.0
     */
    public static final int LF = '\n';

    /**
     * The system line separator string.
     *
     * @deprecated Use {@link System#lineSeparator()}.
     */
    @Deprecated
    public static final String LINE_SEPARATOR = System.lineSeparator();


    /**
     * Returns the given InputStream if it is already a {@link BufferedInputStream}, otherwise creates a
     * BufferedInputStream from the given InputStream.
     *
     * @param inputStream the InputStream to wrap or return (not null)
     * @return the given InputStream or a new {@link BufferedInputStream} for the given InputStream
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedInputStream buffer(final InputStream inputStream) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }

    /**
     * Returns the given InputStream if it is already a {@link BufferedInputStream}, otherwise creates a
     * BufferedInputStream from the given InputStream.
     *
     * @param inputStream the InputStream to wrap or return (not null)
     * @param size the buffer size, if a new BufferedInputStream is created.
     * @return the given InputStream or a new {@link BufferedInputStream} for the given InputStream
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedInputStream buffer(final InputStream inputStream, final int size) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, size);
    }

    /**
     * Returns the given OutputStream if it is already a {@link BufferedOutputStream}, otherwise creates a
     * BufferedOutputStream from the given OutputStream.
     *
     * @param outputStream the OutputStream to wrap or return (not null)
     * @return the given OutputStream or a new {@link BufferedOutputStream} for the given OutputStream
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedOutputStream buffer(final OutputStream outputStream) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }

    /**
     * Returns the given OutputStream if it is already a {@link BufferedOutputStream}, otherwise creates a
     * BufferedOutputStream from the given OutputStream.
     *
     * @param outputStream the OutputStream to wrap or return (not null)
     * @param size the buffer size, if a new BufferedOutputStream is created.
     * @return the given OutputStream or a new {@link BufferedOutputStream} for the given OutputStream
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @SuppressWarnings("resource") // parameter null check
    public static BufferedOutputStream buffer(final OutputStream outputStream, final int size) {
        // reject null early on rather than waiting for IO operation to fail
        // not checked by BufferedInputStream
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream, size);
    }

    /**
     * Returns the given reader if it is already a {@link BufferedReader}, otherwise creates a BufferedReader from
     * the given reader.
     *
     * @param reader the reader to wrap or return (not null)
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    public static BufferedReader buffer(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Returns the given reader if it is already a {@link BufferedReader}, otherwise creates a BufferedReader from the
     * given reader.
     *
     * @param reader the reader to wrap or return (not null)
     * @param size the buffer size, if a new BufferedReader is created.
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    public static BufferedReader buffer(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    /**
     * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise creates a BufferedWriter from the
     * given Writer.
     *
     * @param writer the Writer to wrap or return (not null)
     * @return the given Writer or a new {@link BufferedWriter} for the given Writer
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    public static BufferedWriter buffer(final Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    /**
     * Returns the given Writer if it is already a {@link BufferedWriter}, otherwise creates a BufferedWriter from the
     * given Writer.
     *
     * @param writer the Writer to wrap or return (not null)
     * @param size the buffer size, if a new BufferedWriter is created.
     * @return the given Writer or a new {@link BufferedWriter} for the given Writer
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    public static BufferedWriter buffer(final Writer writer, final int size) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
    }

    /**
     * Returns a new byte array of size {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a new byte array of size {@link #DEFAULT_BUFFER_SIZE}.
     * @since 2.9.0
     */
    public static byte[] byteArray() {
        return byteArray(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a new byte array of the given size.
     *
     * TODO Consider guarding or warning against large allocations...
     *
     * @param size array size.
     * @return a new byte array of the given size.
     * @since 2.9.0
     */
    public static byte[] byteArray(final int size) {
        return new byte[size];
    }

    /**
     * Returns a new char array of size {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a new char array of size {@link #DEFAULT_BUFFER_SIZE}.
     * @since 2.9.0
     */
    private static char[] charArray() {
        return charArray(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a new char array of the given size.
     *
     * TODO Consider guarding or warning against large allocations...
     *
     * @param size array size.
     * @return a new char array of the given size.
     * @since 2.9.0
     */
    private static char[] charArray(final int size) {
        return new char[size];
    }

    /**
     * Closes the given {@link Closeable} as a null-safe operation.
     *
     * @param closeable The resource to close, may be null.
     * @throws IOException if an I/O error occurs.
     * @since 2.7
     */
    public static void close(final Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    /**
     * Closes the given {@link Closeable} as a null-safe operation.
     *
     * @param closeables The resource(s) to close, may be null.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static void close(final Closeable... closeables) throws IOException {
        if (closeables != null) {
            for (final Closeable closeable : closeables) {
                close(closeable);
            }
        }
    }

    /**
     * Closes a URLConnection.
     *
     * @param conn the connection to close.
     * @since 2.4
     */
    public static void close(final URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }


    /**
     * Closes a {@code Closeable} unconditionally.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * <p>
     * This is typically used in finally blocks to ensure that the closeable is closed
     * even if an Exception was thrown before the normal close statement was reached.
     * <br>
     * <b>It should not be used to replace the close statement(s)
     * which should be present for the non-exceptional case.</b>
     * <br>
     * It is only intended to simplify tidying up where normal processing has already failed
     * and reporting close failure as well is not necessary or useful.
     * <p>
     * Example code:
     * </p>
     * <pre>
     * Closeable closeable = null;
     * try {
     *     closeable = new FileReader(&quot;foo.txt&quot;);
     *     // processing using the closeable; may throw an Exception
     *     closeable.close(); // Normal close - exceptions not ignored
     * } catch (Exception e) {
     *     // error handling
     * } finally {
     *     <b>IOUtils.closeQuietly(closeable); // In case normal close was skipped due to Exception</b>
     * }
     * </pre>
     * <p>
     * Closing all streams:
     * <br>
     * <pre>
     * try {
     *     return IOUtils.copy(inputStream, outputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream, outputStream);
     * }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     * @param closeables the objects to close, may be null or already closed
     * @since 2.5
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (final Closeable closeable : closeables) {
            closeQuietly(closeable);
        }
    }


    /**
     * Closes an {@code InputStream} unconditionally.
     * <p>
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   byte[] data = new byte[1024];
     *   InputStream in = null;
     *   try {
     *       in = new FileInputStream("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param input the InputStream to close, may be null or already closed
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final InputStream input) {
        closeQuietly((Closeable) input);
    }

    /**
     * Closes an {@code OutputStream} unconditionally.
     * <p>
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     * byte[] data = "Hello, World".getBytes();
     *
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream("foo.txt");
     *     out.write(data);
     *     out.close(); //close errors are handled
     * } catch (IOException e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(out);
     * }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param output the OutputStream to close, may be null or already closed
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final OutputStream output) {
        closeQuietly((Closeable) output);
    }

    /**
     * Closes an {@code Reader} unconditionally.
     * <p>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   char[] data = new char[1024];
     *   Reader in = null;
     *   try {
     *       in = new FileReader("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param reader the Reader to close, may be null or already closed
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final Reader reader) {
        closeQuietly((Closeable) reader);
    }

    /**
     * Closes a {@code Selector} unconditionally.
     * <p>
     * Equivalent to {@link Selector#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Selector selector = null;
     *   try {
     *       selector = Selector.open();
     *       // process socket
     *
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(selector);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param selector the Selector to close, may be null or already closed
     * @since 2.2
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final Selector selector) {
        closeQuietly((Closeable) selector);
    }

    /**
     * Closes a {@code ServerSocket} unconditionally.
     * <p>
     * Equivalent to {@link ServerSocket#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   ServerSocket socket = null;
     *   try {
     *       socket = new ServerSocket();
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param serverSocket the ServerSocket to close, may be null or already closed
     * @since 2.2
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final ServerSocket serverSocket) {
        closeQuietly((Closeable) serverSocket);
    }

    /**
     * Closes a {@code Socket} unconditionally.
     * <p>
     * Equivalent to {@link Socket#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Socket socket = null;
     *   try {
     *       socket = new Socket("http://www.foo.com/", 80);
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param socket the Socket to close, may be null or already closed
     * @since 2.0
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final Socket socket) {
        closeQuietly((Closeable) socket);
    }

    /**
     * Closes an {@code Writer} unconditionally.
     * <p>
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Writer out = null;
     *   try {
     *       out = new StringWriter();
     *       out.write("Hello World");
     *       out.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(out);
     *   }
     * </pre>
     * <p>
     * Also consider using a try-with-resources statement where appropriate.
     * </p>
     *
     * @param writer the Writer to close, may be null or already closed
     * @see Throwable#addSuppressed(Throwable)
     */
    public static void closeQuietly(final Writer writer) {
        closeQuietly((Closeable) writer);
    }


    /**
     * Compares the contents of two Readers to determine if they are equal or
     * not, ignoring EOL characters.
     * <p>
     * This method buffers the input internally using
     * {@code BufferedReader} if they are not already buffered.
     *
     * @param reader1 the first reader
     * @param reader2 the second reader
     * @return true if the content of the readers are equal (ignoring EOL differences),  false otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    @SuppressWarnings("resource")
    public static boolean contentEqualsIgnoreEOL(final Reader reader1, final Reader reader2)
            throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }
        final BufferedReader br1 = toBufferedReader(reader1);
        final BufferedReader br2 = toBufferedReader(reader2);

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while (line1 != null && line1.equals(line2)) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return Objects.equals(line1, line2);
    }

    /**
     * Copies bytes from an {@code InputStream} to an {@code OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     * </p>
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of {@code -1} after the copy has completed since
     * the correct number of bytes cannot be returned as an int. For large streams use the
     * {@code copyLarge(InputStream, OutputStream)} method.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read.
     * @param outputStream the {@code OutputStream} to write.
     * @return the number of bytes copied, or -1 if greater than {@link Integer#MAX_VALUE}.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 1.1
     */
    public static int copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final long count = copyLarge(inputStream, outputStream);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

    /**
     * Copies bytes from an {@code InputStream} to an {@code OutputStream} using an internal buffer of the
     * given size.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read.
     * @param outputStream the {@code OutputStream} to write to
     * @param bufferSize the bufferSize used to copy from the input to the output
     * @return the number of bytes copied.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.5
     */
    public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize)
            throws IOException {
        return copyLarge(inputStream, outputStream, IOUtils.byteArray(bufferSize));
    }

    /**
     * Copies bytes from an {@code InputStream} to chars on a
     * {@code Writer} using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * <p>
     * This method uses {@link InputStreamReader}.
     *
     * @param input the {@code InputStream} to read from
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #copy(InputStream, Writer, Charset)} instead
     */
    @Deprecated
    public static void copy(final InputStream input, final Writer writer)
            throws IOException {
        copy(input, writer, Charset.defaultCharset());
    }

    /**
     * Copies bytes from an {@code InputStream} to chars on a
     * {@code Writer} using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * <p>
     * This method uses {@link InputStreamReader}.
     *
     * @param input the {@code InputStream} to read from
     * @param writer the {@code Writer} to write to
     * @param inputCharset the charset to use for the input stream, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void copy(final InputStream input, final Writer writer, final Charset inputCharset)
            throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(inputCharset));
        copy(reader, writer);
    }

    /**
     * Copies bytes from an {@code InputStream} to chars on a
     * {@code Writer} using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link InputStreamReader}.
     *
     * @param input the {@code InputStream} to read from
     * @param writer the {@code Writer} to write to
     * @param inputCharsetName the name of the requested charset for the InputStream, null means platform default
     * @throws NullPointerException                         if the input or output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void copy(final InputStream input, final Writer writer, final String inputCharsetName)
            throws IOException {
        copy(input, writer, Charsets.toCharset(inputCharsetName));
    }

    /**
     * Copies chars from a {@code Reader} to a {@code Appendable}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     * <p>
     * Large streams (over 2GB) will return a chars copied value of
     * {@code -1} after the copy has completed since the correct
     * number of chars cannot be returned as an int. For large streams
     * use the {@code copyLarge(Reader, Writer)} method.
     *
     * @param reader the {@code Reader} to read from
     * @param output the {@code Appendable} to write to
     * @return the number of characters copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.7
     */
    public static long copy(final Reader reader, final Appendable output) throws IOException {
        return copy(reader, output, CharBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    /**
     * Copies chars from a {@code Reader} to an {@code Appendable}.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@code BufferedReader}.
     * </p>
     *
     * @param reader the {@code Reader} to read from
     * @param output the {@code Appendable} to write to
     * @param buffer the buffer to be used for the copy
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.7
     */
    public static long copy(final Reader reader, final Appendable output, final CharBuffer buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            buffer.flip();
            output.append(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copies chars from a {@code Reader} to bytes on an
     * {@code OutputStream} using the default character encoding of the
     * platform, and calling flush.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     * <p>
     * Due to the implementation of OutputStreamWriter, this method performs a
     * flush.
     * <p>
     * This method uses {@link OutputStreamWriter}.
     *
     * @param reader the {@code Reader} to read from
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #copy(Reader, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void copy(final Reader reader, final OutputStream output)
            throws IOException {
        copy(reader, output, Charset.defaultCharset());
    }

    /**
     * Copies chars from a {@code Reader} to bytes on an
     * {@code OutputStream} using the specified character encoding, and
     * calling flush.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     * </p>
     * <p>
     * Due to the implementation of OutputStreamWriter, this method performs a
     * flush.
     * </p>
     * <p>
     * This method uses {@link OutputStreamWriter}.
     * </p>
     *
     * @param reader the {@code Reader} to read from
     * @param output the {@code OutputStream} to write to
     * @param outputCharset the charset to use for the OutputStream, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void copy(final Reader reader, final OutputStream output, final Charset outputCharset)
            throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.toCharset(outputCharset));
        copy(reader, writer);
        // XXX Unless anyone is planning on rewriting OutputStreamWriter,
        // we have to flush here.
        writer.flush();
    }

    /**
     * Copies chars from a {@code Reader} to bytes on an
     * {@code OutputStream} using the specified character encoding, and
     * calling flush.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * Due to the implementation of OutputStreamWriter, this method performs a
     * flush.
     * <p>
     * This method uses {@link OutputStreamWriter}.
     *
     * @param reader the {@code Reader} to read from
     * @param output the {@code OutputStream} to write to
     * @param outputCharsetName the name of the requested charset for the OutputStream, null means platform default
     * @throws NullPointerException                         if the input or output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void copy(final Reader reader, final OutputStream output, final String outputCharsetName)
            throws IOException {
        copy(reader, output, Charsets.toCharset(outputCharsetName));
    }


    /**
     * Copies bytes from a {@code URL} to an {@code OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param url the {@code URL} to read.
     * @param file the {@code OutputStream} to write.
     * @return the number of bytes copied.
     * @throws NullPointerException if the URL is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long copy(final URL url, final File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(Objects.requireNonNull(file, "file"))) {
            return copy(url, outputStream);
        }
    }

    /**
     * Copies bytes from a {@code URL} to an {@code OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param url the {@code URL} to read.
     * @param outputStream the {@code OutputStream} to write.
     * @return the number of bytes copied.
     * @throws NullPointerException if the URL is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long copy(final URL url, final OutputStream outputStream) throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(url, "url").openStream()) {
            return copyLarge(inputStream, outputStream);
        }
    }

    /**
     * Copies bytes from a large (over 2GB) {@code InputStream} to an
     * {@code OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read.
     * @param outputStream the {@code OutputStream} to write.
     * @return the number of bytes copied.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 1.3
     */
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream)
            throws IOException {
        return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from a large (over 2GB) {@code InputStream} to an
     * {@code OutputStream}.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@code BufferedInputStream}.
     * </p>
     *
     * @param inputStream the {@code InputStream} to read.
     * @param outputStream the {@code OutputStream} to write.
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if an I/O error occurs.
     * @since 2.2
     */
    @SuppressWarnings("resource") // streams are closed by the caller.
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer)
        throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(outputStream, "outputStream");
        long count = 0;
        int n;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    /**
     * Copies chars from a large (over 2GB) {@code Reader} to a {@code Writer}.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * {@code BufferedReader}.
     * <p>
     *
     * @param reader the {@code Reader} to source.
     * @param writer the {@code Writer} to target.
     * @param buffer the buffer to be used for the copy
     * @return the number of characters copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    public static long copyLarge(final Reader reader, final Writer writer, final char[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    /**
     * Returns the length of the given array in a null-safe manner.
     *
     * @param array an array or null
     * @return the array length -- or 0 if the given array is null.
     * @since 2.7
     */
    public static int length(final byte[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the length of the given array in a null-safe manner.
     *
     * @param array an array or null
     * @return the array length -- or 0 if the given array is null.
     * @since 2.7
     */
    public static int length(final char[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the length of the given CharSequence in a null-safe manner.
     *
     * @param csq a CharSequence or null
     * @return the CharSequence length -- or 0 if the given CharSequence is null.
     * @since 2.7
     */
    public static int length(final CharSequence csq) {
        return csq == null ? 0 : csq.length();
    }

    /**
     * Returns the length of the given array in a null-safe manner.
     *
     * @param array an array or null
     * @return the array length -- or 0 if the given array is null.
     * @since 2.7
     */
    public static int length(final Object[] array) {
        return array == null ? 0 : array.length;
    }



    /**
     * Reads bytes from an input stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link InputStream}.
     *
     * @param input where to read input from
     * @param buffer destination
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final InputStream input, final byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from an input stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link InputStream}.
     *
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = input.read(buffer, offset + location, remaining);
            if (EOF == count) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    /**
     * Reads bytes from a ReadableByteChannel.
     * <p>
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link ReadableByteChannel}.
     *
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @return the actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.5
     */
    public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int length = buffer.remaining();
        while (buffer.remaining() > 0) {
            final int count = input.read(buffer);
            if (EOF == count) { // EOF
                break;
            }
        }
        return length - buffer.remaining();
    }

    /**
     * Reads characters from an input character stream.
     * This implementation guarantees that it will read as many characters
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link Reader}.
     *
     * @param reader where to read input from
     * @param buffer destination
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final Reader reader, final char[] buffer) throws IOException {
        return read(reader, buffer, 0, buffer.length);
    }

    /**
     * Reads characters from an input character stream.
     * This implementation guarantees that it will read as many characters
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link Reader}.
     *
     * @param reader where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = reader.read(buffer, offset + location, remaining);
            if (EOF == count) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     *
     * @param input where to read input from
     * @param buffer destination
     *
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.2
     */
    public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
        readFully(input, buffer, 0, buffer.length);
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     *
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     *
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.2
     */
    public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link InputStream#read(byte[], int, int)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     *
     * @param input where to read input from
     * @param length length to read, must be &gt;= 0
     * @return the bytes read from input
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of bytes read was incorrect
     * @since 2.5
     */
    public static byte[] readFully(final InputStream input, final int length) throws IOException {
        final byte[] buffer = IOUtils.byteArray(length);
        readFully(input, buffer, 0, buffer.length);
        return buffer;
    }

    /**
     * Reads the requested number of bytes or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link ReadableByteChannel#read(ByteBuffer)} may
     * not read as many bytes as requested (most likely because of reaching EOF).
     *
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @throws IOException  if there is a problem reading the file
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.5
     */
    public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int expected = buffer.remaining();
        final int actual = read(input, buffer);
        if (actual != expected) {
            throw new EOFException("Length to read: " + expected + " actual: " + actual);
        }
    }

    /**
     * Reads the requested number of characters or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link Reader#read(char[], int, int)} may
     * not read as many characters as requested (most likely because of reaching EOF).
     *
     * @param reader where to read input from
     * @param buffer destination
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of characters read was incorrect
     * @since 2.2
     */
    public static void readFully(final Reader reader, final char[] buffer) throws IOException {
        readFully(reader, buffer, 0, buffer.length);
    }

    /**
     * Reads the requested number of characters or fail if there are not enough left.
     * <p>
     * This allows for the possibility that {@link Reader#read(char[], int, int)} may
     * not read as many characters as requested (most likely because of reaching EOF).
     *
     * @param reader where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException             if the number of characters read was incorrect
     * @since 2.2
     */
    public static void readFully(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(reader, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param input the {@code InputStream} to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #readLines(InputStream, Charset)} instead
     */
    @Deprecated
    public static List<String> readLines(final InputStream input) throws IOException {
        return readLines(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param input the {@code InputStream} to read from, not null
     * @param charset the charset to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static List<String> readLines(final InputStream input, final Charset charset) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(charset));
        return readLines(reader);
    }

    /**
     * Gets the contents of an {@code InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param input the {@code InputStream} to read from, not null
     * @param charsetName the name of the requested charset, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException                         if the input is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static List<String> readLines(final InputStream input, final String charsetName) throws IOException {
        return readLines(input, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@code Reader} as a list of Strings,
     * one entry per line.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     *
     * @param reader the {@code Reader} to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    @SuppressWarnings("resource") // reader wraps input and is the responsibility of the caller.
    public static List<String> readLines(final Reader reader) throws IOException {
        final BufferedReader bufReader = toBufferedReader(reader);
        final List<String> list = new ArrayList<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            list.add(line);
        }
        return list;
    }


    /**
     * Gets a URL pointing to the given classpath resource.
     *
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name name of the desired resource
     * @return the requested URL
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static URL resourceToURL(final String name) throws IOException {
        return resourceToURL(name, null);
    }

    /**
     * Gets a URL pointing to the given classpath resource.
     *
     * <p>
     * It is expected the given {@code name} to be absolute. The
     * behavior is not well-defined otherwise.
     * </p>
     *
     * @param name        name of the desired resource
     * @param classLoader the class loader that the resolution of the resource is delegated to
     * @return the requested URL
     * @throws IOException if an I/O error occurs.
     *
     * @since 2.6
     */
    public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
        // What about the thread context class loader?
        // What about the system class loader?
        final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);

        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }

        return resource;
    }

    /**
     * Skips bytes from a ReadableByteChannel.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up.
     *
     * @param input ReadableByteChannel to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException              if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @since 2.5
     */
    public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        final ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, DEFAULT_BUFFER_SIZE));
        long remain = toSkip;
        while (remain > 0) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int) Math.min(remain, DEFAULT_BUFFER_SIZE));
            final int n = input.read(skipByteBuffer);
            if (n == EOF) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }



    /**
     * Skips the requested number of bytes or fail if there are not enough left.
     *
     * @param input ReadableByteChannel to skip
     * @param toSkip the number of bytes to skip
     * @throws IOException              if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException             if the number of bytes skipped was incorrect
     * @since 2.5
     */
    public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }


    /**
     * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a BufferedReader from the given
     * reader.
     *
     * @param reader the reader to wrap or return (not null)
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @throws NullPointerException if the input parameter is null
     * @see #buffer(Reader)
     * @since 2.2
     */
    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Returns the given reader if it is a {@link BufferedReader}, otherwise creates a BufferedReader from the given
     * reader.
     *
     * @param reader the reader to wrap or return (not null)
     * @param size the buffer size, if a new BufferedReader is created.
     * @return the given reader or a new {@link BufferedReader} for the given reader
     * @throws NullPointerException if the input parameter is null
     * @see #buffer(Reader)
     * @since 2.5
     */
    public static BufferedReader toBufferedReader(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }


    /**
     * Gets the contents of an {@code InputStream} as a {@code byte[]}. Use this method instead of
     * {@code toByteArray(InputStream)} when {@code InputStream} size is known
     *
     * @param input the {@code InputStream} to read.
     * @param size the size of {@code InputStream}.
     * @return the requested byte array.
     * @throws IOException if an I/O error occurs or {@code InputStream} size differ from parameter size.
     * @throws IllegalArgumentException if size is less than zero.
     * @since 2.1
     */
    public static byte[] toByteArray(final InputStream input, final int size) throws IOException {

        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }

        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        final byte[] data = IOUtils.byteArray(size);
        int offset = 0;
        int read;

        while (offset < size && (read = input.read(data, offset, size - offset)) != EOF) {
            offset += read;
        }

        if (offset != size) {
            throw new IOException("Unexpected read size, current: " + offset + ", expected: " + size);
        }

        return data;
    }

    /**
     * Gets contents of an {@code InputStream} as a {@code byte[]}.
     * Use this method instead of {@code toByteArray(InputStream)}
     * when {@code InputStream} size is known.
     * <b>NOTE:</b> the method checks that the length can safely be cast to an int without truncation
     * before using {@link IOUtils#toByteArray(InputStream, int)} to read into the byte array.
     * (Arrays can have no more than Integer.MAX_VALUE entries anyway)
     *
     * @param input the {@code InputStream} to read from
     * @param size the size of {@code InputStream}
     * @return the requested byte array
     * @throws IOException              if an I/O error occurs or {@code InputStream} size differ from parameter
     * size
     * @throws IllegalArgumentException if size is less than zero or size is greater than Integer.MAX_VALUE
     * @see IOUtils#toByteArray(InputStream, int)
     * @since 2.1
     */
    public static byte[] toByteArray(final InputStream input, final long size) throws IOException {

        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }

        return toByteArray(input, (int) size);
    }


    /**
     * Gets the contents of a {@code String} as a {@code byte[]}
     * using the default character encoding of the platform.
     * <p>
     * This is the same as {@link String#getBytes()}.
     *
     * @param input the {@code String} to convert
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException Never thrown.
     * @deprecated 2.5 Use {@link String#getBytes()} instead
     */
    @Deprecated
    public static byte[] toByteArray(final String input) throws IOException {
        // make explicit the use of the default charset
        return input.getBytes(Charset.defaultCharset());
    }


    /**
     * Gets the contents of an {@code InputStream} as a character array
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param inputStream the {@code InputStream} to read from
     * @return the requested character array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #toCharArray(InputStream, Charset)} instead
     */
    @Deprecated
    public static char[] toCharArray(final InputStream inputStream) throws IOException {
        return toCharArray(inputStream, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@code InputStream} as a character array
     * using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param inputStream the {@code InputStream} to read from
     * @param charset the charset to use, null means platform default
     * @return the requested character array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static char[] toCharArray(final InputStream inputStream, final Charset charset)
            throws IOException {
        final CharArrayWriter writer = new CharArrayWriter();
        copy(inputStream, writer, charset);
        return writer.toCharArray();
    }

    /**
     * Gets the contents of an {@code InputStream} as a character array
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedInputStream}.
     *
     * @param inputStream the {@code InputStream} to read from
     * @param charsetName the name of the requested charset, null means platform default
     * @return the requested character array
     * @throws NullPointerException                         if the input is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static char[] toCharArray(final InputStream inputStream, final String charsetName) throws IOException {
        return toCharArray(inputStream, Charsets.toCharset(charsetName));
    }

    /**
     * Gets the contents of a {@code Reader} as a character array.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * {@code BufferedReader}.
     *
     * @param reader the {@code Reader} to read from
     * @return the requested character array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static char[] toCharArray(final Reader reader) throws IOException {
        final CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toCharArray();
    }

    /**
     * Converts the specified CharSequence to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the CharSequence to convert
     * @return an input stream
     * @since 2.0
     * @deprecated 2.5 use {@link #toInputStream(CharSequence, Charset)} instead
     */
    @Deprecated
    public static InputStream toInputStream(final CharSequence input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    /**
     * Converts the specified CharSequence to an input stream, encoded as bytes
     * using the specified character encoding.
     *
     * @param input the CharSequence to convert
     * @param charset the charset to use, null means platform default
     * @return an input stream
     * @since 2.3
     */
    public static InputStream toInputStream(final CharSequence input, final Charset charset) {
        return toInputStream(input.toString(), charset);
    }

    /**
     * Converts the specified CharSequence to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input the CharSequence to convert
     * @param charsetName the name of the requested charset, null means platform default
     * @return an input stream
     * @throws IOException Never thrown.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 2.0
     */
    public static InputStream toInputStream(final CharSequence input, final String charsetName) throws IOException {
        return toInputStream(input, Charsets.toCharset(charsetName));
    }

    /**
     * Converts the specified string to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the string to convert
     * @return an input stream
     * @since 1.1
     * @deprecated 2.5 use {@link #toInputStream(String, Charset)} instead
     */
    @Deprecated
    public static InputStream toInputStream(final String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    /**
     * Converts the specified string to an input stream, encoded as bytes
     * using the specified character encoding.
     *
     * @param input the string to convert
     * @param charset the charset to use, null means platform default
     * @return an input stream
     * @since 2.3
     */
    public static InputStream toInputStream(final String input, final Charset charset) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charset)));
    }

    /**
     * Converts the specified string to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input the string to convert
     * @param charsetName the name of the requested charset, null means platform default
     * @return an input stream
     * @throws IOException Never thrown.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static InputStream toInputStream(final String input, final String charsetName) throws IOException {
        final byte[] bytes = input.getBytes(Charsets.toCharset(charsetName));
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Gets the contents of a {@code byte[]} as a String
     * using the default character encoding of the platform.
     *
     * @param input the byte array to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException Never thrown.
     * @deprecated 2.5 Use {@link String#String(byte[])} instead
     */
    @Deprecated
    public static String toString(final byte[] input) throws IOException {
        // make explicit the use of the default charset
        return new String(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of a {@code byte[]} as a String
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input the byte array to read from
     * @param charsetName the name of the requested charset, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException Never thrown.
     */
    public static String toString(final byte[] input, final String charsetName) throws IOException {
        return new String(input, Charsets.toCharset(charsetName));
    }


    /**
     * Writes bytes from a {@code byte[]} to an {@code OutputStream}.
     *
     * @param data the byte array to write, do not modify during output,
     * null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static void write(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@code Writer}
     * using the default character encoding of the platform.
     * <p>
     * This method uses {@link String#String(byte[])}.
     *
     * @param data the byte array to write, do not modify during output,
     * null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #write(byte[], Writer, Charset)} instead
     */
    @Deprecated
    public static void write(final byte[] data, final Writer writer) throws IOException {
        write(data, writer, Charset.defaultCharset());
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@code Writer}
     * using the specified character encoding.
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     *
     * @param data the byte array to write, do not modify during output,
     * null ignored
     * @param writer the {@code Writer} to write to
     * @param charset the charset to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void write(final byte[] data, final Writer writer, final Charset charset) throws IOException {
        if (data != null) {
            writer.write(new String(data, Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to chars on a {@code Writer}
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     *
     * @param data the byte array to write, do not modify during output,
     * null ignored
     * @param writer the {@code Writer} to write to
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException                         if output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void write(final byte[] data, final Writer writer, final String charsetName) throws IOException {
        write(data, writer, Charsets.toCharset(charsetName));
    }

    /**
     * Writes chars from a {@code char[]} to bytes on an
     * {@code OutputStream}.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes()}.
     *
     * @param data the char array to write, do not modify during output,
     * null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #write(char[], OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final char[] data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a {@code char[]} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     *
     * @param data the char array to write, do not modify during output,
     * null ignored
     * @param output the {@code OutputStream} to write to
     * @param charset the charset to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void write(final char[] data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes chars from a {@code char[]} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     *
     * @param data the char array to write, do not modify during output,
     * null ignored
     * @param output the {@code OutputStream} to write to
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException                         if output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    public static void write(final char[] data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes chars from a {@code char[]} to a {@code Writer}
     *
     * @param data the char array to write, do not modify during output,
     * null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static void write(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    /**
     * Writes chars from a {@code CharSequence} to bytes on an
     * {@code OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     *
     * @param data the {@code CharSequence} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.0
     * @deprecated 2.5 use {@link #write(CharSequence, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final CharSequence data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a {@code CharSequence} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data the {@code CharSequence} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @param charset the charset to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void write(final CharSequence data, final OutputStream output, final Charset charset)
            throws IOException {
        if (data != null) {
            write(data.toString(), output, charset);
        }
    }

    /**
     * Writes chars from a {@code CharSequence} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data the {@code CharSequence} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException        if output is null
     * @throws IOException                 if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 2.0
     */
    public static void write(final CharSequence data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes chars from a {@code CharSequence} to a {@code Writer}.
     *
     * @param data the {@code CharSequence} to write, null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.0
     */
    public static void write(final CharSequence data, final Writer writer) throws IOException {
        if (data != null) {
            write(data.toString(), writer);
        }
    }


    /**
     * Writes chars from a {@code String} to bytes on an
     * {@code OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     *
     * @param data the {@code String} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #write(String, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void write(final String data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a {@code String} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data the {@code String} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @param charset the charset to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void write(final String data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(data.getBytes(Charsets.toCharset(charset)));
        }
    }

    /**
     * Writes chars from a {@code String} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data the {@code String} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException        if output is null
     * @throws IOException                 if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    public static void write(final String data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    /**
     * Writes chars from a {@code String} to a {@code Writer}.
     *
     * @param data the {@code String} to write, null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static void write(final String data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    /**
     * Writes chars from a {@code StringBuffer} to bytes on an
     * {@code OutputStream} using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     *
     * @param data the {@code StringBuffer} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated replaced by write(CharSequence, OutputStream)
     */
    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output) //NOSONAR
            throws IOException {
        write(data, output, (String) null);
    }

    /**
     * Writes chars from a {@code StringBuffer} to bytes on an
     * {@code OutputStream} using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data the {@code StringBuffer} to write, null ignored
     * @param output the {@code OutputStream} to write to
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException        if output is null
     * @throws IOException                 if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported.
     * @since 1.1
     * @deprecated replaced by write(CharSequence, OutputStream, String)
     */
    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output, final String charsetName) //NOSONAR
            throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charsets.toCharset(charsetName)));
        }
    }

    /**
     * Writes chars from a {@code StringBuffer} to a {@code Writer}.
     *
     * @param data the {@code StringBuffer} to write, null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated replaced by write(CharSequence, Writer)
     */
    @Deprecated
    public static void write(final StringBuffer data, final Writer writer) //NOSONAR
            throws IOException {
        if (data != null) {
            writer.write(data.toString());
        }
    }

    /**
     * Writes bytes from a {@code byte[]} to an {@code OutputStream} using chunked writes.
     * This is intended for writing very large byte arrays which might otherwise cause excessive
     * memory usage if the native code has to allocate a copy.
     *
     * @param data the byte array to write, do not modify during output,
     * null ignored
     * @param output the {@code OutputStream} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.5
     */
    public static void writeChunked(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    /**
     * Writes chars from a {@code char[]} to a {@code Writer} using chunked writes.
     * This is intended for writing very large byte arrays which might otherwise cause excessive
     * memory usage if the native code has to allocate a copy.
     *
     * @param data the char array to write, do not modify during output,
     * null ignored
     * @param writer the {@code Writer} to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.5
     */
    public static void writeChunked(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                writer.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@code OutputStream} line by line, using the default character
     * encoding of the platform and the specified line ending.
     *
     * @param lines the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param output the {@code OutputStream} to write to, not null, not closed
     * @throws NullPointerException if the output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #writeLines(Collection, String, OutputStream, Charset)} instead
     */
    @Deprecated
    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output) throws IOException {
        writeLines(lines, lineEnding, output, Charset.defaultCharset());
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@code OutputStream} line by line, using the specified character
     * encoding and the specified line ending.
     *
     * @param lines the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param output the {@code OutputStream} to write to, not null, not closed
     * @param charset the charset to use, null means platform default
     * @throws NullPointerException if the output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output,
                                  final Charset charset) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        final Charset cs = Charsets.toCharset(charset);
        for (final Object line : lines) {
            if (line != null) {
                output.write(line.toString().getBytes(cs));
            }
            output.write(lineEnding.getBytes(cs));
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * an {@code OutputStream} line by line, using the specified character
     * encoding and the specified line ending.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param lines the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param output the {@code OutputStream} to write to, not null, not closed
     * @param charsetName the name of the requested charset, null means platform default
     * @throws NullPointerException                         if the output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output, final String charsetName) throws IOException {
        writeLines(lines, lineEnding, output, Charset.forName(charsetName));
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * a {@code Writer} line by line, using the specified line ending.
     *
     * @param lines the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param writer the {@code Writer} to write to, not null, not closed
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static void writeLines(final Collection<?> lines, String lineEnding,
                                  final Writer writer) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        for (final Object line : lines) {
            if (line != null) {
                writer.write(line.toString());
            }
            writer.write(lineEnding);
        }
    }

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() { //NOSONAR

    }

}
