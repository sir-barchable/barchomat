package sir.barchable.clash.protocol;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sir Barchable
 *         Date: 21/04/15
 */
public class TypeParserTest {
    private TypeFactory typeParser = new TypeFactory();

    @Test
    public void testParseOptionalLong() {
        TypeFactory.Type type = typeParser.parse("?LONG");
        Assert.assertEquals("LONG", type.getName());
        Assert.assertTrue(type.isOptional());
        Assert.assertFalse(type.isArray());
        Assert.assertEquals(0, type.getLength());
    }
}
