package sir.barchable.clash.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
        private String superType;
        private String comment;
        private List<FieldDefinition> fields;

        public MessageDefinition(
            @JsonProperty("id") Integer id,
            @JsonProperty("extends") String superType,
            @JsonProperty("name") String name,
            @JsonProperty("comment") String comment,
            @JsonProperty("fields") List<FieldDefinition> fields
        ) {
            this.id = id;
            this.name = name;
            this.superType = superType;
            this.fields = fields;
            this.comment = comment;
        }

        public Integer getId() {
            return id;
        }

        public String getExtends() {
            return superType;
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
    }
}
