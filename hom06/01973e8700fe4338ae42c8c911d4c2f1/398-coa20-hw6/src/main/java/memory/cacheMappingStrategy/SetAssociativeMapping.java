package memory.cacheMappingStrategy;

import memory.Cache;
import memory.Memory;
import memory.cacheReplacementStrategy.FIFOReplacement;
import transformer.Transformer;
import util.Tools;

import javax.tools.Tool;

public class SetAssociativeMapping extends MappingStrategy{


    Transformer t = new Transformer();
    private int SETS = 512; // 共256个组
    private int setSize = 2;   // 每个组4行


    /**
     * 该方法会被用于测试，请勿修改
     * @param SETS
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
    }

    /**
     * 该方法会被用于测试，请勿修改
     * @param setSize
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    public int getSETS(){
        return this.SETS;
    }

    public int getSetSize(){
        return this.setSize;
    }
    /**
     *
     * @param blockNO 内存数据块的块号
     * @return cache数据块号 22-bits  [前14位有效]
     */

    @Override
    public char[] getTag(int blockNO) {
        //TODO
        Tools tools = new Tools();
        Transformer transformer = new Transformer();
        int setbits = tools.log(getSetSize(), 2);  //获得组号所占的位数
        int tag = blockNO;
        for(int i = 1; i <= setbits; i ++){
           tag = tag / 2;
        }
        String strtag = transformer.intToBinary(String.valueOf(tag));
        strtag = strtag.substring(10, 32);  //选择低22位作为tag
        return strtag.toCharArray();
    }

    /**
     *
     * @param blockNO 目标数据内存地址前22位int表示
     * @return -1 表示未命中
     */
    @Override
    public int map(int blockNO) {
        //TODO
        FIFOReplacement replacement = (FIFOReplacement)this.replacementStrategy;
        Memory memory = Memory.getMemory();
        Transformer transformer = new Transformer();
        int setsize = getSetSize();
        int setbelonged = blockNO % getSETS();  //看看映射到哪一组
        int start = setbelonged * setsize;
        int end = setbelonged * setsize+ setsize - 1;
        char[] addrtag = getTagFromBlockNum(blockNO);
        int linenum = replacement.isHit(start,end,addrtag);
        if(linenum == -1){
            String sAddr = transformer.intToBinary(String.valueOf(blockNO));
            sAddr = sAddr.substring(10, 32) + "0000000000";
            //String sAddr = getPAddr(blockNO);
            //String sAddr=  new Transformer().intToBinary(String.valueOf(blockNO * 1024));
            char[] data = memory.read(sAddr, 1024);  //读一个块
            linenum = replacement.Replace(start, end, addrtag, data);
        }
        return linenum;
    }

    @Override
    public int writeCache(int blockNO) {
        //TODO

        return -1;
    }

    @Override
    public String getPAddr(int rowNo) {
        //TODO
        Cache cache = Cache.getCache();
        Transformer transformer = new Transformer();
        Tools tools = new Tools();
        char[] tag = cache.getChlineTag(cache.getCacheLine(rowNo));
        int setbits = tools.log(getSETS(), 2);
        int setnum = rowNo / getSetSize();
        //String a = transformer.intToBinary(String.valueOf(setnum));
        String setStr = transformer.intToBinary(String.valueOf(setnum)).substring(32 - setbits,32);
        String res = new String(tag).substring(0,12+tools.log(getSetSize(), 2)) +setStr + "0000000000";
        //String setStr2 = new String(tag).substring(0, 22 - setbits) +setStr + "0000000000";
        //System.out.println(setStr2);
        //System.out.println(res);
        return res;
        //我去40了亲爱的 时间来不及了 你刚刚tag没有取有效位所以地址算出来很大 这里给加了个substring就好啦 爱你哦 醒了给我发消息
    }

    public char[] getTagFromBlockNum(int blocknum){
        Transformer transformer= new Transformer();
        Tools tools = new Tools();
        String strBlockNum = transformer.intToBinary(String.valueOf(blocknum)).substring(10, 32); //获取22位的
        int setbits = tools.log(getSETS(), 2);
        strBlockNum = strBlockNum.substring(0, strBlockNum.length() - setbits);
        for(int i = 1; i <= setbits; i ++){
            strBlockNum = strBlockNum + "0";   //在tag最后补零
        }
        return strBlockNum.toCharArray();


    }

}










