package memory;

import memory.cacheMappingStrategy.MappingStrategy;
import memory.cacheMappingStrategy.SetAssociativeMapping;
import memory.cacheReplacementStrategy.FIFOReplacement;
import memory.cacheReplacementStrategy.ReplacementStrategy;
import memory.cacheWriteStrategy.WriteBackStrategy;
import memory.cacheWriteStrategy.WriteStrategy;
import transformer.Transformer;
import util.Tools;
import java.util.Arrays;


//cache思路整理：
//cache的组成：
//首先cache每一行的组成，低x位用来标识块内地址，32-x位是blockno，其中低s位表示组号，高32-x-s位表示tag
//比如在这个里面按字节寻址，每一个block是1KB，也就是1024个字节，块内地址低10位，blockno22位，假设有1024个组，那么组号10位，tag12位
//cache的操作：
//这个fetch其实就是从memory.read,会先查cache，如果cache里面没有就从memory里面读取，然后写入cache
//首先是fetch数据，fetch操作就是给一个内存基址和数据长度，判断这一段数据（一定在一个块内）是否在catch中，如果在cache中就返回行号，不在就从内存中找到写进去
//其实就是这个基址所在block是否在cache中，如何判断？
//组关联映射：首先找到block的组，用它去对组的个数取余得到组号，然后blockno的高x位获得tag，在这个组内搜索（用组号找到起始行和终止行），如果有一个的tag和tag匹配就返回
//如果不匹配就要从内存里面读取对应数据：注意，必须是一整个block（memory.read)，然后将这个数据映射到cache里，还是找到对应的组，如果这个组里面有空行就直接映射进去，注意更新时间戳
//如果这个组里面没有空行使用替换算法，比如FIFO的话找到最先进来的行，注意：在替换时需要判断是否写回（如果使用write back策略的话，如果是write through就是在cache.write的时候将指定行的改动写入主存）
//write back就是在指定行判断一下dirty位是否为true，如果为true的话就要写回主存：
//关键：从cache行号对应到主存地址：首先可以找到tag（注意cacheline存储的tag是22位但实际上不一定是22位，是22位减掉set位）其次可以根据行号找到在哪个set里，根据set的个数计算出set位所占位数然后找到这行的set位，
//最后再末尾加上块内地址位数的0得到32位内存起始地址，然后写入一整个块就可以了



/**
 * 高速缓存抽象类
 * TODO: 缓存机制实现
 */
public class Cache {	//

	public static final boolean isAvailable = true;			// 默认启用Cache

	public static final int CACHE_SIZE_B = 1 * 1024 * 1024;      // 1 MB 总大小

	public static final int LINE_SIZE_B = 1 * 1024; // 1 KB

	private CacheLinePool cache = new CacheLinePool(CACHE_SIZE_B/LINE_SIZE_B); 	// 总大小1MB / 行大小1KB = 1024个行

	private static Cache cacheInstance = new Cache();

	private Cache() {
		this.mappingStrategy = new SetAssociativeMapping();
		//mappingStrategy.setReplacementStrategy(new FIFOReplacement());
		mappingStrategy.mysetReplacementStrategy(new FIFOReplacement(), new WriteBackStrategy());
		this.writeStrategy = new WriteBackStrategy();
		this.writeStrategy.setMappingStrategy(new SetAssociativeMapping());
	}

	public static Cache getCache() {
		return cacheInstance;
	}

	private MappingStrategy mappingStrategy;
	private WriteStrategy writeStrategy;

