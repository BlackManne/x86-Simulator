package cpu.alu;

import transformer.Transformer;
import util.BinaryIntegers;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 * TODO: 乘除
 */
public class ALU {

	// 模拟寄存器中的进位标志位
    private String CF = "0";

    // 模拟寄存器中的溢出标志位
    private String OF = "0";

    //private int c = 0; //自定义的进位标志位

	/**
	 * 返回两个二进制整数的乘积(结果低位截取后32位)
	 * @param src 32-bits
	 * @param dest 32-bits
	 * @return 32-bits
	 */
	public String mul (String src, String dest){
		//TODO
		if(src.equals("00000000000000000000000000000000") || dest.equals("00000000000000000000000000000000")){
			return "00000000000000000000000000000000";
		}
		//使用布斯乘法：首先将32位扩展到64位（左边添加32个0），然后由于布斯乘法的要求，要在最右边再加上一个0，相当于扩展到65位
		src = "00000000000000000000000000000000".concat(src);
		src = src.concat("0");
		//分别对应着Yi和Yi+1
		int Ycurrent;
		int Ynext;
		String temp; //用来进行前32位的加法
		for(int i = 0; i < 32; i ++){
			Ycurrent = Integer.parseInt(String.valueOf(src.charAt(64)));
			Ynext = Integer.parseInt(String.valueOf(src.charAt(63)));
			if(Ycurrent - Ynext == 0)
				src = shrZero(src);
			else if(Ycurrent - Ynext == -1){
				temp = add(src.substring(0, 32), toNegative(dest));
				src = temp + src.substring(32);
				src = shrZero(src);
			}
			else if (Ycurrent - Ynext == 1){
				temp = add(src.substring(0, 32), dest);
				src = temp + src.substring(32);
				src = shrOne(src);
			}
		}
		//直接截取低32位作为输出
	    return src.substring(32, 64);
    }

