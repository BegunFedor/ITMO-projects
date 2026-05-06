

import org.example.lab.beans.PointBean;
import org.example.lab.tools.PointService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {

    private PointService service;

    @Before
    public void setUp() {
        service = new PointService();

        PointBean bean = new PointBean();
        bean.setPointType("SPIDER");
        bean.setLogsQuantity(4);

        try {
            var field = PointService.class.getDeclaredField("pointBean");
            field.setAccessible(true);
            field.set(service, bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTriangleHit() {
        assertTrue(invokeCheckHit(1, 1, 4));
        assertTrue(invokeCheckHit(2, 0, 4));
        assertFalse(invokeCheckHit(3, 3, 4));
    }

    @Test
    public void testRectangleHit() {
        assertTrue(invokeCheckHit(-1, -1, 2));
        assertTrue(invokeCheckHit(0, -2, 2));
        assertFalse(invokeCheckHit(-2, -3, 2));
    }

    @Test
    public void testCircleHit() {
        assertTrue(invokeCheckHit(1, -1, 2));
        assertTrue(invokeCheckHit(0, -2, 2));
        assertFalse(invokeCheckHit(2, -2, 2));
    }

    @Test
    public void testMiss() {
        assertFalse(invokeCheckHit(-3, 3, 2));
        assertFalse(invokeCheckHit(3, 3, 2));
    }

    private boolean invokeCheckHit(double x, double y, double r) {
        try {
            var method = PointService.class.getDeclaredMethod("checkHit", double.class, double.class, double.class);
            method.setAccessible(true);
            return (boolean) method.invoke(service, x, y, r);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}