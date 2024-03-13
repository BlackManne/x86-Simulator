package util;

import transformer.Transformer;

public class Tools {
    public int log(int input, int a){
        int res = 0;
        while(input >= a){
            input /= a;
            res ++;
        }
        return res;
    }
}
