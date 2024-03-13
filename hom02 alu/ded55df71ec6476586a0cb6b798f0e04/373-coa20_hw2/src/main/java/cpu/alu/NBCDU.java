package cpu.alu;

import transformer.Transformer;

public class NBCDU {

	// 模拟寄存器中的进位标志位
	private String CF = "0";

	// 模拟寄存器中的溢出标志位
	private String OF = "0";

	char[] fourBitsAddResult = new char[4];
	char[] resChar = new char[32];
	Transformer transformer = new Transformer();
	/**
	 *
	 * @param a A 32-bits NBCD String
	 * @param b A 32-bits NBCD String
	 * @return a + b
	 */
	public String add(String a, String b) {
		// TODO
		String flaga = a.substring(0,4);
		String flagb = b.substring(0,4);
		String res = "";

		if(flaga.equals(flagb)){  //说明符号相同，可以做加法
			String tempa = "";
			String tempb = "";
			String temp;
			for(int i = 0; i < 7; i ++){
				tempa = a.substring(32 - 4 * i - 4, 32 - 4 * i);
				tempb = b.substring(32 - 4 * i - 4, 32 - 4 * i);
				temp = fourAdder(tempa, tempb);
				if(CF.equals("1")){     //如果产生进位，第一位一定剩下0，不可能是1（是1说明三个1）
					//注意要重置CF
					CF = "0";
					temp = fourAdder(temp, "0110");   //更新res
					CF = "1";
				}else{
					//现在没有进位，要处理数值是否比9大
					int tempinint = temp.charAt(3) - '0' + 2 * (temp.charAt(2) - '0') + 4 * (temp.charAt(1) - '0') + 8 * (temp.charAt(0) - '0');
					if(tempinint > 9){
						temp = fourAdder(temp, "0110");   //更新res
						CF = "1";
					}
				}
				res = temp + res;
			}
			res = flaga + res;
		}else{
			//调整一下b的符号
			b = flaga + b.substring(4);
			res = sub(b, a);
		}
		return res;
	}

	/***
	 *
	 * @param a A 32-bits NBCD String
	 * @param b A 32-bits NBCD String
	 * @return b - a
	 */
	public String sub(String a, String b) {
		// TODO
//
		//表示b-a
		String flaga = a.substring(0,4);
		String flagb = b.substring(0,4);
		String res = "";
		if(!flaga.equals(flagb)){
			a = flagb + a.substring(4);
			res = add(a, b);
			return res;
		}
		//如果符号相同，同符号减法

		//首先对a进行按位取反
		String newa = flaga;
		for(int j = 1; j <= 7; j ++){
			newa = newa + toNegation(a.substring(j * 4, j * 4 + 4));
		}
		//恢复CF
		String nowCF = CF;
		CF = "0";
		//取反以后+1
		a = add(newa, flagb +"0000000000000000000000000001");
		CF = nowCF;

		String tempa = "";
		String tempb = "";
		String temp;
		for(int i = 0; i < 7; i ++){
			tempa = a.substring(32 - 4 * i - 4, 32 - 4 * i);
			tempb = b.substring(32 - 4 * i - 4, 32 - 4 * i);
			temp = fourAdder(tempa, tempb);
			//完成4位加法
			if(CF.equals("1")){     //如果产生进位，第一位一定剩下0，不可能是1（是1说明三个1）
				//注意要重置CF
				CF = "0";
				temp = fourAdder(temp, "0110");   //更新res
				CF = "1";
			}else{
				//现在没有进位，要处理数值是否比9大
				int tempinint = temp.charAt(3) - '0' + 2 * (temp.charAt(2) - '0') + 4 * (temp.charAt(1) - '0') + 8 * (temp.charAt(0) - '0');
				if(tempinint > 9){
					temp = fourAdder(temp, "0110");   //更新res
					CF = "1";
				}
			}
			res = temp + res;
		}
		//用正数表示0
		if(res.equals("0000000000000000000000000000"))
			return "1100" + res;
		if(CF.equals("1")){    //如果有一位进位，不用改变符号
			res = flagb + res;
		}else{
			flagb = "110" + String.valueOf('1' - flaga.charAt(3));  //更换flaga符号
			res = flagb + toNegation32(res);
		}
		return res;
	}

	public String oneAdder(String a, String b){
		int res = Integer.parseInt(a)+ Integer.parseInt(b) + Integer.parseInt(CF);
		if(res > 2){
			CF = "1";
			return "1";
		}else if(res == 2){
			CF = "1";
			return "0";
		}else{          //如果数值是1或者0
			CF = "0";
			return String.valueOf(res);
		}
	}

	public String fourAdder(String a, String b){
		String tempa;
		String tempb;
		String res = "";
		for(int i = 3; i >= 0; i --){
			tempa = String.valueOf(a.charAt(i));
			tempb = String.valueOf(b.charAt(i));
			res = oneAdder(tempa, tempb) + res;
		}

//		//完成4位加法
//		if(CF.equals("1")){     //如果产生进位，第一位一定剩下0，不可能是1（是1说明三个1）
//			String nowCF = CF;
//			//注意要重置CF
//			CF = "0";
//			res = fourAdder(res, "0110");   //更新res
//			CF = nowCF;
//			return res;
//		}
//		//现在没有进位，要处理数值是否比9大
//		int resinint = res.charAt(3) - '0' + 2 * (res.charAt(2) - '0') + 4 * (res.charAt(1) - '0') + 8 * (res.charAt(0) - '0');
//		if(resinint > 9){
//			String nowCF = CF;
//			//注意要重置CF
//			CF = "0";
//			res = fourAdder(res, "0110");   //更新res
//			CF = nowCF;
//		}
		return res;
	}

	public String toNegation(String a){
		String temp = "";
		for(int i = 0; i < 4; i ++){
			temp =  temp + String.valueOf('1' - a.charAt(i));
		}//完成按位取反，再加1010
		String res = "";
		String b = "1010";
		String tempa, tempb;
		for(int i = 3; i >= 0; i --){
			tempa = String.valueOf(temp.charAt(i));
			tempb = String.valueOf(b.charAt(i));
			res = oneAdder(tempa, tempb) + res;
		}
		//重置CF
		CF = "0";
		return res;
	}

	public String toNegation32(String a){
		//传入的a是28位的
		String res = "";
		for(int i = 0; i < 7; i ++){
			res = res + toNegation(a.substring(i * 4, i * 4 + 4));
		}
		//取了反记得+1！！！！！！
		return add("1100" + res, "11000000000000000000000000000001").substring(4);
	}
}
