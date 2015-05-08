package sir.barchable.clash.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model for protocol definition in JSON.
 *
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class Protocol {
    private List<StructDefinition> messages;

    public Protocol(@JsonProperty("messages") List<StructDefinition> messages) {
        this.messages = messages;
    }

    public List<StructDefinition> getMessages() {
        return messages;
    }

    public static class StructDefinition {
        private Integer id;
        private String name;
        private String comment;
        private List<FieldDefinition> fields;
        private final List<Extension> extensions;

        public StructDefinition(
            @JsonProperty(value = "id") Integer id,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty("comment") String comment,
            @JsonProperty("fields") List<FieldDefinition> fields,
            @JsonProperty("extensions") List<Extension> extensions
        ) {
            if (name == null) {
                throw new NullPointerException("null type name");
            }
            this.id = id;
            this.name = name;
            this.fields = fields == null ? Collections.emptyList() : fields;
            this.comment = comment;
            this.extensions = extensions == null ? Collections.emptyList() : extensions;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public List<FieldDefinition> getFields() {
            return fields;
        }

        public List<Extension> getExtensions() {
            return extensions;
        }

        public FieldDefinition getField(String name) {
            for (FieldDefinition field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            return null;
        }

        public Extension getExtension(int id) {
            for (Extension extension : extensions) {
                if (extension.getId() == id) {
                    return extension;
                }
            }
            return null;
        }

        public static class FieldDefinition {

            /**
             * Field name.
             */
            private String name;

            /**
             * Field type.
             */
            private String type;

            /**
             * Field comment.
             */
            private String comment;

            /**
             * Default value if not set.
             */
            private String dflt;

            public FieldDefinition(
                @JsonProperty(value = "name") String name,
                @JsonProperty(value = "type", required = true) String type,
                @JsonProperty("comment") String comment,
                @JsonProperty("default") String dflt
            ) {
                Objects.requireNonNull(type, "null field type");
                this.name = name;
                this.type = type;
                this.comment = comment;
                this.dflt = dflt;
            }

            public String getName() {
                return name;
            }

            public String getType() {
                return type;
            }

            public String getComment() {
                return comment;
            }

            public String getDefault() {
                return dflt;
            }

            @Override
            public String toString() {
                return "FieldDefinition[" + "name='" + name + '\'' + ", type=" + type + ']';
            }
        }

        public static class Extension {
            private Integer id;
            private String comment;
            private List<FieldDefinition> fields;

            public Extension(
                @JsonProperty(value = "id", required = true) Integer id,
                @JsonProperty("comment") String comment,
                @JsonProperty("fields") List<FieldDefinition> fields
            ) {
                Objects.requireNonNull("null extension id");
                this.id = id;
                this.comment = comment;
                this.fields = fields == null ? Collections.emptyList() : fields;
            }

            public Integer getId() {
                return id;
            }

            public String getComment() {
                return comment;
            }

            public List<FieldDefinition> getFields() {
                return fields;
            }
        }
    }
}
