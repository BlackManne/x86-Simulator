package memory;
import transformer.Transformer;
import util.CRC;

/**
 * 磁盘抽象类，磁盘大小为64M
 */

//磁盘的思路：
    //1.CRC计算，calculate（在磁盘中的操作是写的时候使用，整个扇区的数据tobitstream然后计算就可以了）和check（check是把数据流和checkcode并在一起，在磁盘中的操作是读的时候使用，把这一整个扇区的data转换为bitstream，再把目前的CRC取出来tobitstream，然后并在一起
    //并在一起之后去和polynomial做运算即可；
    //2.tobitstream和tobytestream注意java里面的char是占据两个字节的，但是我们转换成的byte只占用一个字节
    //3.磁盘读写：注意！！！！！！Disk.adjust是在每一次循环开始前进行的！！！因为Disk.seek很有可能产生的磁头位置就是有问题的，这一次不能不处理
        //读:找到开始的地址和len，逐字节读出数据，注意：如果到了扇区末尾DISKHEAD.point==511相当于一个扇区读完了，这个时候要读出CRC校验；如果到了最后一次读取，i == len - 1也要进行一次CRC校验
        //写：同理，同样在读到扇区末尾和最后一个写入的时候要更新所在整个扇区的CRC
public class Disk {

    public static int DISK_SIZE_B = 64 * 1024 * 1024;      // 磁盘大小 64 MB

    private static Disk diskInstance = new Disk();

    /**
     * 请勿修改下列属性，至少不要修改一个扇区的大小，如果要修改请保证磁盘的大小为64MB
     */
    public static final int CYLINDER_NUM = 8;
    public static final int PLATTER_PRE_CYLINDER = 8;
    public static final int TRACK_PRE_PLATTER = 16;
    public static final int SECTOR_PRE_TRACK = 128;
    public static final int BYTE_PRE_SECTOR = 512;


    public static final String POLYNOMIAL = "11000000000100001";
    public disk_head DISK_HEAD = new disk_head();

    RealDisk disk = new RealDisk();

    /**
     * 初始化
     */
    public Disk() { }

    public static Disk getDisk() {
        return diskInstance;
    }

    /**
     * 读磁盘
     * @param eip
     * @param len
     * @return
     */
    public char[] read(String eip, int len) {
        //TODO
        Transformer transformer = new Transformer();
        DISK_HEAD.Seek(Integer.parseInt(transformer.binaryToInt(eip)));
        RealDisk disk= getDisk().disk;
        char[] res = new char[len];
        int i = 0;
        while(i < len){
            //注意磁头移动完之后要立即adjust磁头让磁头在正确的位置
            DISK_HEAD.adjust();
            int pointindex = DISK_HEAD.point;
            int sectorindex = DISK_HEAD.sector;
            int trackindex = DISK_HEAD.track;
            int platterindex = DISK_HEAD.platter;
            int cylinderindex = DISK_HEAD.cylinder;
            res[i] = disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.Data[pointindex];
            i ++;
            DISK_HEAD.point ++;  //每读取一个数据就让point下标+1
        }

        return res;
    }

    /**
     * 写磁盘
     * @param eip
     * @param len
     * @param data
     * @return
     */
    public void write(String eip, int len, char[] data) {
        //TODO
        Transformer transformer = new Transformer();
        DISK_HEAD.Seek(Integer.parseInt(transformer.binaryToInt(eip)));
        RealDisk disk = getDisk().disk;
        int i = 0;
        while(i < len){
            DISK_HEAD.adjust();
            int pointindex = DISK_HEAD.point;
            int sectorindex = DISK_HEAD.sector;
            int trackindex = DISK_HEAD.track;
            int platterindex = DISK_HEAD.platter;
            int cylinderindex = DISK_HEAD.cylinder;
            disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.Data[pointindex] = data[i];
            if(DISK_HEAD.point == 511 || i == len - 1){    //如果到了一个扇区的最后或者进行了最后一次
                char[] bitdata = ToBitStream(disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.Data);
                disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.CRC = ToByteStream(CRC.Calculate(bitdata,POLYNOMIAL));
            }
            i ++;
            DISK_HEAD.point ++;  //每读取一个数据就让point下标+1

        }
    }

