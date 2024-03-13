package memory.cacheWriteStrategy;

import memory.Cache;
import memory.Memory;
import memory.cacheMappingStrategy.MappingStrategy;

/**
 * @Author: A cute TA
 * @CreateTime: 2020-11-12 11:38
 */
public abstract class WriteStrategy {
    MappingStrategy mappingStrategy;
    /**
     * 将数据写入Cache，并且根据策略选择是否修改内存
     * @param rowNo 行号
     * @param input  数据
     * @return
     */
    public String write(int rowNo, char[] input) {
        //TODO
        Cache cache = Cache.getCache();
        cache.setChlineDirty(cache.getCacheLine(rowNo), true);  //修改过了，脏位置为true
        cache.setChlineData(cache.getCacheLine(rowNo), input);
        return null;
    }


    /**
     * 修改内存
     * @return
     */
    public void writeBack(int rowNo) {
        //TODO
        Memory memory = Memory.getMemory();
        Cache cache = Cache.getCache();
        char[] data = cache.getChlineData(cache.getCacheLine(rowNo));
        String sAddr = mappingStrategy.getPAddr(rowNo);
        memory.write(sAddr,data.length, data);
        //memory.write(sAddr,data.length, data);
        //cache.setChlineDirty(cache.getCacheLine(rowNo), false);   //把脏位修改回去


    }

    public void setMappingStrategy(MappingStrategy mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }

    public abstract Boolean isWriteBack();
}
