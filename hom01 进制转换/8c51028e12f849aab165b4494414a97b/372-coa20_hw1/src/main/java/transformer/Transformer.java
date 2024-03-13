package transformer;


import java.lang.reflect.Array;
import java.util.Arrays;

//
public class Transformer {
    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        //TODO:
        int[] binary = new int[32];
        //字符数组初始化为0
        for(int j = 0; j < 32; j ++){
            binary[j] = 0;
        }
        boolean flag = false;    //如果是正数，flag = false， 如果是负数 flag=true
        int i =  Integer.parseInt(numStr);
        if(i < 0)
            flag = true;

        int j;
        //如果是正数的话
        if(i >= 0) {
            for (j = 31; j > 0; j--) {
                if (i == 0)
                    break;
                binary[j] = (i % 2);
                i = i / 2;
            }
        }else{
            //如果是负数，采用补码表示法取反加一
            i = Math.abs(i) - 1;
            //然后按照正数处理
            for (j = 31; j > 0; j--) {
                if (i == 0)
                    break;
                binary[j] = (i % 2);
                i = i / 2;
            }
            //处理完了以后全部取反
            for(j = 0; j < 32; j ++){
                binary[j] = 1 - binary[j];  //取反，表示成负数
            }
        }

        String binaryString = "";
        for(int k = 0; k < 32 ; k ++){
            binaryString = binaryString.concat(String.valueOf(binary[k]));
        }
        return binaryString;
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
        char[] binChar = binStr.toCharArray();
        boolean flag = false;      //如果是正数就是false；如果是负数就是true
        char judgeChar = binChar[0];
        int i;    //i用来寻找第一个不是符号位的数位
        int result = 0;
        //首先考虑正数
        if(judgeChar == '0') {
            for (i = 0; i < 32; i++) {
                if (binChar[i] == '1')
                    //找到第一个为1的数
                    break;
            }
            int count = 1;   //用来计算应该×2的几次方
            for(int k = 31; k >= i; k --){
                result += Integer.parseInt(String.valueOf(binChar[k])) * count;
                count *= 2;
            }
        }
        //然后考虑负数
        if(judgeChar == '1'){
            flag = true;
            //负数考虑取反加一
            //先做取反
            for(int k = 0; k < 32; k ++){
                if(binChar[k] == '1')
                    binChar[k] = '0';
                else if(binChar[k] == '0')
                    binChar[k] = '1';
            }
            //然后考虑把取反的数值算出来，再+1
            for (i = 0; i < 32; i++) {
                if (binChar[i] == '1')
                    //找到第一个为1的数
                    break;
            }
            int count = 1;   //用来计算应该×2的几次方
            for(int k = 31; k >= i; k --){
                result += Integer.parseInt(String.valueOf(binChar[k])) * count;
                count *= 2;
            }
            result += 1;
        }

