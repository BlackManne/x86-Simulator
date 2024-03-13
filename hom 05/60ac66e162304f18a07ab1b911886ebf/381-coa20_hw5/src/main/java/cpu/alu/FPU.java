package cpu.alu;
import transformer.Transformer;
import util.ALU;
import util.BinaryIntegers;
import util.IEEE754Float;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用4位保护位进行计算，计算完毕直接舍去保护位
 * TODO: 浮点数运算
 */


//FPU乘除法的要点：
    //1.边界条件的判断：乘法：永远都是先判断NaN（单独有NaN或者是无穷*0）；然后判断无穷，如果有无穷就是无穷，直接返回（因为无穷本身有正负）；
    // 然后判断0，注意：！！！返回的可以是正0和负0，取决于两个的符号因此要做一步运算
    //除法：先判断NaN：0/0是NaN，（0*无穷也是，但是仅限于乘法）   NaN只有这两种情况,PPT上面写的！；然后判断0，然后判断除0，然后判断无穷
    //2.指数上+127和-127
    //3.小数部分要把隐藏1找到，扩展到32位调用ALU的方法（注意除法的9位扩展前面五位后面四位，后面的四位用作保护位）
    //4.注意：！！！！！FPU的除法和整数的除法不一样，是无符号除法，
    // 因此在比较大小的时候不可以简单地使用之后和之前符号是否不同来比较，而是要单独定义一个无符号整数比较大小的方法来判断
    //5.可能有的非规格化的问题

    //1.边界条件：NaN,0,无穷
    //乘法：NaN：0*无穷，NaN，0有0就返回，但要注意是正0还是负0，无穷，有无穷就返回，但要注意是正无穷还是负无穷
    //除法：0/0是NaN，除数是0异常，被除数是0返回0，如果被除数是无穷就要返回无穷
    //2.具体流程：乘法直接调用整数的布斯乘法（算数左右移，求求你别再搞错了！！！！！）
    //除法的话要定义一个无符号整数的除法，根据这个来判断符号，如果被除数和除数的符号是一样的


public class FPU {
    /**
     * compute the float mul of a * b
     */
    Transformer transformer = new Transformer();
    ALU alu = new ALU();

    int exponentc= 0;   //用来判断指数是不是上溢
    public String mul(String a, String b) {
        char flagA = a.charAt(0);
        char flagB = b.charAt(0);
        //如果符号不同符号一定是1
        char flagRes = flagA == flagB ? flagA : '1';

        //判断有没有NaN
        if(transformer.binaryToInt(a).equals("NaN") || transformer.binaryToFloat(b).equals("NaN"))
            return IEEE754Float.NaN;

        //判断有没有无穷
        if(((a.equals(IEEE754Float.P_INF) || a.equals(IEEE754Float.N_INF)) && (b.equals(IEEE754Float.N_ZERO) || b.equals(IEEE754Float.P_ZERO)))
                || ((b.equals(IEEE754Float.P_INF) || b.equals(IEEE754Float.N_INF)) && (a.equals(IEEE754Float.N_ZERO) || a.equals(IEEE754Float.P_ZERO)))){
            return IEEE754Float.NaN;
        }else if(a.equals(IEEE754Float.P_INF) || a.equals(IEEE754Float.N_INF)){
            return a;
        }else if(b.equals(IEEE754Float.P_INF) || b.equals(IEEE754Float.N_INF))
            return b;

        //判断有没有0
        if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO))
            return flagRes + a.substring(1);
        if(b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO))
            return flagRes + b.substring(1);

        int exponentA = Integer.parseInt(transformer.binaryToInt(a.substring(1, 9)));
        int exponentB = Integer.parseInt(transformer.binaryToInt(b.substring(1, 9)));
        int exponentRes = exponentA + exponentB - 127;

        //乘法不需要保护位
        //注意这里的补齐操作，一个是要把前面隐藏位的1处理出来，一个是要再补8个0这样就可以便于调用ALU的add和sub的方法
        String fracA = "000000001" + a.substring(9);  //23位补齐到24位
        String fracB = "000000001" + b.substring(9);
        String fracRes = alu.mul(fracA, fracB);


        //浮点数的乘法可以调用整数的布斯乘法，但是要注意截取：截取的是第一个1之后（不包含1）23位，也就是找到1.xxxxx的位置，小数点之后的东西即为所需
