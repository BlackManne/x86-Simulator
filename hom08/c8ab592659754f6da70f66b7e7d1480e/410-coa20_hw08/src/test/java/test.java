import cpu.MMU;
import memory.Memory;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class test {
    //test helper=new test();
    Memory memory=new Memory();
    MMU mmu=new MMU();
    // 段⻚式
    @Test
    public void test1() {
        Memory.SEGMENT = true;
        Memory.PAGE = true;
        //开启段页式
        String eip = "00000000000000000000000000000000";
        int len = 2 * 1024;
        char[] data = fillData((char) 0b00001111, len);
        memory.alloc_seg_force(0, eip, len / 2, true, "");
        assertArrayEquals(data,mmu.read("000000000000000000000000000000000000000000000000", len));
    }
    // 实模式
    @Test
    public void test2() {
        Memory.SEGMENT = false;
        Memory.PAGE = false;
        int len = 128;
        char[] data = fillData((char)0b00001111, 128);
        assertArrayEquals(data,
                mmu.read("000000000000000000000000000000000000000000000000", len));
    }
    // 段式
    @Test
    public void test3() {
        Memory.SEGMENT = true;
        Memory.PAGE = false;
        String eip = "00000000000000000000000000000000";
        int len = 1024 * 1024;
        char[] data = fillData((char)0b00001111, len);
        memory.alloc_seg_force(0, eip, len, false, eip);
        assertArrayEquals(data,
                mmu.read("000000000000000000000000000000000000000000000000", len));
    }
    public char[] fillData(char dataUnit, int len) {
        char[] data = new char[len];
        Arrays.fill(data, dataUnit);
        return data;
    }
}