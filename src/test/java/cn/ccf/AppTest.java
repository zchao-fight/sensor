package cn.ccf;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void testSoundDir() {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(System.getProperty("file.separator"));
    }
}
