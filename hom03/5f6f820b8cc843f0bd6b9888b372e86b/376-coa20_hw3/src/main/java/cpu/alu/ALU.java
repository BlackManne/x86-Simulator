package cpu.alu;

import transformer.Transformer;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 * TODO: 取模、逻辑/算术/循环左右移
 */
public class ALU {
    Transformer transformer = new Transformer();
    // 模拟寄存器中的进位标志位
    private String CF = "0";

    // 模拟寄存器中的溢出标志位
    private String OF = "0";

    //signed integer mod
    //dest是被除数，src是除数
    String imod(String src, String dest) {
        // TODO
        //注意在运算中的0，被除数除数都是0；被除数是0，除数不是0；被除数不是0，除数是0
        if(transformer.binaryToInt(dest).equals("0") && !(transformer.binaryToInt(src).equals("0"))){
            //被除数是0，除数不是0
            return "0";
        }
        String temp = dest;
        //方法的本质在于，不断让dest逼近0，如果两个符号不一样，就是加，如果符号一样就是减
        //temp用来保存每一次做减法之前的被除数
        //dest用来保存每一次做减法之后的被除数
        //如果dest和temp不一样，说明最后一次的src多减了，也就是说应该要的数是temp
        while(dest.charAt(0) == temp.charAt(0)){
            temp = dest;
            if(src.charAt(0) != temp.charAt(0)){
                dest = add(src, temp);
            }else
                dest = sub(src, temp);
        }
		return temp;
    }

    String shl(String src, String dest) {
        // TODO
        int numOf0 = Integer.parseInt(transformer.binaryToInt(src));
        //超过32位时，移动位数为位数%32
        if(numOf0 >= 32)
            numOf0 = numOf0 % 32;
        //左移src位相当于截取从src下标开始的substring
        String res = dest.substring(numOf0);
        for(int i = 1; i <= numOf0; i ++){
            res = res.concat("0");
        }
        return res;
    }

    String shr(String src, String dest) {
        // TODO
        int numOf0 = Integer.parseInt(transformer.binaryToInt(src));
        if(numOf0 >= 32)
            numOf0 = numOf0 % 32;
        String res = dest.substring(0, 32 - numOf0);
        String temp = "";
        for(int i = 1; i <= numOf0; i ++){
            temp = temp.concat("0");
        }
        res = temp.concat(res);
        return res;

    }

    String sal(String src, String dest) {
        // TODO
		return shl(src, dest);
    }

    //算数左右移考虑符号，逻辑左右移不考虑符号（全部补0）
    String sar(String src, String dest) {
        // TODO
        int flag = 0;  //假设符号位0，也就是默认为正数
        if(dest.charAt(0) == '1')
            flag = 1;  //如果第一位符号位是1的话，flag是1，代表是负数
        int numOfFlag = Integer.parseInt(transformer.binaryToInt(src));
        if(numOfFlag >= 32)
            numOfFlag = numOfFlag % 32;
        String res = dest.substring(0, 32 - numOfFlag);
        String temp = "";
        if(flag == 1) {
            for (int i = 1; i <= numOfFlag; i++) {
                temp = temp.concat("1");
            }
        }else{   //如果flag是负数的话
            for (int i = 1; i <= numOfFlag; i++) {
                temp = temp.concat("0");
            }
        }
        res = temp.concat(res);
        return res;

    }

    String rol(String src, String dest) {
        // TODO
        //循环左移
//        char[] res = new char[32];
//        int bitsToShift = Integer.parseInt(transformer.binaryToInt(src));
//        if(bitsToShift >= 32)
//            bitsToShift = bitsToShift % 32;
//        int newIndex;
//        char currentChar;
//        for(int i = 0; i <= 31; i ++){
//            newIndex = (i + 32 - bitsToShift) % 32;
//            currentChar = dest.charAt(i);
//            res[newIndex] = currentChar;
//        }
//        String ret = "";
//        for(int i = 0; i < 32; i ++){
//            ret = ret.concat(String.valueOf(res[i]));
//        }
//		return ret;
        //循环移动的位数
        int bits = Integer.parseInt(transformer.binaryToInt(src)) % 32;
        return dest.substring(bits) + dest.substring(0, bits);
    }

    String ror(String src, String dest) {
        // TODO
//        char[] res = new char[32];
//        int bitsToShift = Integer.parseInt(transformer.binaryToInt(src));
//        if(bitsToShift >= 32)
//            bitsToShift = bitsToShift % 32;
//        int newIndex;
//        char currentChar;
//        for(int i = 0; i <= 31; i ++){
//            newIndex = (i + bitsToShift) % 32;
//            currentChar = dest.charAt(i);
//            res[newIndex] = currentChar;
//        }
//        String ret = "";
//        for(int i = 0; i < 32; i ++){
//            ret = ret.concat(String.valueOf(res[i]));
//        }
//        return ret;
        int bits = Integer.parseInt(transformer.binaryToInt(src)) % 32;
        return dest.substring(32 - bits) + dest.substring(0, 32 -bits);    //后面的在前面
    }

    /*String add(String src, String dest) {
        // TODO
        char[] result = new char[32];
        char srcC;
        char destC;
        for(int i = 31; i >= 0; i --){
            srcC = src.charAt(i);
            destC = dest.charAt(i);
            if(srcC == '1' && destC == '1'){  //如果两个数这个位都是1，产生进位，CF的值不产生变化
                result[i] = CF.charAt(0);
                CF = "1";
            }else if(srcC == '0' && destC == '0'){  //如果两个数这个位都是0，不产生进位，这一位的值就是CF的值，但是CF要被置为0
                result[i] = CF.charAt(0);
                CF = "0";
            }else{    //srcC和destC一个是0一个是1的情况
                if(CF.equals("1")){   //如果进位是1的话，
                    result[i] = '0';
                    CF = "1";
                }else{
                    result[i] = '1';
                    CF = "0";
                }
            }
        }
        String res = "";
        for(int i = 0; i < 32; i ++){
            res = res.concat(String.valueOf(result[i]));
        }
        return res;
    }

     */

    // dest - src
    String sub(String src, String dest) {
        // TODO
        //dest是被减数，src是减数
        String newSrc = "";
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

    /*private String toNegative(String code) {
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

     */

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


}
