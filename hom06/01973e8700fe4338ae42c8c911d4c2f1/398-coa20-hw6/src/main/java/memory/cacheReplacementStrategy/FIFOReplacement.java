package memory.cacheReplacementStrategy;

import memory.Cache;
import memory.cacheWriteStrategy.WriteBackStrategy;
import memory.cacheWriteStrategy.WriteStrategy;
import transformer.Transformer;

import java.util.Arrays;

/**
 * 先进先出算法
 */
public class FIFOReplacement extends ReplacementStrategy {

    public long timenow=0;
    public long newtime(){
        return ++timenow;
    }

    @Override
    public int isHit(int start, int end, char[] addrTag) {
        //TODO
        Cache cache = Cache.getCache();
        Transformer transformer = new Transformer();
        String strtag = new String(addrTag);
        //两个tag是二进制的，比较两个tag是否相等可以选择binaryToInt都变成整型再判断是否相等，也可以用.equals判断两个二进制字符串是否相等
        int tag = Integer.parseInt(transformer.binaryToInt(strtag));
        for(int i = start; i <= end; i ++){
            boolean validbit = cache.getChlineValid(cache.getCacheLine(i));
            //如果有效位是true，说明在这一位上是有数据的
            if(validbit) {
                String cachestrtag = new String(cache.getChlineTag(cache.getCacheLine(i)));
                int cachetag = Integer.parseInt(transformer.binaryToInt(cachestrtag));
                if (cachetag == tag)
                    //如果找到了就返回所在的行
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int Replace(int start, int end, char[] addrTag, char[] input) {
        //TODO
        Cache cache = Cache.getCache();
        //再替换之前先获得所有到目前为止最小的时间
        //long time = cache.getMinTime();
        for(int i = start; i <= end; i ++){
           // boolean validbit = cache.getChlineValid(cache.getCacheLine(i));
            if(cache.getCacheLine(i) == null || !cache.getChlineValid(cache.getCacheLine(i))){
                cache.setChlineData(cache.getCacheLine(i), input);
                cache.setChlineTag(cache.getCacheLine(i), addrTag);
                cache.setChlineValid(cache.getCacheLine(i),true);  //找到了某一个空行并且把数据存了进去
                //这里比较巧妙：在没有进行任何“替换”的时候，cache里面的所有数据的相对时间都是一样的，因此可以不用更新时间，
                //当有一个新的数据进行了“替换”。产生了不同的时间戳，更新一下时间就可以了
                //所以只有在数据“替换”的时候才调用changetime方法
                cache.changeTime(cache.getCacheLine(i), newtime());
                return i;
            }
        }
        //如果到了这里说明cache已经满了
        int lineindex = cache.getMinTimeLine(start,end);
        //在被替换的时候进行一次写回
        //只有dirty位是true的时候才要写回。在写回的时候必须要判断一下dirty位是否是true，否则不进行写回，直接写入数据
        if(cache.getCacheLine(lineindex) != null && cache.getChlineDirty(cache.getCacheLine(lineindex))){
            writeStrategy.writeBack(lineindex);
            cache.setChlineDirty(cache.getCacheLine(lineindex), false);
        }
        //替换之后的行时间比之前还要+1，设置新的时间戳
        //也就是说只要有行替换（直接进去还是替换进去），都要修改一下时间
        cache.changeTime(cache.getCacheLine(lineindex), newtime());
        cache.setChlineData(cache.getCacheLine(lineindex), input);
        cache.setChlineTag(cache.getCacheLine(lineindex),addrTag);
        cache.setChlineValid(cache.getCacheLine(lineindex),true);
        return lineindex;
    }

}
