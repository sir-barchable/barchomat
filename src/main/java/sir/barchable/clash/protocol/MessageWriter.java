package sir.barchable.clash.protocol;

import sir.barchable.clash.protocol.Protocol.StructDefinition.FieldDefinition;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class MessageWriter {
    private TypeFactory typeFactory;

    public MessageWriter(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public void write(TypeFactory.Type type, Object o, MessageOutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("null payload");
        }

        if (type.isOptional()) {
            out.writeBit(o != null);
            if (o == null) {
                return;
            }
        }

        if (type.isArray()) {
            writeArray(type, o, out);
        } else if (type.isPrimitive()) {
            writePrimitive(type, o, out);
        } else {
            writeStruct(type, o, out);
        }
    }

    private void writeArray(TypeFactory.Type type, Object o, MessageOutputStream out) throws IOException {
        int length = o == null ? 0 : Array.getLength(o);
        if (type.getLength() == 0) {
            out.writeInt(length);
        } else if (length != type.getLength()) {
            throw new PduException("Array length mismatch for " + type.getName() + " (" + type.getLength() + "!=" + length + ")");
        }
        if (o != null) {
            if (type.isPrimitive()) {
                switch (type.getPrimitiveType()) {

                    case BYTE:
                        out.write((byte[]) o);
                        break;

                    case INT:
                        int[] ints = (int[]) o;
                        for (int v : ints) {
                            out.writeInt(v);
                        }
                        break;

                    case LONG:
                        long[] longs = (long[]) o;
                        for (long v : longs) {
                            out.writeLong(v);
                        }
                        break;

                    case STRING:
                        String[] strings = (String[]) o;
                        for (String v : strings) {
                            out.writeString(v);
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("Don't know how to write arrays of type " + type.getPrimitiveType());
                }
            } else {
                TypeFactory.Type structName = typeFactory.newType(type.getStructDefinition().getName());
                for (Object struct : (Object[]) o) {
                    write(structName, struct, out);
                }
            }
        }
    }

    private void writePrimitive(TypeFactory.Type type, Object o, MessageOutputStream out) throws IOException {
        switch (type.getPrimitiveType()) {
            case BOOLEAN:
                out.writeBit(o == null ? false : (Boolean) o);
                break;

            case BYTE:
                out.write(o == null ? 0 : (Byte) o);
                break;

            case INT:
                out.writeInt(o == null ? 0 : (Integer) o);
                break;

            case LONG:
                out.writeLong(o == null ? 0 : (Long) o);
                break;

            case STRING:
                out.writeString((String) o);
                break;

            case ZIP_STRING:
                out.writeZipString(o == null ? "" : (String) o);
                break;

            default:
                throw new IllegalArgumentException("Don't know how to write type " + type.getPrimitiveType());
        }
    }

    private void writeStruct(TypeFactory.Type type, Object o, MessageOutputStream out) throws IOException {
        Map<String, Object> struct = (Map<String, Object>) o;
        int fieldIndex = 0;
        for (FieldDefinition fieldDefinition : type.getStructDefinition().getFields()) {
            fieldIndex++;
            String key = fieldDefinition.getName();
            if (key == null) {
                key = "field" + fieldIndex;
            }
            Object value = struct.get(key);
            TypeFactory.Type fieldType = typeFactory.newType(fieldDefinition.getType());
            if (value == null && fieldDefinition.getDefault() != null) {
                value = fieldType.valueOf(fieldDefinition.getDefault());
            }
            try {
                write(fieldType, value, out);
            } catch (RuntimeException e) {
                throw new IOException("Failed to write field " + key + " of " + type.getName(), e);
            }
        }
        Object id = struct.get(TypeFactory.ID_FIELD);
        if (id != null && id instanceof Integer) {
            Protocol.StructDefinition.Extension extension = type.getStructDefinition().getExtension((Integer) id);
            if (extension != null) {
                for (FieldDefinition fieldDefinition : extension.getFields()) {
                    fieldIndex++;
                    String key = fieldDefinition.getName();
                    if (key == null) {
                        key = "field" + fieldIndex;
                    }
                    TypeFactory.Type fieldType = typeFactory.newType(fieldDefinition.getType());
                    Object value = struct.get(key);
                    if (value == null && fieldDefinition.getDefault() != null) {
                        value = fieldType.valueOf(fieldDefinition.getDefault());
                    }
                    try {
                        write(fieldType, value, out);
                    } catch (IOException e) {
                        throw new IOException("Failed to write field " + key + " of " + type.getName(), e);
                    }
                }
            }
        }
    }
}
