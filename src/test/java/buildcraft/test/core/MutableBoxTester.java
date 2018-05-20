package buildcraft.test.core;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.misc.data.MutableBox;

import buildcraft.test.TestHelper;

@RunWith(Theories.class)
public class MutableBoxTester {
    private static final BlockPos MIN = new BlockPos(1, 2, 3), MAX = new BlockPos(4, 5, 6);
    private static final BlockPos SIZE = new BlockPos(4, 4, 4);
    private static final BlockPos CENTER = new BlockPos(3, 4, 5);
    private static final Vec3d CENTER_EXACT = new Vec3d(3, 4, 5);

    @DataPoints("testContainsVec3d")
    public static Set<Entry<Vec3d, Boolean>> dataContainsVec3d() {
        return ImmutableMap.<Vec3d, Boolean> builder()
            // @formatter:off
                .put(new Vec3d(0, 0, 0), false).put(new Vec3d(1, 2, 3), true).put(new Vec3d(1.3, 2.4, 3.5), true).put(new Vec3d(4.9, 5.9, 6.9), true).put(
                        new Vec3d(5, 5, 6), false)
                // @formatter:on
            .build().entrySet();
    }

    @Test
    @Theory
    public void testContainsVec3d(@FromDataPoints("testContainsVec3d") Entry<Vec3d, Boolean> entry) {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        Vec3d in = entry.getKey();
        boolean expected = entry.getValue();
        Assert.assertEquals(expected, mutableBox.contains(in));
    }

    @Test
    public void testMin() {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        Assert.assertEquals(MIN, mutableBox.min());
    }

    @Test
    public void testMax() {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        Assert.assertEquals(MAX, mutableBox.max());
    }

    @Test
    public void testSize() {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        Assert.assertEquals(SIZE, mutableBox.size());
    }

    @Test
    public void testCenter() {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        Assert.assertEquals(CENTER, mutableBox.center());
    }

    @Test
    public void testCenterExact() {
        MutableBox mutableBox = new MutableBox(MIN, MAX);
        TestHelper.assertVec3dEquals(CENTER_EXACT, mutableBox.centerExact());
    }

    @Test
    public void testIntersection1() {
        MutableBox mutableBox1 = new MutableBox(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        MutableBox mutableBox2 = new MutableBox(new BlockPos(1, 1, 1), new BlockPos(3, 3, 3));
        MutableBox inter = new MutableBox(new BlockPos(1, 1, 1), new BlockPos(2, 2, 2));
        Assert.assertEquals(inter, mutableBox1.intersect(mutableBox2));
        Assert.assertEquals(inter, mutableBox2.intersect(mutableBox1));
    }

    @Test
    public void testIntersection2() {
        MutableBox mutableBox1 = new MutableBox(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        MutableBox mutableBox2 = new MutableBox(new BlockPos(0, 0, 0), new BlockPos(3, 3, 3));
        MutableBox inter = new MutableBox(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        Assert.assertEquals(inter, mutableBox1.intersect(mutableBox2));
        Assert.assertEquals(inter, mutableBox2.intersect(mutableBox1));
    }

    @Test
    public void testIntersection3() {
        MutableBox mutableBox1 = new MutableBox(new BlockPos(1, 1, 1), new BlockPos(2, 2, 2));
        MutableBox mutableBox2 = new MutableBox(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        MutableBox inter = new MutableBox(new BlockPos(1, 1, 1), new BlockPos(1, 1, 1));
        Assert.assertEquals(inter, mutableBox1.intersect(mutableBox2));
        Assert.assertEquals(inter, mutableBox2.intersect(mutableBox1));
    }
}
