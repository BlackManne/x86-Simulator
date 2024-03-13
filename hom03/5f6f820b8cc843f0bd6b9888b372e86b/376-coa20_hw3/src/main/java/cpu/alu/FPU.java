package cpu.alu;

import transformer.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用4位保护位进行计算，计算完毕直接舍去保护位
 * TODO: 浮点数运算
 */


//FPU加减法
    //1.首先判断有没有NaN，a和b都要判断；然后判断有没有0和无穷，分别返回a和b
    //2.整体逻辑：加法处理符号相同的加法，减法处理符号相同的减法，符号不同的减法给加法，提取符号；符号不同的加法给减法，提取符号
    //3.如果两个数初始时是非规格化的，那么把指数+1分数补0变成规格化数    非规格化必须最先判断并且最先处理！！！！！！！！！
    //加法：
        //1.判断指数，进行移位对齐（移位永远都是小数对齐大数），如果移位完了小数变成了0，说明太小了，直接返回大数
        //2.对齐完指数以后直接做加法，因为1.xx+1.xx不可能超过4，最多超过2，所以如果加法产生了进位就右移一位，指数位++，没有的话直接返回（处理小数部分）
        //3.如果指数超过了255，那么就是无穷，返回无穷（处理指数部分）
    //减法：
        //0.减法比较复杂，如果要处理减法的话，为了避免b的溢出，要首先将b右移一位，然后b的指数加1
        //1.判断指数，进行移位对齐，如果移位完了小数变成了0，根据小数是被减数还是减数判断是否改变符号，然后返回指定的结果
        //2.对齐完指数以后对b取反，做加法，如果加法完了的结果有了进位，说明借掉的2可以减掉，直接得到结果，如果加法完了的结果没有进位，那么就把最终的结果取反，然后改变符号
        //3.因为减法可能会产生非规格化数，所以要注意进行规格化
        //4.b的指数+1了，怎么处理？
        //处理非规格化数的本质是这个小数部分比1要小，而b右移了之后小数部分比1要小，b指数大的时候，减法得到结果可能也包括在比1小的范围内，所以可以用规格话解决？



public class FPU {
    /**
     * compute the float add of (a + b)
     **/
    int c = 0;
    Transformer transformer = new Transformer();
    public String add(String a, String b){
        // TODO
        c = 0;

        //1.判断有没有NaN
        if(transformer.binaryToFloat(a).equals("NaN"))
            return a;
        if(transformer.binaryToFloat(b).equals("NaN"))
            return b;
        //2.特别判断加法中有没有0
        if(a.equals("00000000000000000000000000000000"))
            return b;
		if(b.equals("00000000000000000000000000000000"))
		    return a;
		//3.特别判断加法中有没有无穷
        if(a.substring(1).equals("1111111100000000000000000000000"))
            return a;
        if(b.substring(1).equals("1111111100000000000000000000000"))
            return b;

       if(a.length() == 32) {
           a = a.concat("0000");
           b = b.concat("0000");
       }

        if(a.charAt(0) != b.charAt(0)){   //如果符号不同的话，选择使用减法
            b = '1' - b.charAt(0) + b.substring(1);
            return sub(a, b);
        }

        //4.解析exponent
        int exponentA = Integer.parseInt(transformer.binaryToInt(a.substring(1,9)));
        int exponentB = Integer.parseInt(transformer.binaryToInt(b.substring(1,9)));
        String SigA = "1".concat(a.substring(9));
        String SigB = "1".concat(b.substring(9));
        String resSig;    //加法之后的新指数
        int exponentDiffer = exponentA - exponentB;
        String differInBits = null;
        //(1)考虑两个数指数相同的情况
        if(exponentDiffer == 0){   //如果指数位完全相同的话，significand进行加法
            resSig = bitsadd(SigA, SigB, 28);
            if(c > 0){   //如果加法过程中产生进位，加减法如果有进位最多也只有一位进位，进到“2”那一位，因此分数部分只用做一次移位就可以，
                //指数位+1，分数位右移
                String newExp = bitsadd(transformer.myIntToBinary(String.valueOf(exponentA)), "00000001", 8);
                if(newExp.equals("11111111"))
                    return a.charAt(0) + "1111111100000000000000000000000";
                resSig = shr(resSig,1);
                return a.charAt(0) + newExp + resSig.substring(1, 24);

            }
            //没有产生进位直接返回
            return a.charAt(0)+ transformer.myIntToBinary(String.valueOf(exponentA)) + resSig.substring(1, 24);
        }
        //(2)如果B的指数位比A要大，a的指数位右移，始终是指数位小的那个分数位进行移动
        if(exponentDiffer < 0) {
            String newSigA = shr(SigA, -exponentDiffer);
            if(newSigA.equals("0000000000000000000000000000")) //说明a的量级比b小太多了
                return b.substring(0, 32);
            resSig = bitsadd(newSigA, SigB, 28);
            if(c > 0){   //如果加法过程中产生进位
                //指数位+1，分数位右移
                String newExp = bitsadd(transformer.myIntToBinary(String.valueOf(exponentB)), "00000001", 8);
                resSig = shr(resSig,1);
                return a.charAt(0) + newExp + resSig.substring(1, 24);
            }
            //没有产生进位直接返回
            //resSig = resSig.substring(1);
            return a.charAt(0)+ transformer.myIntToBinary(String.valueOf(exponentB)) + resSig.substring(1, 24);
        }
        //(3)如果A的指数位比B要大
        String newSigB = shr(SigB, exponentDiffer);
        if(newSigB.equals("0000000000000000000000000000")) //说明b的量级比a小太多了
            return a.substring(0, 32);
        resSig = bitsadd(newSigB, SigA, 28);
        if(c > 0){   //如果加法过程中产生进位
            //指数位+1，分数位右移
            String newExp = bitsadd(transformer.myIntToBinary(String.valueOf(exponentA)), "00000001", 8);

            resSig = shr(resSig,1);
            return a.charAt(0) + newExp + resSig.substring(1, 24);
        }
        //没有产生进位直接返回
        //resSig = resSig.substring(1);
        return a.charAt(0)+ transformer.myIntToBinary(String.valueOf(exponentA))+ resSig.substring(1, 24);

    }