	/**
	 * 查询{@link Cache#cache}表以确认包含[sAddr, sAddr + len)的数据块是否在cache内
	 * 	 * 如果目标数据块不在Cache内，则将其从内存加载到Cache(注意，写入的是一整个内存块）
	 * @param sAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
	 * @param len 待读数据的字节数，[sAddr, sAddr + len)包含的数据必须在同一个数据块内
	 * 	 * @return 数据块在Cache中的对应行号
	 */
	public int fetch(String sAddr, int len) {
		//TODO
		Memory memory = Memory.getMemory();
		Transformer transformer = new Transformer();
		Cache cache = Cache.getCache();
		SetAssociativeMapping mapping = (SetAssociativeMapping)(cache.mappingStrategy);
		FIFOReplacement replacement = new FIFOReplacement();
		//int setnum = mapping.getSETS();   //有多少个组
		//int setsize = mapping.getSetSize();   //每个组有多少行
		//int setBits = tools.log(setsize, 2);
		//String addrInBlock = sAddr.substring(22);  //最后十位是块内地址
		String blocknum = sAddr.substring(0, 22);
		//char[] addrtag = mapping.getTag(Integer.parseInt(blocknum));
		int blockNo = Integer.parseInt(transformer.binaryToInt(blocknum));
		return mapping.map(blockNo);
		/*String addrSet = sAddr.substring(22 - setBits, 22);  //中间是组号
		int setbelonged = tools.log(Integer.parseInt(transformer.binaryToInt(blocknum)), setnum);
		int start = setbelonged * setsize;
		int end = setbelonged * setsize+ setsize - 1;
		int linenum = replacement.isHit(start,end,addrtag);
		if(linenum == -1){
			char[] data = memory.read(sAddr, len);
			linenum = replacement.Replace(start, end, addrtag, data);
		}
		return linenum;

		 */
	}

	/**
	 * 读取[eip, eip + len)范围内的连续数据，可能包含多个数据块的内容
	 * @param eip 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
	 * @param len 待读数据的字节数
	 * @return
	 */
	public char[] read(String eip, int len){
		return helper(eip, len, null);
	}

	/**
	 * 将连续的数据写入[eip, eip + len]，可能包含多个数据块的内容
	 * @param eip 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
	 * @param len 待读数据的字节数
	 * @param data 连续数据
	 */
	public void write(String eip, int len, char[] data){
		helper(eip, len, data);
	}

	private char[] helper(String eip, int len, char[] writeData){
		char[] data = new char[len];
		Transformer t = new Transformer();
		int addr =  Integer.parseInt(t.binaryToInt("0" + eip));
		int upperBound = addr + len;
		int index = 0;
		while (addr < upperBound) {
			int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
			if (addr + nextSegLen >= upperBound) {
				nextSegLen = upperBound - addr;
			}
			int i=0;
			if(writeData == null){
				int rowNO = fetch(t.intToBinary(String.valueOf(addr)), nextSegLen);
				char[] cache_data = cache.get(rowNO).getData();
				while (i < nextSegLen) {
					data[index] = cache_data[addr % LINE_SIZE_B + i];
					index++;
					i++;
				}
			}
			else{
				int rowNO = fetch(t.intToBinary(String.valueOf(addr)), nextSegLen);
				char[] input = cache.get(rowNO).getData();
				while (i < nextSegLen) {
					input[addr % LINE_SIZE_B + i] = writeData[index];
					index++;
					i++;
				}
				writeStrategy.write(rowNO, input);
			}
			addr += nextSegLen;
		}
		return data;
	}


	public void setStrategy(MappingStrategy mappingStrategy, ReplacementStrategy replacementStrategy, WriteStrategy writeStrategy) {
		//TODO
		this.mappingStrategy = mappingStrategy;
		this.mappingStrategy.mysetReplacementStrategy(replacementStrategy, writeStrategy);
		this.writeStrategy = writeStrategy;
		this.writeStrategy.setMappingStrategy(this.mappingStrategy);
		//replacementStrategy.setWriteStrategy(writeStrategy);
		//writeStrategy.setMappingStrategy(mappingStrategy);
		//mappingStrategy.setReplacementStrategy(replacementStrategy);
		//replacementStrategy.setWriteStrategy(writeStrategy);
	}

	/**
	 * 从32位物理地址(22位块号 + 10位块内地址)获取目标数据在内存中对应的块号
	 * 	 * @param addr
	 * @return
	 */
	public int getBlockNO(String addr) {
		Transformer t = new Transformer();
		return Integer.parseInt(t.binaryToInt("0" + addr.substring(0, 22)));
	}

	/**
	 * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
	 * @param sAddr 发生变化的数据段的起始地址
	 * @param len 数据段长度
	 */
	public void invalid(String sAddr, int len) {
		int from = getBlockNO(sAddr);
		Transformer t = new Transformer();
		int to = getBlockNO(t.intToBinary(String.valueOf(Integer.parseInt(t.binaryToInt("0" + sAddr)) + len - 1)));

		for (int blockNO=from; blockNO<=to; blockNO++) {
			int rowNO = mappingStrategy.map(blockNO);
			if (rowNO != -1) {
				cache.get(rowNO).validBit = false;
			}
		}

	}

