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

package sir.barchable.util;

import java.io.IOException;
import java.io.Writer;

/**
 * HexDump from commons IO, patched write to a {@link java.io.Writer}
 * and accept both offset and length to define the target byte range.
 * <p>
 * Origin of code: Commons IO, via POI
 */
public class Hex {

    public Hex() {
        super();
    }

    public static void dump(byte[] data, Writer out) throws IOException {
        dump(data, out, 0, 0, data.length);
    }

    /**
     * Dump an array of bytes to an OutputStream. The output is formatted
     * for human inspection, with a hexadecimal offset followed by the
     * hexadecimal values of the next 16 bytes of data and the printable ASCII
     * characters (if any) that those bytes represent printed per each line
     * of output.
     * <p>
     * The offset argument specifies the start offset of the data array
     * within a larger entity like a file or an incoming stream. For example,
     * if the data array contains the third kibibyte of a file, then the
     * offset argument should be set to 2048. The offset value printed
     * at the beginning of each line indicates where in that larger entity
     * the first byte on that line is located.
     * <p>
     *
     * @param data the byte array to be dumped
     * @param out the Writer to which the data is to be written
     * @param offset offset of the byte array within a larger entity
     * @param start initial index into the byte array
     * @param length the number of bytes to write
     */

    public static void dump(byte[] data, Writer out, long offset, int start, int length)
        throws IOException {

        if (start < 0 || start > data.length) {
            throw new IllegalArgumentException("Illegal index: " + start + " into array of length " + data.length);
        }
        if (length < 0 || start + length > data.length) {
            throw new IllegalArgumentException("Illegal index: " + length + " into array of length " + data.length);
        }
        if (out == null) {
            throw new NullPointerException("null output stream");
        }
        long display_offset = offset + start;
        StringBuilder buffer = new StringBuilder(74);

        for (int j = start; j < start + length; j += 16) {
            int chars_read = start + length - j;

            if (chars_read > 16) {
                chars_read = 16;
            }
            dump(buffer, display_offset).append(' ');
            for (int k = 0; k < 16; k++) {
                if (k < chars_read) {
                    dump(buffer, data[k + j]);
                } else {
                    buffer.append("  ");
                }
                buffer.append(' ');
            }
            for (int k = 0; k < chars_read; k++) {
                if (data[k + j] >= ' ' && data[k + j] < 127) {
                    buffer.append((char) data[k + j]);
                } else {
                    buffer.append('.');
                }
            }
            buffer.append(EOL);
            out.write(buffer.toString());
            out.flush();
            buffer.setLength(0);
            display_offset += chars_read;
        }
    }

    /**
     * The line-separator (initializes to "line.separator" system property.
     */
    public static final String EOL = System.getProperty("line.separator");

    private static final char[] CHARS = {
        '0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F'
    };

    private static final int[] SHIFTS = {
        28, 24, 20, 16, 12, 8, 4, 0
    };

    /**
     * Dump a long value into a StringBuilder.
     *
     * @param _lbuffer the StringBuilder to dump the value in
     * @param value  the long value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder _lbuffer, long value) {
        for (int j = 0; j < 8; j++) {
            _lbuffer.append(CHARS[(int) (value >> SHIFTS[j]) & 15]);
        }
        return _lbuffer;
    }

    /**
     * Dump a byte value into a StringBuilder.
     *
     * @param _cbuffer the StringBuilder to dump the value in
     * @param value  the byte value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder _cbuffer, byte value) {
        for (int j = 0; j < 2; j++) {
            _cbuffer.append(CHARS[value >> SHIFTS[j + 6] & 15]);
        }
        return _cbuffer;
    }
}
