package cpu.alu;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 * TODO: 加减与逻辑运算
 */
public class ALU {

    // 模拟寄存器中的进位标志位
    private String CF = "0";

    // 模拟寄存器中的溢出标志位
    private String OF = "0";

    //add two integer
//    String add(String src, String dest) {
//        // TODO
//        char[] result = new char[32];
//        char srcC;
//        char destC;
//        for(int i = 31; i >= 0; i --){
//            srcC = src.charAt(i);
//            destC = dest.charAt(i);
//            if(srcC == '1' && destC == '1'){  //如果两个数这个位都是1，产生进位，CF的值不产生变化
//                result[i] = CF.charAt(0);
//                CF = "1";
//            }else if(srcC == '0' && destC == '0'){  //如果两个数这个位都是0，不产生进位，这一位的值就是CF的值，但是CF要被置为0
//                result[i] = CF.charAt(0);
//                CF = "0";
//            }else{    //srcC和destC一个是0一个是1的情况
//                if(Integer.parseInt(CF) == 1){   //如果进位是1的话，
//                    result[i] = '0';
//                }else{
//                    result[i] = '1';
//                }
//            }
//        }
//        String res = "";
//        for(int i = 0; i < 32; i ++){
//            res = res.concat(String.valueOf(result[i]));
//        }
//        return res;
//    }

    public String add(String src, String dest){
        String ans = "";
        char currX, currY;
        for(int i = 31; i >= 0; i --){
            currX = src.charAt(i);
            currY = dest.charAt(i);
            ans = String.valueOf((currX - '0') ^ (currY - '0') ^ Integer.parseInt(CF)) + ans;
            CF = String.valueOf(((currX - '0') & (currY - '0')) | ((currX - '0') & CF.charAt(0)) | ((currY - '0') & CF.charAt(0)));
        }
        return ans;
    }

    //sub two integer
    // dest - src
    public String sub(String src, String dest) {
        // TODO
        //dest是被减数，src是减数
        String newSrc = "";
        //按位取反就是找到最后一个为1的数，然后这个1之前的所有数按位取反，包括这个1以及之后的不变
        for(int i = 0; i < src.lastIndexOf("1"); i ++){
            if(src.charAt(i) == '1')//按位取反
                newSrc = newSrc.concat("0");
            else
                newSrc = newSrc.concat("1");
        }
        for(int j = src.lastIndexOf("1"); j <= 31; j ++){
            newSrc = newSrc.concat(String.valueOf(src.charAt(j)));
        }
		return add(dest,newSrc);
	}


	public String toNegation(String a){
        char[] ans = a.toCharArray();
        for(int i = 0; i < 32; i ++){
            if(ans[i] == '1') ans[i] = '0';
            else ans[i] = '1';
        }//按位取反
        //每一位+1
        //因为char是用ascii码，所以可以直接在这一位上+1，等于char+'1'
        ans[31] ++;
        for(int i = 31; i >= 1; i --){
            if(ans[i] == '2'){
                ans[i] = '0';
                ans[i - 1]++;
            }
        }
        if(ans[0] == '2')
            ans[0] = '0';

        return ans.toString();
    }

    public String and(String src, String dest) {
        // TODO
        char[] result = new char[32];
        for(int i = 0; i < 32; i ++)
        {
           if(src.charAt(i) == '1' && dest.charAt(i) == '1'){
               result[i] = '1';
           }
           else
               result[i] = '0';
        }
        String res = "";
        for(int i = 0; i < 32; i ++){
            res = res.concat(String.valueOf(result[i]));
        }
		return res;
    }

    public String or(String src, String dest) {
        // TODO
        char[] result = new char[32];
        for(int i = 0; i < 32; i ++){
            if(src.charAt(i) == '1' || dest.charAt(i) == '1'){
                result[i] = '1';
            }
            else
                result[i] = '0';
        }
        String res = "";
        for(int i = 0; i < 32; i ++){
            res = res.concat(String.valueOf(result[i]));
        }
		return res;
    }

    public String xor(String src, String dest) {
        // TODO
        char[] result = new char[32];
        for(int i = 0; i < 32; i ++){
            if(src.charAt(i) == dest.charAt(i))
                result[i] = '0';
            else
                result[i] = '1';
        }
        String res = "";
        for(int i = 0; i < 32; i ++){
            res = res.concat(String.valueOf(result[i]));
        }
        return res;
    }

}