    /**
     * 写磁盘（地址为Integer型）
     * 测试会调用该方法
     * @param eip
     * @param len
     * @param data
     */
    public void write(int eip, int len, char[] data) {
        //TODO
        DISK_HEAD.Seek(eip);
        RealDisk disk= getDisk().disk;
        int i = 0;
        while(i < len){
            DISK_HEAD.adjust();
            int pointindex = DISK_HEAD.point;
            int sectorindex = DISK_HEAD.sector;
            int trackindex = DISK_HEAD.track;
            int platterindex = DISK_HEAD.platter;
            int cylinderindex = DISK_HEAD.cylinder;
            disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.Data[pointindex] = data[i];
            if(DISK_HEAD.point == 511 || i == len - 1){    //如果到了一个扇区的最后
                char[] bitdata = ToBitStream(disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.Data);
                disk.cylinders[cylinderindex].platters[platterindex].tracks[trackindex].sectors[sectorindex].dataField.CRC = ToByteStream(CRC.Calculate(bitdata,POLYNOMIAL));
            }
            i ++;
            DISK_HEAD.point ++;  //每读取一个数据就让point下标+1
        }
    }

    /**
     * 该方法仅用于测试
     */
    public char[] getCRC() {
        return disk.getCRC(DISK_HEAD);
    }

    /**
     * 磁头
     */
    private class disk_head {
        int cylinder;
        int platter;
        int track;
        int sector;
        int point;

        /**
         * 调整磁头的位置
         */
        public void adjust() {
            if (point == BYTE_PRE_SECTOR) {
                point = 0;
                sector++;
            }
            if (sector == SECTOR_PRE_TRACK) {
                sector = 0;
                track++;
            }
            if (track == TRACK_PRE_PLATTER) {
                track = 0;
                platter++;
            }
            if (platter == PLATTER_PRE_CYLINDER) {
                platter = 0;
                cylinder++;
            }
            if (cylinder == CYLINDER_NUM) {
                cylinder = 0;
            }
        }

        /**
         * 磁头回到起点
         */
        public void Init() {
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            cylinder = 0;
            track = 0;
            sector = 0;
            point = 0;
            platter = 0;
        }