	/**
	 * 清除Cache全部缓存
	 * 这个方法只会在测试的时候用到
	 */
	public void clear() {
		for (CacheLine line:cache.clPool) {
			if (line != null) {
				line.validBit = false;
			}
		}
	}

	/**
	 * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
	 * 这个方法仅用于测试
	 * @param lineNOs
	 * @param validations
	 * @param tags
	 * @return
	 */
	public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
		if (lineNOs.length != validations.length || validations.length != tags.length) {
			return false;
		}
		for (int i=0; i<lineNOs.length; i++) {
			CacheLine line = cache.get(lineNOs[i]);
			if (line.validBit != validations[i]) {
				return false;
			}
			if (!Arrays.equals(line.getTag(), tags[i])) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 负责对CacheLine进行动态初始化
	 */
	public class CacheLinePool {
		/**
		 * @param lines Cache的总行数
		 */
		CacheLinePool(int lines) {
			clPool = new CacheLine[lines];
		}
		public CacheLine[] clPool;
		public CacheLine get(int lineNO) {
			if (lineNO >= 0 && lineNO <clPool.length) {
				CacheLine l = clPool[lineNO];
				if (l == null) {
					clPool[lineNO] = new CacheLine();
					l = clPool[lineNO];
				}
				return l;
			}
			return null;
		}
	}

	/**
	 * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
	 */
	public class CacheLine {
		// 有效位，标记该条数据是否有效
		boolean validBit = false;
		// 脏位，标记该条数据是否被修改
		boolean dirty = false;
		// 用于LFU算法，记录该条cache使用次数
		int visited = 0;

		// 用于LRU和FIFO算法，记录该条数据时间戳
		Long timeStamp = 0l;

		// 标记，占位长度为()22位，有效长度取决于映射策略：
		// 直接映射: 12 位
		// 全关联映射: 22 位
		// (2^n)-路组关联映射: 22-(10-n) 位
		// 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(12位)+行号(10位)+块内地址(10位)，
		// 那么对于值为0b1111的tag应该表示为0000000011110000000000，其中前12位为有效长度，
		// 因为测试平台的原因，我们无法使用4GB的内存，但是我们还是选择用32位地址线来寻址
		char[] tag = new char[22];

		// 数据
		char[] data = new char[LINE_SIZE_B];

		char[] getData() {
			return this.data;
		}
		char[] getTag() {
			return this.tag;
		}
		boolean getValidBits(){return this.validBit;}
		void setTimeStamp(long ts){this.timeStamp  = ts;}
		long getTimeStamp(){return this.timeStamp;}

	}

	public CacheLine getCacheLine(int lineIndex){
		return this.cache.get(lineIndex);
	}

	public void setChlineData(CacheLine cacheLine, char[] data){
		cacheLine.data = data;
	}

	public char[] getChlineData(CacheLine cacheLine){
		return cacheLine.getData();
	}

	public void setChlineTag(CacheLine cacheLine, char[] tag){
		cacheLine.tag = tag;
	}

	public char[] getChlineTag(CacheLine cacheLine){
		return cacheLine.getTag();
	}

	public void setChlineValid(CacheLine cacheLine, boolean valid){
		cacheLine.validBit = valid;
	}

	public boolean getChlineValid(CacheLine cacheLine){
		return cacheLine.getValidBits();
	}

	public boolean getChlineDirty(CacheLine cacheLine){
		return cacheLine.dirty;
	}
	public void setChlineDirty(CacheLine cacheLine, boolean d){
		cacheLine.dirty = d;
	}

	public void changeTime(CacheLine cacheLine, long time){
		//设定指定的时间戳
		cacheLine.setTimeStamp(time);
	}

	public long getMinTime(){
		CacheLine[] lines = this.cache.clPool;
		long time = Long.MAX_VALUE;
		for (CacheLine line : lines) {
			if (line != null && line.getValidBits()) {//如果他是有效的也就是有数据的
				if (time > line.getTimeStamp())
					time = line.getTimeStamp();
			}
		}
		return time;
	}

	public int getMinTimeLine(int start,int end){
		CacheLine[] lines = this.cache.clPool;
		long time = Long.MAX_VALUE;
		int res = -1;
		for(int i = start; i < end; i ++){
			if(lines[i] != null && lines[i].getValidBits()){//如果他是有效的也就是有数据的
				if(time > lines[i].getTimeStamp()){
					time = lines[i].getTimeStamp();
					res = i;
				}
			}
		}
		return res;
	}
}
