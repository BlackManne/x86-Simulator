package util;

/**
 * @CreateTime: 2020-11-23 22:13
 */
public class CRC {

    /**
     * CRC计算器
     * @param data 数据流
     * @param polynomial 多项式
     * @return CheckCode
     */
    public static char[] Calculate(char[] data, String polynomial) {
        //TODO
        //首先要在后面补多项式长度-1的0
        //然后计算出要做多少次
        //每一次选择polynomial长度的substing进入molediv方法
        //molediv：
            //1.首先看这个src的第一位是1还是0，如果是1就和polynomial逐位异或，如果是0就和全0逐位异或
            //2.返回结果的最后length-1位和之后的一个char形成新的substing
        String src = new String(data);
        int length = polynomial.length();
        for(int i = 0; i < length - 1; i ++){
            src = src.concat("0");   //在源代码里面补充0
        }
        int cnt = src.length() - length + 1;  //一共要进行多少次除法
        String temp = src.substring(0, length - 1);
        char c;
        for(int i = 0; i < cnt; i ++){
            c = src.charAt(length - 1 + i);
            temp = moleDiv(temp + c, polynomial, length);
        }
        /*String temp;
        String newtemp;
        for(int i = 0; i < cnt; i ++){
            temp = src.substring(i, i + length);
            newtemp = moleDiv(temp, polynomial, length);
            src = src.substring(0, i) + newtemp + src.substring(i + length);  //获得新的src
        }

         */
        return temp.toCharArray();
    }

    private static String moleDiv(String src, String polynomial, int length){
        String res =  "";
        //如果源代码第一位是0的话，不再和polynomial做摩尔除法，而是和0000做摩尔除法
        //所以在这一步对polynomial对一个重新赋值：0000
        if(src.charAt(0) == '0'){
            char[] newPoly = new char[length];
            for(int i = 0; i < length; i ++){
                newPoly[i] = '0';
            }
            polynomial = new String(newPoly);
        }

        //这里才是进行摩尔除法，返回的位数比实际的polynomial的位数少一位，然后加上src再往后一位
        for(int i = 1; i < length; i ++){
            if(src.charAt(i) == polynomial.charAt(i)){
                res = res.concat("0");
            }else
                res = res.concat("1");
        }
        return res;
    }

    /**
     * CRC校验器
     * @param data 接收方接受的数据流
     * @param polynomial 多项式
     * @param CheckCode CheckCode
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode){
        //TODO
        //check是把数据流和checkcode按照顺序排列在一起得到新的data，用新的data和polynomial做摩尔除法
        //然后和多项式做摩尔除法，如果余数是0，说明data没有发生任何变化，否则发生了变化
        String checkcode = new String(CheckCode);
        String src = new String(data) + checkcode;
        char[] newdata = src.toCharArray();
        //
        String temp = String.valueOf(Calculate(newdata, polynomial));
//        int length = polynomial.length();
//        int cnt = src.length() - length + 1;  //一共要进行多少次除法
//        String temp = src.substring(0, length - 1);
//        char c;
//        for(int i = 0; i < cnt; i ++){
//            c = src.charAt(length - 1 + i);
//            temp = moleDiv(temp + c, polynomial, length);
//        }
        return temp.toCharArray();
    }

    /*public static void main(String[] args){
        String polynomial = "1001";
        char[] data = {'1','0','0','0','1','1'};
        char[] CheckCode = {'1','1','1','0'};
        System.out.println(Calculate(data,polynomial));
        System.out.println(Check(data,polynomial,CheckCode));
    }

     */

    /**
     * 这个方法仅用于测试，请勿修改
     * @param data
     * @param polynomial
     */
    public static void CalculateTest(char[] data, String polynomial){
        System.out.print(Calculate(data, polynomial));
    }
    /**
     * 这个方法仅用于测试，请勿修改
     * @param data
     * @param polynomial
     */
    public static void CheckTest(char[] data, String polynomial, char[] CheckCode){
        System.out.print(Check(data, polynomial, CheckCode));
    }
}
