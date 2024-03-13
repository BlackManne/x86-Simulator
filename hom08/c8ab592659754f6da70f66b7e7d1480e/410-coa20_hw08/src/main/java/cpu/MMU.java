package cpu;

import cpu.alu.ALU;
import memory.Cache;
import memory.Memory;
import memory.MemoryInterface;
import transformer.Transformer;


//MMU的逻辑：
//MMU使用虚拟内存，但是本质是不变的，计算机程序本质还是存储在内存中
//在读一个逻辑程序段的时候将这个程序段加载到物理内存，并且从物理内存中读取（1.内存更快，2.计算机真正的内存就是物理内存，他本来就应该在那里）。注意逻辑地址和物理地址不同


//1.在有mmu的情况下，读取数据由MMU进行，MMU.read方法读取一段逻辑地址中的数据，也就是读取磁盘中的数据
//2.MMU.read：首先先判断是什么模式（实模式，段式，段页式），在各自模式下首先访问内存，看看指定程序段是否在内存里，不在内存就载入内存然后从内存中读取；（先加载后读取——从内存中）
//实模式下直接从磁盘中加载，调用memory.REAL_LOAD方法
//段模式下通过逻辑地址解析段选择符，判断段选择符是否valid（也就是是否在内存中），如果在内存中就直接根据段表找到物理地址读取，如果不在内存中就去memory.SEG_LOAD加载段到内存中
//实模式和段模式下逻辑地址=线性地址：从内存中读取的时候physicalAddr=linearAddr调用memory.read
//段页式模式：段式+页式，依然是先加载后读取，加载步骤要先先加载段再加载页，因此首先判断段在不在内存，如果不在内存就seg_LOAD；然后加载页：获得这段内存中数据的虚拟页号，遍历，
//引入tlb，首先看在不在tlb里，如果在tlb里可以直接找到物理页号那么直接读取；如果不在tlb里先看在不在内存里，在内存里可以直接通过页表获得物理页号；不在内存里就PAGE_LOAD（两次查找：tlb，内存）
//3.Memory.read:段/页加载到内存之后要读取数据使用memory.read；如果引入了cache则修改为cache.read
//4.REAL_LOAD
//5.SEG_LOAD:首先要判断内存中够不够空间加载一个seg，腾出空间以后使用fit函数，段式的话读取磁盘数据，和段自己一起加载进去；页式的话不用读取磁盘数据，但是段占用的大小，进入页表什么的要提前规定好，所以不读数据，但是也要fit把内存中占据空间划分好。
//6.PAGE_LOAD
//7.TLB：删除页表项，查找页表项，向内存中加载页表项


/**
 * MMU接收一个48-bits的逻辑地址，并最终将其转换成32-bits的物理地址
 *
 * Memory.SEGMENT和Memory.PAGE标志用于表示是否开启分段和分页。
 * 实际上在机器里从实模式进入保护模式后分段一定会保持开启(即使实模式也会使用段寄存器)，因此一共只要实现三种模式的内存管理即可：实模式、只有分段、段页式
 * 所有模式的物理地址都采用32-bits，实模式的物理地址高位补0
 *
 * 大致流程(仅供参考)：
 * 		1. 逻辑地址：总长度48-bits(16-bits段选择符+32位段内偏移)
 * 		2. 段选择符高13-bits表示段描述符的索引，低3-bits本作业不使用
 *  * 		3. 通过段选择符查询段表，获得段描述符，包括32-bits的基地址、31-bits的限长、1-bit的有效位(判断段是否被加载到内存或者失效)
 *  * 	 		3.1 如果分页未启用且段未加载/失效，则将段从磁盘读取到内存(分段下根据段描述符中的32-bits磁盘基址，段页式下根据虚页号)
 * 		4. 根据基地址和段内偏移计算线性地址(32-bits，包括20-bits虚页页号和12-bits页内偏移)
 * 		5. 通过虚页页号查询页表，并获得20-bits的页框号和1-bit标志位(记录该页是否在内存中或者失效)
 * 			5.1 如果页不在内存，则将页从磁盘读取到内存
 * 		6. 页框号与页内偏移组合成物理地址，根据物理地址和数据长度读Memory
 */