        /**
         * 将磁头移动到目标位置
         * @param start
         */
        public void Seek(int start) {
//            try {
//                Thread.sleep(0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            for (int i = cylinder; i < CYLINDER_NUM; i++) {
                for (int t = platter; t < PLATTER_PRE_CYLINDER; t++) {
                    for (int j = track; j < TRACK_PRE_PLATTER; j++) {
                        for (int z = sector; z < SECTOR_PRE_TRACK; z++) {
                            for (int k = point; k < BYTE_PRE_SECTOR; k++) {
                                if ((i * PLATTER_PRE_CYLINDER * TRACK_PRE_PLATTER * SECTOR_PRE_TRACK * BYTE_PRE_SECTOR + t * TRACK_PRE_PLATTER * SECTOR_PRE_TRACK * BYTE_PRE_SECTOR + j * SECTOR_PRE_TRACK * BYTE_PRE_SECTOR + z * BYTE_PRE_SECTOR + k) == start) {
                                    cylinder = i;
                                    track = j;
                                    sector = z;
                                    point = k;
                                    platter = t;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            Init();
            Seek(start);
        }

        @Override
        public String toString() {
            return "The Head Of Disk Is In\n" +
                    "platter:\t" + cylinder + "\n" +
                    "track:\t\t" + track + "\n" +
                    "sector:\t\t" + sector + "\n" +
                    "point:\t\t" + point;
        }
    }

    /**
     * 600 Bytes/Sector
     */
    private class Sector {
        char[] gap1 = new char[17];
        IDField idField = new IDField();
        char[] gap2 = new char[41];
        DataField dataField = new DataField();
        char[] gap3 = new char[20];
    }

    /**
     * 7 Bytes/IDField
     */
    private class IDField {
        char SynchByte;
        char[] Track = new char[2];
        char Head;
        char sector;
        char[] CRC = new char[2];
    }

    /**
     * 515 Bytes/DataField
     */
    private class DataField {
        char SynchByte;
        char[] Data = new char[512];
        char[] CRC = new char[2];
    }

    /**
     * 128 sectors pre track
     */
    private class Track {
        Sector[] sectors = new Sector[SECTOR_PRE_TRACK];

        Track() {
            for (int i = 0; i < SECTOR_PRE_TRACK; i++)
                sectors[i] = new Sector();
        }
    }


    /**
     * 32 tracks pre platter
     */
    private class Platter {
        Track[] tracks = new Track[TRACK_PRE_PLATTER];

        Platter() {
            for (int i = 0; i < TRACK_PRE_PLATTER; i++)
                tracks[i] = new Track();
        }
    }

    /**
     * 8 platter pre Cylinder
     */
    private class Cylinder {
        Platter[] platters = new Platter[PLATTER_PRE_CYLINDER];

        Cylinder() {
            for (int i = 0; i < PLATTER_PRE_CYLINDER; i++)
                platters[i] = new Platter();
        }
    }


    private class RealDisk {
        Cylinder[] cylinders = new Cylinder[CYLINDER_NUM];

        public RealDisk() {
            for (int i = 0; i < CYLINDER_NUM; i++)
                cylinders[i] = new Cylinder();
        }

        public char[] getCRC(disk_head d) {
            return cylinders[d.cylinder].platters[d.platter].tracks[d.track].sectors[d.sector].dataField.CRC;
        }
    }

    /**
     * 将Byte流转换成Bit流
     * @param data
     * @return
     */
    public static char[] ToBitStream(char[] data) {
        Transformer transformer = new Transformer();
        //java中的char是两个字节，但是实际使用的只有低八位
        //要一位一位取出低八位中的每一位
        int length = data.length;
        char[] t = new char[length * 8];
        for(int i = 0; i < length; i ++){
            char temp = data[i];
            for(int j = 1; j <= 8; j ++){    //对于每一个char要做8次
                t[i * 8 + 8 - j] = (char)((temp & (0b00000001)) + '0');  //按位与
                temp = (char)(temp >> 1);  //右移一位
            }
//            int temp = (int)data[i];
//            String tempstr = transformer.intToBinary(String.valueOf(temp)).substring(24);
//            for(int j = 0; j < 8; j ++){
//                t[8 * i + j] = tempstr.charAt(j);
//            }
       }

        return t;
    }

    /**
     * 将Bit流转换为Byte流
     * @param data
     * @return
     */
    public static char[] ToByteStream(char[] data) {
        int length = data.length / 8;
        char[] t = new char[length];
        for(int i = 0; i < length; i ++){
            int pow = 1;
            int sum = 0;
            for(int j = 7; j >= 0; j --){
                sum += (data[i * 8 + j] - '0') * pow;
                pow *= 2;
            }
            t[i] = (char)sum;
        }
        return t;
    }

    /**
     * 这个方法仅供测试，请勿修改
     * @param eip
     * @param len
     * @return
     */
    public char[] readTest(String eip, int len){
        char[] data = read(eip, len);
        System.out.print(data);
        return data;
    }


    public static void main(String[] args){
        char[] data = {'0', '1', '0', '0', '0', '0', '0', '1'};
        char[] res = ToByteStream(data);
        System.out.println(res);
        char [] data2 = {'A'};
        char[] res2 = ToBitStream(data2);
        for(int i = 0; i < 8; i ++){
            System.out.print(res2[i]);
        }
    }
}