        String intString = Integer.toString(result);
        if(flag)
            intString = "-".concat(intString);
        return intString;
    }

    /**
     * Float true value to binaryString
     * @param floatStr : The string of the float true value
     * */
    public String floatToBinary(String floatStr) {
        //TODO:
        //先创建一个32位字符数组
        char[] charStr = new char[32];
        for(int i = 0; i < 32; i ++){
            charStr[i] = '0';
        }

        //首先判断是不是无穷
        int indexOfE = floatStr.indexOf("E");
        if(indexOfE != -1) {
            String exponent = floatStr.substring(indexOfE + 1);
            if(Integer.parseInt(exponent) >= 255 && floatStr.charAt(0) == '-')
                return "-Inf";
            else if(Integer.parseInt(exponent) >= 255 && floatStr.charAt(0) != '-'){
                return "+Inf";
            }
        }

        double inputFloat = Double.parseDouble(floatStr);
        //为什么要用double，因为可能会有非规格化数的存在，使用float的话可能会数值不够大，double的位数多，可以保证精度
        //判断正负
        //boolean flag = false;   //false默认为负数
        if(inputFloat < 0){
            //flag = true;       //如果第一位是1说明是负数
            charStr[0] = '1';
        }else
            charStr[0] = '0';

        //浮点数取绝对值便于后面处理指数和分数
        //Math.abs可以直接用这种含有E的表示法把一个字符串变成一个double
        inputFloat = Math.abs(inputFloat);


        int exponent = 0;
        //判断浮点数是否比1大
        if(inputFloat > 1){
            double i = inputFloat;
            while(i >= 2){  //如果比2小说明是规格化数
                i /= 2;
                exponent += 1;
            }
            //置正常的指数
            exponent += 127;
            //获得分数部分
            double fraction = i - 1.0;

            //处理指数位
            int expStd = 128;
            for(int k = 1; k <= 8; k ++){
                if(exponent == 0)
                    break;
                if(exponent >= expStd){
                    charStr[k] = '1';
                    exponent -= expStd;
                }
                expStd /= 2;
            }

            //处理分数位
            double fracStd = 0.5;
            for(int k = 9; k < 32; k ++){
                if(fraction == 0)
                    break;
                if(fraction >= fracStd){
                    charStr[k] = '1';
                    fraction -= fracStd;
                }
                fracStd/= 2;
            }
        }else {
            //判断浮点数是否比1小
            //其中又分为两种，规格化数和非规格化数
            //先判断是否是规格化数
            if (floatStr.charAt(0) == '1' //正数，第一位是1，规格化数
                    || floatStr.charAt(0) == '-' && floatStr.charAt(1) == '1') { //负数 第一位是-，第二位是1，规格化数
                double i = inputFloat;
                while (i < 1) { //一直做到比1大
                    i *= 2;
                    exponent -= 1;
                }
                exponent += 127;
                double fraction = i - 1.0;

                //处理指数位
                int expStd = 128;
                for(int k = 1; k <= 8; k ++){
                    if(exponent == 0)
                        break;
                    if(exponent >= expStd){
                        charStr[k] = '1';
                        exponent -= expStd;
                    }
                    expStd /= 2;
                }

                //处理分数位
                double fracStd = 0.5;
                for(int k = 9; k < 32; k ++){
                    if(fraction == 0)
                        break;
                    if(fraction >= fracStd){
                        charStr[k] = '1';
                        fraction -= fracStd;
                    }
                    fracStd/= 2;
                }
            }else{  //如果不满足上面的情况就是非规格化数
                //非规格化数：
                //非规格化数就是太小了，比2-126次方还要小（这是规格化数最小值，指数上面是00000001，分数是1.0。
                //尽管指数是00000000，但是表示的是2-126次方，所以fraction部分必须是0.f。因此在获取实际的分数部分的时候要乘上2的126次方，然后再解析fraction
                double fraction = inputFloat;
                for(int k = 1; k <= 126; k ++){
                    fraction *= 2;
                }

                //处理指数位
                for(int k = 1; k <= 8; k ++){
                    charStr[k] = '0';
                }

                //处理分数位
                double fracStd = 0.5;
                for(int k = 9; k < 32; k ++){
                    if(fraction == 0)
                        break;
                    if(fraction >= fracStd){
                        charStr[k] = '1';
                        fraction -= fracStd;
                    }
                    fracStd/= 2;
                }

            }
        }

        String binary = "";
        for(int k = 0; k < 32; k ++){
            binary = binary.concat(String.valueOf(charStr[k]));
        }

        return binary;
        //return floatStr;
    }

    /**
     * Binary code to its float true value
     * */
    public String binaryToFloat(String binStr) {
        //TODO:
        char[] binChar = binStr.toCharArray();
        boolean flag = false;  //如果是正数就是false，如果是负数就是true
        if(binChar[0] == '1')
            flag = true;   //说明是负数

        //先判断是不是无穷
        boolean isInf = true;   //假设是负数
        for(int k = 1; k <= 8; k ++){
            if(binChar[k] == '0') {
                isInf = false;
                break;
            }
        }
        if(isInf && flag )
            return "-Inf";
        else if(isInf && !flag)
            return "+Inf";

        //再判断是不是0
        boolean isZero = true;   //假设是0
        for(int k = 1; k <= 31; k ++){
            //如果后面31位不全为0
            if(binChar[k] != '0'){
                isZero = false;
                break;
            }
        }
        if(isZero)
            return "0.0";

        //float部分注意特判Inf和0是一定要特判的，有的时候有NaN
        //Inf就是指数位全部都为1（11111111）
        //0就是分数位全部为0（00..00000）

        //处理指数和分数部分
        //先根据指数处理分数部分的“基”：实际的数值其实是分数部分的每一位乘上对应的基数。首先处理的就是这个基数
        //根据本来的指数位将基数1.x或者0.x扩大（不断*2），然后按照每一位进行处理
        int count = 1;   //用来表示指数位要×的2
        int exponent = 0;
        for(int k = 8; k >= 1; k --){
            exponent += Integer.parseInt(String.valueOf(binChar[k])) * count;
            count *= 2;
        }
        if(exponent != 0){ //如果指数不是0的话说明有指数,是0的话指数依旧是0
           exponent -= 127;
        }
        double fraction = 1.0;
        double floatCount = (double)0.5;
        if(exponent > 0){//如果exponent是正数
            for(int i = 1; i <= exponent; i ++){
                fraction *= 2; floatCount *= 2;
            }
        }else if(exponent < 0){
            for(int i = 1; i <= Math.abs(exponent); i ++){
                fraction /= 2; floatCount /= 2;
            }
        }else{  //如果exponent是0的话，说明是非规格化数，要特判：非规格化数指数位都是0，并且所有位数不全为0
            fraction = 0;
            for(int i = 1; i <= 126; i ++){
                floatCount /= 2;
            }
        }
        for(int k = 9; k < 32; k ++){
            fraction += Double.parseDouble(String.valueOf(binChar[k])) * floatCount;
            floatCount /= 2;
        }

        if(flag)//如果是负数的话
            return "-" + fraction;
        else
            return "" + fraction;


        //如果指数小于0
        /*if(exponent < 0){
            if(!flag){
                return "-1." + fraction + "E" + exponent;
            }
            else{
                return "1." + fraction + "E" + exponent;
            }
        }
        //指数等于0
        else if(exponent == 0){
            if(!flag){
                return "-1." + fraction;
            }
            else{
                return "1." + fraction;
            }
        }
        //指数大于0
        else{
            //如果是正数的话
            if(!flag){

            }else{

            }
        }

         */

    }

    /**
     * The decimal number to its NBCD code
     * */
    public String decimalToNBCD(String decimal) {
        //TODO:
        boolean flag = false;
        String newDecimal;
        if(decimal.charAt(0) == '-') {    //说明是负数
            flag = true;
            newDecimal = decimal.substring(1);
        }
        else
            newDecimal = decimal;

        int[] decimalChar = new int[32];
        //decimalChar初始化
        decimalChar[0] = 1;
        decimalChar[1] = 1;
        for(int i = 2; i < 32; i ++){
            decimalChar[i] = 0;
        }
        if(flag)   //如果是负数第3位是1
            decimalChar[3] = 1;
        //前面这几步用来处理NBCD的符号，1101代表负数，1100代表正数


        int length = newDecimal.length();
        int count = 0;
        for(int i = 31; i > 31 - length * 4; i -= 4){
            //注意decimal是整数的表示法：整数的每一位拿出来作为一个NBCD的四位组合，根据整数这一位单位的数值解析
            //步骤：找出这个整数有几位，每一位解析为NBCD的四位，从index=31开始往index=0数，一直到31-length*4结束，其他数据初始化为0
            int abs = Integer.parseInt(String.valueOf(newDecimal.charAt(length - 1 - count)));
            if(abs >= 8){
                decimalChar[i - 3] = abs / 8;
                abs -= 8;
            }
            if(abs >= 4){
                decimalChar[i - 2] = abs / 4;
                abs -= 4;
            }
            if(abs >= 2){
                decimalChar[i - 1] = abs / 2;
                abs -= 2;
            }
            decimalChar[i] = abs;
            count += 1;
        }
        String res = "";
        for(int i = 0; i < 32; i ++){
            res = res.concat(String.valueOf(decimalChar[i]));
        }

        return res;
    }

    /**
     * NBCD code to its decimal number
     * */
    public String NBCDToDecimal(String NBCDStr) {
        //TODO:
        char[] charNBCD = NBCDStr.toCharArray();
        boolean flag = false;//false默认为正数
        if(charNBCD[3] == '1')//说明是负数
            flag = true;
        String res = "";
        for(int i = 4; i < 32; i += 4){
            int temp = 0;
            int count = 1;
            for(int k = 3; k >= 0; k --){
                temp += Integer.parseInt(String.valueOf(charNBCD[i + k])) * count;
                count *= 2;
            }
            //最开始可能会有很多个4位组合都是0，因此如果整数str没有0就加一个0，有一个0就不再加
            if(temp != 0 || !res.equals("") && !res.contains("0"))
                res = res.concat(String.valueOf(temp));
        }
        //如果在最后res还是空的话，那么补0
        if(res.equals(""))
            return "0";
        if(flag)
            res = "-".concat(res);
        return res;
    }




}
