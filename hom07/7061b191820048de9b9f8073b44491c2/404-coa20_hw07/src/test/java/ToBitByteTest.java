import memory.Disk;
import org.junit.Test;


public class ToBitByteTest {
    String polynomial = "1001";
    char[] data = {'1','0','0','0','1','1'};

    @Test
    public void test1(){
        char[] data = new char[24];
        for(int i = 0; i < data.length; i ++){
            if(i != 0 && i % 8 != 0 && (i % 2 == 0 || i % 7 == 0))
                data[i]= '1';
            else data[i] = '0';
        }
        System.out.println(new String(data));
        char[] res = Disk.ToByteStream(data);
        System.out.println(res);
    }

    @Test
    public void test2(){
        char[] data = {'+','*','.'};
        char[] res = Disk.ToBitStream(data);
        String op = new String(res);
        System.out.println(op);
    }
}
