package memory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import util.CRC;

public class MemoryTest {
    Disk disk=Disk.getDisk();
    @Test
    public void test0(){
        Disk disk=Disk.getDisk();
        String eip="00000000000000000000000000000001";
        char[] data=new char[511];    //？？？？？？？？？？？？511？？？？？？？？？你是程序员吗？？？（/发怒/发怒）？？？？？不写2的次方？？？？？？气死我了！！！！
        for(int i=0;i<data.length;++i){
            data[i]=(char)(i%128);
        }
        disk.write(eip, data.length, data);
        char[] crc="k".toCharArray();
        char[] res=disk.getCRC();
        for(int i=0;i<res.length;++i){
            Assert.assertEquals(crc[i], res[i]);
        }
        Assert.assertEquals(String.valueOf(data), String.valueOf(disk.read(eip, data.length)));
        return;
    }
}
