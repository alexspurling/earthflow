package earth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Matrix4Test {

    @Test
    public void testMultiply() {
        Matrix4 b = new Matrix4(new double[][] {
                {1, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 3, 0},
                {0, 0, 0, 4},
        });
        var mul = Matrix4.identity().multiply(b);
        assertEquals(b, mul);
        System.out.println(mul);
    }

    @Test
    public void testMultiply2() {
        Matrix4 a = new Matrix4(new double[][] {
                {5, 7, 9, 10},
                {2, 3, 3, 8},
                {8, 10, 2, 3},
                {3, 3, 4, 8},
        });
        Matrix4 b = new Matrix4(new double[][] {
                {3, 10, 12, 18},
                {12, 1, 4, 9},
                {9, 10, 12, 2},
                {3, 12, 4, 10},
        });
        Matrix4 expected = new Matrix4(new double[][] {
                {210, 267, 236, 271},
                {93, 149, 104, 149},
                {171, 146, 172, 268},
                {105, 169, 128, 169},
        });
        Matrix4 mul = a.multiply(b);
        System.out.println(mul);
        assertEquals(expected, mul);
    }

    @Test
    public void testMultiply3() {
        Matrix4 a = new Matrix4(new double[][] {
                {5, 7, 9, 10},
                {2, 3, 3, 8},
                {8, 10, 2, 3},
                {3, 3, 4, 8},
        });
        Matrix4 b = new Matrix4(new double[][] {
                {3, 10, 12, 18},
                {12, 1, 4, 9},
                {9, 10, 12, 2},
                {3, 12, 4, 10},
        });
        Matrix4 expected = new Matrix4(new double[][] {
                {185.0, 225.0, 153.0, 290.0},
                {121.0, 154.0, 155.0, 212.0},
                {167.0, 219.0, 143.0, 222.0},
                {101.0, 127.0, 111.0, 218.0},
        });
        Matrix4 mul = b.multiply(a);
        System.out.println(mul);
        assertEquals(expected, mul);
    }
}
