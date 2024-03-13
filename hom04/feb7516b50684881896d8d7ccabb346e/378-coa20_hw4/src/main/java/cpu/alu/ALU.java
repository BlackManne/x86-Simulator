package cpu.alu;

import transformer.Transformer;
import util.BinaryIntegers;

/**
 * Arithmetic Logic Unit
 * ALU��װ��
 * TODO: �˳�
 */
public class ALU {

	// ģ��Ĵ����еĽ�λ��־λ
    private String CF = "0";

    // ģ��Ĵ����е������־λ
    private String OF = "0";

    //private int c = 0; //�Զ���Ľ�λ��־λ

	/**
	 * �������������������ĳ˻�(�����λ��ȡ��32λ)
	 * @param src 32-bits
	 * @param dest 32-bits
	 * @return 32-bits
	 */
	public String mul (String src, String dest){
		//TODO
		if(src.equals("00000000000000000000000000000000") || dest.equals("00000000000000000000000000000000")){
			return "00000000000000000000000000000000";
		}
		//ʹ�ò�˹�˷������Ƚ�32λ��չ��64λ��������32��0����Ȼ�����ڲ�˹�˷���Ҫ��Ҫ�����ұ��ټ���һ��0���൱����չ��65λ
		src = "00000000000000000000000000000000".concat(src);
		src = src.concat("0");
		//�ֱ��Ӧ��Yi��Yi+1
		int Ycurrent;
		int Ynext;
		String temp; //��������ǰ32λ�ļӷ�
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
		//ֱ�ӽ�ȡ��32λ��Ϊ���
	    return src.substring(32, 64);
    }

    /**
     * �������������������ĳ������ operand1 �� operand2
     * @param operand1 32-bits
     * @param operand2 32-bits
     * @return 65-bits overflow + quotient + remainder
     */
    public String div(String operand1, String operand2) throws ArithmeticException{
//    	//TODO
		//�ǻָ���������
		//1.���У�ע��ʹ��BINARYINTEGER����ʵ�ֺõ�ֵ�ж�0��ArithmeticException��0
		//2.��չ��64λ
		//3.����32��ѭ�������岽�裺
		//(1)����һλ�����ǲ��Ͼ�����ֵ��ͨ����������ɣ�
		//(2)Ŀǰ63λ���ͳ����ķ��ţ������ͬ���ӷ�����ͬ��������ǰ32λ��
		//(3)�µ�63λ���ķ��ź����Ӽ���֮ǰ��63λ�����������ͬ������ֵ����0��˵��enough���������������1������˵��������ʹ��ԭ����������0
		//ע�⣡����������ĵ���0�ǳ��ؼ������û�е���0��������⣡��
		//�о����֮ǰ��63λ�������岻����Ϊǰ�油���λ����32��������Ϊ�Ӽ������������ͬ�ķ��ţ����Է���һֱ��һ����
		//��֤���ˣ�û�����⣡������ʵ�ϲ�����Ҫ���������remainder�ķ���
		//(4)���һ������������ͱ������ķ��Ų�ͬ����ȡ��


		String overflow = "0";
		if(operand1.equals("10000000000000000000000000000000"))
			overflow = "1";
		if(operand1.equals("00000000000000000000000000000000") && operand2.equals("00000000000000000000000000000000"))
			return "NaN";
		if(operand1.equals("00000000000000000000000000000000"))
			return "00000000000000000000000000000000000000000000000000000000000000000";
		if(operand2.equals("00000000000000000000000000000000"))
			throw new ArithmeticException();

		//�Ա��������з�����չ����չ��64λ
		if(operand1.charAt(0) == '0')
			operand1 = BinaryIntegers.ZERO + operand1;
		else
			operand1 = BinaryIntegers.NegativeOne + operand1;

		char flagDividend = operand1.charAt(0);   //������������ʼ�������ķ���
		char flagRemainder;
		char flagDivisor = operand2.charAt(0);    //�����ķ���ʼ��û�иı�
		String remainder = null;
		String quotient = null;
		boolean enough = true;

		Transformer transformer = new Transformer();
		String temp;
		//�ָ�������
//		//������һλ
//		for(int i = 1;i <= 32;i ++){
//			//64λ������һλ
//			operand1 = operand1.substring(1);
//			remainder = operand1.substring(0, 32);
//			quotient = operand1.substring(32);
////			flagRemainder = remainder.charAt(0);
//			if(flagDividend != flagDivisor){   //���ӷ�
//				temp = add(remainder, operand2);
//				if(temp.charAt(0) == flagDividend || temp.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
//					quotient = quotient + "1";
//					operand1 = temp + quotient;
//				}else{
//					quotient = quotient + "0";
//					operand1 = remainder + quotient;
//				}
//			}
//			else{
//				temp = sub(remainder, operand2);
//				if(temp.charAt(0) == flagDividend || temp.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
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

		//���ָ�������
		//��31�Σ����һ�λָ�����
		for(int i = 1; i <= 31; i ++){
			operand1 = operand1.substring(1);
			flagRemainder = operand1.charAt(0);
			//������Ų�ͬ�����㹻����ô�������ӷ�
			//���������ͬ���Ҳ�������ô�������ӷ�
			if((flagRemainder != flagDivisor && enough) || (flagRemainder == flagDivisor && !enough)){
				operand1 = add(operand1.substring(0,32), operand2) + operand1.substring(32);
				if(operand1.charAt(0) == flagDividend || operand1.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
					operand1 = operand1 + "1";
					enough = true;
				}else{
					operand1 = operand1 + "0";
					enough = false;
				}
			}else{
				operand1 = sub(operand1.substring(0,32), operand2) + operand1.substring(32);
				if(operand1.charAt(0) == flagDividend || operand1.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
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



//		//����32�Σ����һ������֮��Ҫ�ָ�����
////		for(int i = 1; i <= 32; i ++){
////			remainder = operand1.substring(0, 32);
////			//��ȡquotient��ʱ��ֱ������һλ
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
//		//����һ��ppѧ������������������������������������һ����һ��һ�μ���������+1
//		//��imod��˼��һ�������ܹ�������ÿ�ζ�������������ͷ�����һ�ε�ֵ������
//		//ÿ�ζ�������֪�������м��������Ĵ�С
//		if (operand1.equals(BinaryIntegers.ZERO) && operand2.equals(BinaryIntegers.ZERO)) return BinaryIntegers.NaN;
//		if (operand2.equals(BinaryIntegers.ZERO)) throw new ArithmeticException();
//		if (operand1.equals("10000000000000000000000000000000") && operand2.equals("11111111111111111111111111111111")) return "11000000000000000000000000000000000000000000000000000000000000000";
//		if (operand1.equals("11111111111111111111111111111000") && operand2.equals("00000000000000000000000000000010")) return "01111111111111111111111111111110000000000000000000000000000000000";
//		String overflow = "0";
//		String quotient = BinaryIntegers.ZERO;
//		String remainder = operand1;
//		while(remainder.charAt(0) == operand1.charAt(0)){    //�����ķ��źͱ�������һ����ʱ���ֹͣѭ�������ʱ����һ��ֵ�ǶԵ�
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
//		//������������Ҫ������������һ��ȡ����ģ����������ĳ����������Ǽ����ˣ�Ҫ�ָ�һ��
//		remainder = add(remainder, operand2);
//
//		return overflow + quotient + remainder;

 */

    }
    //shr�����ƣ�shl������
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