    /**
     * 返回两个二进制整数的除法结果 operand1 ÷ operand2
     * @param operand1 32-bits
     * @param operand2 32-bits
     * @return 65-bits overflow + quotient + remainder
     */
    public String div(String operand1, String operand2) throws ArithmeticException{
//    	//TODO
		//非恢复余数法：
		//1.特判：注意使用BINARYINTEGER里面实现好的值判断0，ArithmeticException，0
		//2.扩展到64位
		//3.进行32次循环，具体步骤：
		//(1)左移一位（但是不上具体数值，通过左移来完成）
		//(2)目前63位数和除数的符号，如果相同做加法，不同做减法（前32位）
		//(3)新的63位数的符号和做加减法之前的63位数如果符号相同或者数值等于0，说明enough，这个数保留，上1，否则说明不够，使用原来的数，上0
		//注意！！！！这里的等于0非常关键，如果没有等于0会出现问题！！
		//感觉这里“之前的63位数”意义不大，因为前面补充的位数是32个，又因为加减法不会产生不同的符号，所以符号一直是一样的
		//验证过了，没有问题！所以事实上并不需要保存过程中remainder的符号
		//(4)最后一步：如果除数和被除数的符号不同，商取反


		String overflow = "0";
		if(operand1.equals("10000000000000000000000000000000"))
			overflow = "1";
		if(operand1.equals("00000000000000000000000000000000") && operand2.equals("00000000000000000000000000000000"))
			return "NaN";
		if(operand1.equals("00000000000000000000000000000000"))
			return "00000000000000000000000000000000000000000000000000000000000000000";
		if(operand2.equals("00000000000000000000000000000000"))
			throw new ArithmeticException();

		//对被除数做有符号扩展，扩展到64位
		if(operand1.charAt(0) == '0')
			operand1 = BinaryIntegers.ZERO + operand1;
		else
			operand1 = BinaryIntegers.NegativeOne + operand1;

		char flagDividend = operand1.charAt(0);   //这个用来保存最开始被除数的符号
		char flagRemainder;
		char flagDivisor = operand2.charAt(0);    //除数的符号始终没有改变
		String remainder = null;
		String quotient = null;
		boolean enough = true;

		Transformer transformer = new Transformer();
		String temp;
		//恢复余数法
//		//先左移一位
//		for(int i = 1;i <= 32;i ++){
//			//64位数左移一位
//			operand1 = operand1.substring(1);
//			remainder = operand1.substring(0, 32);
//			quotient = operand1.substring(32);
////			flagRemainder = remainder.charAt(0);
//			if(flagDividend != flagDivisor){   //做加法
//				temp = add(remainder, operand2);
//				if(temp.charAt(0) == flagDividend || temp.equals(BinaryIntegers.ZERO)){    //符号没有变化，说明足够
//					quotient = quotient + "1";
//					operand1 = temp + quotient;
//				}else{
//					quotient = quotient + "0";
//					operand1 = remainder + quotient;
//				}
//			}
//			else{
//				temp = sub(remainder, operand2);
//				if(temp.charAt(0) == flagDividend || temp.equals(BinaryIntegers.ZERO)){    //符号没有变化，说明足够
//					quotient = quotient + "1";
//					operand1 = temp + quotient;
//				}else{
//					quotient = quotient + "0";
//					operand1 = remainder + quotient;
//				}
//			}
//			//System.out.println("remainder" + remainder);
//			//System.out.println("quotient:" + quotient);
//		}

		//不恢复余数法
		//做31次，最后一次恢复余数
		for(int i = 1; i <= 31; i ++){
			operand1 = operand1.substring(1);
			flagRemainder = operand1.charAt(0);
			//如果符号不同并且足够，那么就是做加法
			//如果符号相同并且不够，那么就是做加法
			if((flagRemainder != flagDivisor && enough) || (flagRemainder == flagDivisor && !enough)){
				operand1 = add(operand1.substring(0,32), operand2) + operand1.substring(32);
				if(operand1.charAt(0) == flagDividend || operand1.equals(BinaryIntegers.ZERO)){    //符号没有变化，说明足够
					operand1 = operand1 + "1";
					enough = true;
				}else{
					operand1 = operand1 + "0";
					enough = false;
				}
			}else{
				operand1 = sub(operand1.substring(0,32), operand2) + operand1.substring(32);
				if(operand1.charAt(0) == flagDividend || operand1.equals(BinaryIntegers.ZERO)){    //符号没有变化，说明足够
					operand1 = operand1 + "1";
					enough = true;
				}else{
					operand1 = operand1 + "0";
					enough = false;
				}
			}
		}
		if(operand1.charAt(0) != flagDivisor)
			if(flagDividend == flagDivisor)
				operand1 = add(operand1, )


		if(flagDividend != flagDivisor){
			return overflow + toNegative(operand1.substring(32)) + operand1.substring(0, 32);
		}
		return overflow + operand1.substring(32) + operand1.substring(0, 32);



//		//先做32次，最后一次做完之后还要恢复余数
////		for(int i = 1; i <= 32; i ++){
////			remainder = operand1.substring(0, 32);
////			//获取quotient的时候直接左移一位
////			quotient = operand1.substring(33);
////			flagRemainder = remainder.charAt(0);
////			if(flagRemainder != flagDivisor){
////				if(enough){
////					remainder = add(remainder, operand2);
////				}else
////					remainder = sub(remainder, operand2);
////
////				if(remainder.charAt(0) == flagRemainder){
////					quotient = quotient + "1";
////					enough = true;
////				}else{
////					quotient = quotient + "0";
////					enough= false;
////				}
////				operand1 = remainder + quotient;
////			}else{
////				if(enough)
////					remainder = sub(remainder, operand2);
////				else
////					remainder = add(remainder, operand2);
////
////				if(remainder.charAt(0) == flagRemainder){
////					quotient = quotient + "1";
////					enough = true;
////				}else {
////					quotient = quotient + "0";
////					enough= false;
////				}
////				operand1 = remainder + quotient;
////			}
////		}
////
////		if(remainder.charAt(0) != flagDividend){
////			if(flagDividend == flagDivisor)
////				remainder = add(remainder, operand2);
////			else
////				remainder = sub(remainder, operand2);
////		}
////
////		if(flagDividend == flagDivisor)
////			quotient = add(quotient, "00000000000000000000000000000001");
////
////		return overflow + quotient + remainder;

/*
//		//引入一个pp学长的做法：逐次相减法——就像正常的运算一样，一次一次减，够减就+1
//		//跟imod的思想一样，不管够不够，每次都减，如果不够就返回上一次的值就行了
//		//每次都减才能知道究竟有几个除数的大小
//		if (operand1.equals(BinaryIntegers.ZERO) && operand2.equals(BinaryIntegers.ZERO)) return BinaryIntegers.NaN;
//		if (operand2.equals(BinaryIntegers.ZERO)) throw new ArithmeticException();
//		if (operand1.equals("10000000000000000000000000000000") && operand2.equals("11111111111111111111111111111111")) return "11000000000000000000000000000000000000000000000000000000000000000";
//		if (operand1.equals("11111111111111111111111111111000") && operand2.equals("00000000000000000000000000000010")) return "01111111111111111111111111111110000000000000000000000000000000000";
//		String overflow = "0";
//		String quotient = BinaryIntegers.ZERO;
//		String remainder = operand1;
//		while(remainder.charAt(0) == operand1.charAt(0)){    //余数的符号和被除数不一样的时候就停止循环，这个时候上一个值是对的
//			if(operand1.charAt(0) == operand2.charAt(0)){
//				remainder = sub(remainder, operand2);
//				if(operand1.charAt(0) == remainder.charAt(0)){
//					quotient = add(BinaryIntegers.One, quotient);
//				}
//			}else{
//				remainder = add(operand2, remainder);
//				if(operand1.charAt(0) == remainder.charAt(0)){
//					quotient = add(BinaryIntegers.One, quotient);
//				}
//			}
//		}
//		//这种做法不需要最后对余数进行一个取反（模拟的是正常的除法），但是减多了，要恢复一下
//		remainder = add(remainder, operand2);
//
//		return overflow + quotient + remainder;

 */

    }
    //shr是右移，shl是左移
    public String shrOne (String dest){
    	return "1" + dest.substring(0, 64);
	}
	public String shrZero(String dest){
		return "0" + dest.substring(0, 64);
	}
	public String shlOne(String dest){
    	return dest.substring(1, 64) + "1";
	}
	public String shlZero(String dest){
    	return dest.substring(1, 64) + "0";
	}
	public String shlOne32(String dest){
    	return dest.substring(1, 32) + "1";
	}
	public String shlZero32(String dest){
    	return dest.substring(1, 32) + "0";
	}
	private String toNegative(String code) {
		char[] ans = code.toCharArray();
		for (int i = 0; i < 32; ++i) {
			if (ans[i] == '0') ans[i] = '1';
			else ans[i] = '0';
		}
		ans[31]++;
		for (int i = 31; i > 0; --i) {
			if (ans[i] == '2') {
				ans[i - 1]++;
				ans[i] = '0';
			}
		}
		if (ans[0] == '2') ans[0] = '0';
		return String.valueOf(ans);
	}
	//add two integer
	String add(String src, String dest) {
		StringBuilder ans = new StringBuilder();
		 int c = 0;
		for (int i = 31; i >= 0; --i) {
			int a = src.charAt(i) - '0' + dest.charAt(i) - '0';
			if (a + c > 1) {
				ans.append(a + c - 2);
				c = 1;
			} else {
				ans.append(a + c);
				c = 0;
			}
		}
		return ans.reverse().toString();
	}
	String sub(String src, String dest){
    	//src - dest
    	return add(src, toNegative(dest));
	}
}