public class MMU {

	private static MMU mmuInstance = new MMU();

	public MMU() {}

	public static MMU getMMU() {
		return mmuInstance;
	}

	public MemoryInterface memory = Memory.getMemory();

	public void setMemory(MemoryInterface memory) {
		this.memory = memory;
	}

	Transformer t = new Transformer();

	Cache cache = Cache.getCache();

	TLB tlb = TLB.getTLB();
	/**
	 * 地址转换
	 * @param logicAddr 48-bits逻辑地址。实模式和分段模式下，磁盘物理地址==内存物理地址，段页式下，磁盘物理地址==虚页号 * 页框大小 + 偏移量
	 * @param length 读取数据的长度
	 * @return 内存中的数据
	 */
	//TODO need to add cache and tlb
	public char[] read(String logicAddr, int length) {
		String linearAddr =  "";          // 32位线性地址
		String physicalAddr = "";   // 32位物理地址

		if (!Memory.PAGE) {  // 实模式或分段模式
			if(!Memory.SEGMENT){
				//TODO 获取线性地址并将数据加载到从磁盘加载到内存
				// 实模式：线性地址等于物理地址
				//此时是实模式
				linearAddr = toRealLinearAddr(logicAddr);
				//加载的任务从来都是内存来处理的，所以是各种load都是内存的方法
				memory.real_load(linearAddr, length);  // 从磁盘中加载到内存
			}else{
				//TODO 获取段号并且将段加载到内存，接着获取线性地址
				// 分段模式：线性地址等于物理地址
				int segIndex = getSegIndex(logicAddr);
				if( !memory.isValidSegDes( segIndex ) ){
					memory.seg_load(segIndex);
				}
				//获得地址的时候段已经被加载了，而解析地址的下一步就是从内存中读取数据，所以在解析地址之前必须先把段加载到内存中
				linearAddr = toSegLinearAddr(logicAddr);

			}
			physicalAddr = linearAddr;

		} else {  // 段页式
			//1.加载段
			//2.读取段中每一个页的数据，根本就是要知道读取哪些页，中间二十位获得初始页号；根据length获得结束的页号（注意不满一页的时候要读取一页）
			//offset是对于首地址而言的，所以只有读取第一页的时候才会使用，后面读取的都是整页
			//3.一页一页读取，如果在tlb中从tlb中获得，如果不在tlb中去找内存在内存中页表获得；如果不在内存中page_load
			int segIndex = getSegIndex( logicAddr );
			int limit = Integer.parseInt( t.binaryToInt( String.valueOf( memory.getLimitOfSegDes( segIndex ) )));  // 段的限长

			if( length > limit*2 ){
				throw new SecurityException( "访问限制" );
			}
			if( !memory.isValidSegDes( segIndex ) ) {
				// 缺段中断，该段不在内存中，在内存中为该段分配内存
				memory.seg_load( segIndex );
			}
			//TODO 计算段页式下的起始虚拟页号与结束虚拟页号与偏移量
			// 线性地址的中间20位表示虚拟页号 最后12位表示页内偏移
			linearAddr = toSegLinearAddr(logicAddr);
			int startvPageNo = Integer.parseInt(t.binaryToInt(linearAddr.substring(0,20)));
			int offset = Integer.parseInt(t.binaryToInt(linearAddr.substring(20, 32)));   //页内偏移
			//如果numdiffer=0就有问题了，这里默认限定了numdiffer>=1
			int numDiffer = length / Memory.PAGE_SIZE_B;
			//如果数据不满一页，要读取一页
			if(numDiffer == 0){
				numDiffer = 1;
			}
			int endvPageNo = startvPageNo + numDiffer - 1;

			char[] res = new char[length];
			int p=0;

			for(int i=startvPageNo;i<=endvPageNo;i++){
				String pageAddr;
				//TODO ADD TLB HERE
				//首先判断这个页在不在TLB里面，在TLB里面就不可能invalid
				//加载页
				if(tlb.isMatch(i) != -1){
					pageAddr = String.valueOf(tlb.getFrameOfPage(tlb.isMatch(i)));
				}else {
					if (!memory.isValidPage(i)) {
						// 缺页
						pageAddr = memory.page_load(segIndex, i);   // 从磁盘中加载到内存
					} else {
						pageAddr = String.valueOf(memory.getFrameOfPage(i));   // 访问页表
					}
				}

				if(i==startvPageNo){  // 读第一页
					// 读第一页的
					char[] pageData = cache.read(pageAddr, Memory.PAGE_SIZE_B);
					for(int j=offset;j<Memory.PAGE_SIZE_B && p<length;j++){
						res[p++] = pageData[j];
					}
				}else{   // 读取非第一页的数据
					char[] pageData = cache.read(pageAddr, Memory.PAGE_SIZE_B);
					for(int j=0;j<Memory.PAGE_SIZE_B && p<length;j++){
						res[p++] = pageData[j];
					}
				}
			}
			return res;
		}
		//第二个return是段式和实模式的读取
		return cache.read(physicalAddr, length);
	}