    /**
     * compute the float add of (a - b)
     **/
    public String sub(String a, String b){
        // TODO
        c = 0;
        char flag = a.charAt(0);   //提取a的符号
        //1.特别判断减法中有没有0
        if(a.equals("000000000000000000000000000000000000"))
            return '1' - b.charAt(0) + b.substring(1);
        if(b.equals("000000000000000000000000000000000000"))
            return a;

        //2.如果符号不同，选择加法，符号和被减数相同
        //如果没有被扩展过，先对a和b扩展成32位
        if(a.length() == 32) {
            a = a.concat("0000");
            b = b.concat("0000");
        }
        if(a.charAt(0) != b.charAt(0)){
            b = '1' - b.charAt(0) + b.substring(1);
            return add(a, b);
        }

        int exponentA = Integer.parseInt(transformer.binaryToInt(a.substring(1,9)));
        int exponentB = Integer.parseInt(transformer.binaryToInt(b.substring(1,9)));
        int resexponent;
        String SigA = "1".concat(a.substring(9));
        String SigB = "1".concat(b.substring(9));
//        if(SigB.equals("1000000000000000000000000000")){
//            exponentB += 1;
//            SigB = shr(SigB, 1);
//            c2 = '1';
//        }
        //进行减法之前将B移动一位
        exponentB += 1;
        SigB = shr(SigB, 1);
        String resSig;    //加法之后的新指数

        int exponentDiffer = exponentA - exponentB;
        //(1)考虑两个数指数相同的情况
        resexponent = exponentA;
        if(exponentDiffer == 0){   //如果指数位完全相同的话，significand进行加法
            SigB= toNegative(SigB, 28);
            resSig = bitsadd(SigA, SigB, 28);
            //如果产生了0那么直接返回0
            if(resSig.substring(0, 24).equals("000000000000000000000000"))
                return "00000000000000000000000000000000";
//            if(c2 == '1'){  //如果进到了4这一位
//                resSig = bitssub(resSig, "0100000000000000000000000000", 28);  //减掉一个2
//                exponentA -= 1;
//            }
//            else {
                if (c > 0) {   //如果加法过程中产生进位,说明在2位上有一个2，这样的话直接减二，符号无变化
//                    return flag + transformer.myIntToBinary(String.valueOf(exponentA)) + resSig.substring(1, 24);
                } else {     //没有产生进位
                    resSig = toNegative(resSig, 28);
                    if (flag == '0')
                        flag = '1';
                    else
                        flag = '0';
                }
//            }
//            return flag + transformer.myIntToBinary(String.valueOf(exponentA)) + resSig.substring(1, 24);
        }
        //(2)如果B的指数位比A要大，a的指数位右移
        else if(exponentDiffer < 0) {
            SigB= toNegative(SigB, 28);
            String newSigA = shr(SigA, -exponentDiffer);
            if(newSigA.equals("0000000000000000000000000000")) //说明a的量级比b小太多了,返回b的相反数
                return '1' - b.charAt(0) + b.substring(1, 32);
            resSig = bitsadd(newSigA, SigB, 28);
            //如果产生了0那么直接返回0
            if(resSig.substring(0, 24).equals("000000000000000000000000"))
                return "00000000000000000000000000000000";
            if(c > 0){   //如果加法过程中产生进位
//                return flag + transformer.myIntToBinary(String.valueOf(exponentB)) + resSig.substring(1, 24);
            }
            else{   //没有产生进位
                resSig = toNegative(resSig, 28);
                if(flag == '0')
                    flag = '1';
                else
                    flag = '0';
//                return flag + transformer.myIntToBinary(String.valueOf(exponentB)) + resSig.substring(1, 24);
            }
            //B最开始移动了1位，所以要把移动的1位减回去
//            exponentB -= 1;
//            resSig = shl(resSig, 1);
            resexponent = exponentB;
        }
        else{
            //(3)如果A的指数位比B要大
            String newSigB = shr(SigB, exponentDiffer);
            if(newSigB.equals("0000000000000000000000000000")) //说明b的量级比a小太多了
                return a.substring(0, 32);
            newSigB = toNegative(newSigB, 28);
            resSig = bitsadd(newSigB, SigA, 28);
            if(resSig.substring(0, 24).equals("000000000000000000000000"))
                return "00000000000000000000000000000000";
            if(c > 0){   //如果加法过程中产生进位
            /*if(resSig.charAt(0) == '0'){   //如果第一位是0的话说明是非规格化数
                int bit = findFirst(resSig);
                resSig = shl(resSig, bit);
                exponentA -= bit;
            }

             */
//            return flag + transformer.myIntToBinary(String.valueOf(exponentA)) + resSig.substring(1, 24);
            }
            else{     //没有产生进位
                resSig = toNegative(resSig, 28);
                if(flag == '0')
                    flag = '1';
                else
                    flag = '0';

//            return flag + transformer.myIntToBinary(String.valueOf(exponentA)) + resSig.substring(1, 24);
            }
        }
        //检查一下是否是规格化的
        int index = resSig.indexOf("1");
        if(index != 0){
            resexponent -= index;
            resSig = shl(resSig, index);
        }
        return flag + transformer.myIntToBinary(String.valueOf(resexponent)) + resSig.substring(1, 24);
    }