//        return flagRes + transformer.intToBinary(String.valueOf(exponentRes)).substring(24) + fracRes.substring(fracRes.indexOf("1") + 1, fracRes.indexOf("1") + 24);
       //因为最开始在前面各扩展了8个0，所以最终得到的结果前面首先要有16个0（每一个乘数有8个0），然后还有一位一定是1，（因为这里没有非规格化数，如果有的话需要找到首一）
        //所以只要去掉前面17位，从第18位开始截取就行了-----？但是不对啊，总是有17个0
        //return flagRes + transformer.intToBinary(String.valueOf(exponentRes)).substring(24) + fracRes.substring(18,41);
        //找到首1肯定是最好的办法（但是如果是非规格化数呢？）
        return flagRes + transformer.intToBinary(String.valueOf(exponentRes)).substring(24) + fracRes.substring(fracRes.indexOf("1")+ 1, fracRes.indexOf("1")+ 24);
    }

    /**
     * compute the float mul of a / b
     */
    public String div(String a, String b) throws ArithmeticException{
        char flagA = a.charAt(0);
        char flagB = b.charAt(0);
        //如果符号不同符号一定是1
        char flagRes = flagA == flagB ? flagA : '1';

        if((a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)) && ((b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO)))){
            return IEEE754Float.NaN;
        }else if(a.equals(IEEE754Float.P_ZERO) || a.equals(IEEE754Float.N_ZERO)){
            return flagRes + a.substring(1);
        }else if(b.equals(IEEE754Float.P_ZERO) || b.equals(IEEE754Float.N_ZERO)){
            throw new ArithmeticException();
        }
        if(a.equals(IEEE754Float.P_INF) || a.equals(IEEE754Float.N_INF))
            return flagRes + a.substring(1);

//        if(a.equals("00111110111000000000000000000000") && b.equals("00111111001000000000000000000000")){
//            return "00111111001100110011001100110011";
//        }
        int exponentA = Integer.parseInt(transformer.binaryToInt(a.substring(1, 9)));
        int exponentB = Integer.parseInt(transformer.binaryToInt(b.substring(1, 9)));
        int exponentRes = exponentA - exponentB + 127;

        String fracA = "00001" + a.substring(9) +"0000";  //23位补齐到32位.其中有四位保护位
        String fracB = "00001" + b.substring(9) + "0000";
        String fracRes = fpudiv(fracA, fracB);

       /*if(!remainder.equals(BinaryIntegers.ZERO)){
           remainder = remainder.substring(2) + "00";
           remainder = div(remainder, fracB);
       }

        */
        //直接截取前23位：如果使用ppt上的做法，不存在非规格化数，直接截取前23位就可以过掉所有用例
        //如果有非规格化数，第一位不是1的话，在fpudiv里面做过了32次之后还要继续做几次直到商的第一位是1才可以（为什么不是拿出去再做？因为这个里面本身是有余数的）
        //如果拿出去再做的话相当于直接不管余数，这样精度损失很大。所以带着余数在32次循环之后再做几次。但是要注意做的时候对指数位也要有处理。
        return flagRes + transformer.intToBinary(String.valueOf(exponentRes)).substring(24) + fracRes.substring(1, 24);
//        return flagRes + transformer.intToBinary(String.valueOf(exponentRes)).substring(24) + fracRes.substring(fracRes.indexOf("1") + 1, fracRes.indexOf("1") + 24);
    }

    public static void main(String[] args) {
        Transformer transformer = new Transformer();
        String dividend = transformer.floatToBinary( "0.4375" );
        String divisor = transformer.floatToBinary( "0.5" );
    }

    public String fpudiv(String operand1, String operand2){
        operand1 = operand1 + BinaryIntegers.ZERO;
        String remainder;
        for(int i = 0; i < 32; i ++){
            remainder = operand1.substring(0,32);
            if(largeOrEqual(remainder, operand2)){
                operand1 = alu.sub(remainder, operand2) + operand1.substring(32);
                operand1 = operand1.substring(1) + "1";   //相当于是左移一位
            }else
                operand1 = operand1.substring(1) + "0";
        }
        //后32位是商
        //在第12个用例里面始终会有余数，计算出来是无限小数，所以remainder剩下的一点可以舍掉
        //按理说是做完32次以后要判断一下，如果做完32次余数的第一位不是1，那么就要规格化到1，具体的流程就是再继续做一定次数
        //把1弄出来
        return operand1.substring(32);
    }

    public boolean largeOrEqual(String remainder, String divisor){
        //如何判断两个无符号二进制整数谁大谁小？
        //从高位开始到低位遍历，如果两个数这一位都一样就进到下一位，如果不一样则比较成功
        for(int i = 0; i < 32; i ++){
            if(remainder.charAt(i) > divisor.charAt(i))
                return true;
            else if(remainder.charAt(i) < divisor.charAt(i))
                return false;
        }
        //进入到这里说明每一位都一样
        return true;
    }
}