	/**
	 * 该方法仅用于测试，请勿修改
	 * @param logicAddr
	 * @param length
	 * @return
	 */
	public void readTest(String logicAddr, int length){
		System.out.print(this.read(logicAddr, length));
	}

	/**
	 * 根据逻辑地址找到对应的段号
	 * 段选择符高13-bits表示段描述符的索引，低3-bits本作业不使用
	 * @param logicAddr 逻辑地址
	 * @return
	 */
	private int getSegIndex(String logicAddr){
		//TODO
		int segIndex = Integer.parseInt(t.binaryToInt(logicAddr.substring(0,13)));    //高13位标识段描述符的索引
		return segIndex;
	}


	/**
	 * 实模式下的逻辑地址转线性地址
	 * @param logicAddr 48位 = 16位段选择符(高13位index选择段表项) + 32位offset，计算公式为：①(16-bits段寄存器左移4位 + offset的低16-bits) = 20-bits物理地址 ②高位补0到32-bits
	 * @return 32-bits实模式线性地址
	 */
	private String toRealLinearAddr(String logicAddr){
		//TODO
		ALU alu = new ALU();
		String lowOffset = logicAddr.substring(32,48);   //16位
		String segIndex = logicAddr.substring(0,16);
		//段的地址扩展到20位
		segIndex = segIndex + "0000";
		String res = alu.add(lowOffset + "0000000000000000", segIndex + "000000000000").substring(0,20);
		res = "000000000000" + res;
		return res;
	}


	/**
	 * 分段模式下的逻辑地址转线性地址
	 * @param logicAddr 48位 = 16位段选择符(高13位index选择段表项) + 32位段内偏移
	 * @return 32-bits 线性地址
	 */
	private String toSegLinearAddr(String logicAddr) {
		//TODO linearAddr = SegBase + offset
		int segIndex = getSegIndex(logicAddr);
		String segBase = Memory.getSegBase(segIndex);
		String offset = logicAddr.substring(16,48);   //32位offset
		String res = add(segBase.toCharArray(), offset);
		return res;
	}


	/**
	 * 基地址+偏移地址
	 * @param base 20/32位基地址
	 * @param offsetStr 20/32位偏移
	 * @return 32-bits 线性地址
	 */
	private String add(char[] base, String offsetStr) {
		char[] offset = offsetStr.toCharArray();
		StringBuilder res = new StringBuilder(  );
		char carry = '0';
		for(int i=base.length-1;i>=0;i-- ){
			res.append ((carry - '0') ^ (base[i] - '0') ^ (offset[i] - '0'));
			carry = (char) (((carry - '0') & (base[i] - '0')) | ((carry - '0') & (offset[i] - '0')) | ((base[i] - '0') & (offset[i] - '0'))+'0');
		}

		for(int i=0;i<32-res.length();i++){
			res.append( "0" );
		}
		return res.reverse().toString();
	}

}
