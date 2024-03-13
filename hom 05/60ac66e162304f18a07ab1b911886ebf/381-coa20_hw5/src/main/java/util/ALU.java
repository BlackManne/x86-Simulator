package util;

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
		//��չsrc��65λ
		src = "00000000000000000000000000000000".concat(src);
		src = src.concat("0");
		//�ֱ��Ӧ��Yi��Yi+1
		int Ycurrent;
		int Ynext;
		String temp; //��������ǰ32λ�ļӷ�
		for(int i = 0; i < 32; i ++){
			Ycurrent = Integer.parseInt(String.valueOf(src.charAt(64)));
			Ynext = Integer.parseInt(String.valueOf(src.charAt(63)));
			//if(Ycurrent - Ynext == 0)
				//src = shrZero(src);
			if(Ycurrent - Ynext == -1){
				temp = add(src.substring(0, 32), toNegative(dest));
				src = temp + src.substring(32);
				//src = shrZero(src);
			}
			else if (Ycurrent - Ynext == 1){
				temp = add(src.substring(0, 32), dest);
				src = temp + src.substring(32);
				//src = shrOne(src);
			}
			src = sarZero(src);
			//注意是算术右移，算术右移跟-X,+X,+0是哪一个没有任何关系
			//只与第一个符号有关系
		}
	    return src;
    }

    /**
     * �������������������ĳ������ operand1 �� operand2
     * @param operand1 32-bits
     * @param operand2 32-bits
     * @return 65-bits overflow + quotient + remainder
     */
    public String div(String operand1, String operand2) throws ArithmeticException{
    	//TODO
		String overflow = "0";
		/*if(operand1.equals("10000000000000000000000000000000"))
			overflow = "1";
		if(operand1.equals("00000000000000000000000000000000") && operand2.equals("00000000000000000000000000000000"))
			return "NaN";
		if(operand1.equals("00000000000000000000000000000000"))
			return "00000000000000000000000000000000000000000000000000000000000000000";
		if(operand2.equals("00000000000000000000000000000000"))
			throw new ArithmeticException();

		 */

		//�Ա��������з�����չ����չ��64λ
		/*if(operand1.charAt(0) == '0')
			operand1 = BinaryIntegers.ZERO + operand1; //右边补0
		else
			operand1 = BinaryIntegers.NegativeOne + operand1;
		 */
		operand1 = BinaryIntegers.ZERO +operand1;


		char flagDividend = operand1.charAt(0);   //������������ʼ�������ķ���
		char flagRemainder;
		char flagDivisor = operand2.charAt(0);    //�����ķ���ʼ��û�иı�
		String remainder = null;
		String quotient = null;
		boolean enough = true;

		Transformer transformer = new Transformer();
		String temp;
		//������һλ
		for(int i = 1;i <= 32; i ++){
			operand1 = operand1.substring(1);
			remainder = operand1.substring(0, 32);
			quotient = operand1.substring(32);
			flagRemainder = remainder.charAt(0);
			if(flagRemainder != flagDivisor){   //���ӷ�
				temp = add(remainder, operand2);
				if(temp.charAt(0) == flagRemainder || temp.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
					quotient = quotient + "1";
					operand1 = temp + quotient;
				}else{
					quotient = quotient + "0";
					operand1 = remainder + quotient;
				}
			}
			else{
				temp = sub(remainder, operand2);
				if(temp.charAt(0) == flagRemainder || temp.equals(BinaryIntegers.ZERO)){    //����û�б仯��˵���㹻
					quotient = quotient + "1";
					operand1 = temp + quotient;
				}else{
					quotient = quotient + "0";
					operand1 = remainder + quotient;
				}
			}
			//System.out.println("remainder" + remainder);
			//System.out.println("quotient:" + quotient);
		}
		//if(flagDividend != flagDivisor){
			//return overflow + toNegative(operand1.substring(32)) + operand1.substring(0, 32);
		//}

		for(int i = 1; i <= 32; i ++){
			remainder = operand1.substring(0, 32);
			quotient = operand1.substring(32);
		}

		quotient = operand1.substring(32);
		quotient = (quotient + BinaryIntegers.ZERO).substring(31,63);
		remainder = operand1.substring(4,32) + "0000";
		/*int lastIndexOf1 = quotient.lastIndexOf("1");
		remainder = remainder.substring(lastIndexOf1);
		for(int i = 0; i < lastIndexOf1; i ++){
			remainder = "0" + remainder;
		}

		 */
		String res = add(quotient, remainder);
		return res;


		//return remainder +quotient;

		//����32�Σ����һ������֮��Ҫ�ָ�����
		/*for(int i = 1; i <= 32; i ++){
			remainder = operand1.substring(0, 32);
			//��ȡquotient��ʱ��ֱ������һλ
			quotient = operand1.substring(32);
			flagRemainder = remainder.charAt(0);
			if(flagRemainder != flagDivisor){
				if(enough){
					remainder = add(remainder, operand2);
				}else
					remainder = sub(remainder, operand2);

				if(remainder.charAt(0) == flagRemainder){
					operand1 = shlOne(remainder + quotient);
					enough = true;
				}else{
					operand1 = shlZero(remainder + quotient);
					enough= false;
				}
			}else{
				if(enough)
					remainder = sub(remainder, operand2);
				else
					remainder = add(remainder, operand2);

				if(remainder.charAt(0) == flagRemainder){
					operand1 = shlOne(remainder + quotient);
					enough = true;
				}else {
					operand1 = shlZero(remainder + quotient);
					enough= false;
				}
			}
			//System.out.println(operand1);
		}

		if(remainder.charAt(0) != flagDividend){
			if(flagDividend == flagDivisor)
				remainder = add(remainder, operand2);
			else
				remainder = sub(remainder, operand2);
		}

		if(flagDividend == flagDivisor)
			quotient = add(quotient, "00000000000000000000000000000001");

		return quotient;

		 */
/*
		for(int i = 1; i <= 32; i ++){
			quotient = operand1.substring(32);
			remainder = operand1.substring(0, 32);
			flagRemainder = remainder.charAt(0);

			if(flagRemainder!= flagDivisor){
				//���ӷ�
				if(enough){
					remainder = add(remainder, operand2);
				}else
					remainder = sub(remainder, operand2);

				if(remainder.charAt(0) == flagRemainder){   //�������remainder�ķ��ź�ԭ���ķ���һ���Ļ�,˵����
					if(i != 32){  //���һ�β�Ҫ�ƶ�
						operand1= shlOne(remainder + quotient);
						enough = true;
					}else
						quotient = shlOne32(quotient);
				}else{     //�������remainder�ķ��ź�ԭ���ķ��Ų�һ���Ļ���˵������
					if(i != 32){
						operand1 = shlZero(remainder + quotient);
						enough = false;
					}else
						quotient = shlZero32(quotient);
				}
			}else {
				//������ͬ��ʱ��������
				if(enough){
					remainder = sub(remainder, operand2);
				}else
					remainder = add(remainder, operand2);

				if(remainder.charAt(0) == flagRemainder){   //�������remainder�ķ��ź�ԭ���ķ���һ���Ļ�,˵����
					if(i != 32){  //���һ�β�Ҫ�ƶ�
						operand1= shlOne(remainder + quotient);
						enough = true;
					}else
						quotient = shlOne32(quotient);
				}else{     //�������remainder�ķ��ź�ԭ���ķ��Ų�һ���Ļ���˵������
					if(i != 32){
						operand1 = shlZero(remainder + quotient);
						enough = false;
					}else
						quotient = shlZero32(quotient);
				}
			}
		}

		//���һ�ηֱ�����������
		//���remainder��dividend���Ų�ͬ
		//quotient
		//��ȡ������������һλ
		if(flagDividend != flagDivisor)
			quotient = add(quotient, "00000000000000000000000000000001");

		//remainder
		if(remainder.charAt(0) != flagDividend){
			if(flagDividend == flagDivisor)
				remainder = add(remainder, operand2);
			else
				remainder = sub(remainder, operand2);
		}
        return overflow + quotient + remainder;

 */

    }
    //shr�����ƣ�shl������
    public String shrOne (String dest){
    	return "1" + dest.substring(0, 64);
	}
	public String shrZero(String dest){
		return "0" + dest.substring(0, 64);
	}
	public String sarZero(String dest){
    	return dest.charAt(0) + dest.substring(0, 64);
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
	public String add(String src, String dest) {
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
	public String sub(String src, String dest){
    	//src-dest
    	return add(src, toNegative(dest));
	}
}
