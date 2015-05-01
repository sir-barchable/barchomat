package sir.barchable.clash.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Date: 6/04/15
 *
 * @author Sir Barchable
 */
public class Protocol {
    private List<MessageDefinition> messages = new ArrayList<>();

    public Protocol(@JsonProperty("messages") List<MessageDefinition> messages) {
        this.messages = messages;
    }

    public List<MessageDefinition> getMessages() {
        return messages;
    }

    public static class MessageDefinition {
        private Integer id;
        private String name;
        private String comment;
        private List<FieldDefinition> fields;
        private final List<Extension> extensions;

        public MessageDefinition(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("comment") String comment,
            @JsonProperty("fields") List<FieldDefinition> fields,
            @JsonProperty("extensions") List<Extension> extensions
        ) {
            this.id = id;
            this.name = name;
            this.fields = fields;
            this.comment = comment;
            this.extensions = extensions;
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
            if (name == null) {
                throw new NullPointerException("null field name");
            }
            for (FieldDefinition field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            return null;
        }

        public Extension getExtension(int id) {
            if (extensions != null) {
                for (Extension extension : extensions) {
                    if (extension.getId() == id) {
                        return extension;
                    }
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

            public FieldDefinition(
                @JsonProperty("name") String name,
                @JsonProperty("type") String type,
                @JsonProperty("comment") String comment
            ) {
                this.name = name;
                this.type = type;
                this.comment = comment;
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
                @JsonProperty("id") Integer id,
                @JsonProperty("comment") String comment,
                @JsonProperty("fields") List<FieldDefinition> fields
            ) {
                this.id = id;
                this.comment = comment;
                this.fields = fields;
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