    String toNegative(String code, int length) {
        char[] ans = code.toCharArray();
        for (int i = 0; i < length; ++i) {
            if (ans[i] == '0') ans[i] = '1';
            else ans[i] = '0';
        }
        ans[length - 1]++;
        for (int i = length - 1; i > 0; --i) {
            if (ans[i] == '2') {
                ans[i - 1]++;
                ans[i] = '0';
            }
        }
        if (ans[0] == '2') ans[0] = '0';
        return String.valueOf(ans);
    }

    //add two integer
    String bitsadd(String src, String dest, int length) {
        StringBuilder ans = new StringBuilder();
        c = 0;
        for (int i = length - 1; i >= 0; --i) {
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

    //dest是被减数，src是减数
    String bitssub(String dest, String src, int length){
        return bitsadd(dest, toNegative(src,length), length);
    }

    //算术右移指定的位数
    String shr(String dest, int src){
        if(src >= dest.length())
            src = dest.length();
        String res = dest.substring(0, dest.length() - src);
        String temp = "";
        for(int i = 1; i <= src; i ++){
            temp = temp.concat("0");
        }
        res = temp.concat(res);
        return res;
    }

    //算术左移指定的位数
    String shl(String dest, int src){
        if(src >= dest.length())
            src = dest.length();
        String res = dest.substring(src);
        String temp = "";
        for(int i = 1; i <= src; i ++){
            res = res.concat("0");
        }
        return res;
    }

    int findFirst(String str){
        int length = str.length();
        for(int i = 0; i < length; i ++){
            if(str.charAt(i) == '1')
                //i是下标，要找的是挪动的位数
                return i;
        }
        return -1;
    }



}
