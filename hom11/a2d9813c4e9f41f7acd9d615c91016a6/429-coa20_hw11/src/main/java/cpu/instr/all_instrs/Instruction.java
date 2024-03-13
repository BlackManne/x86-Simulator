package cpu.instr.all_instrs;

import program.Log;

public interface Instruction {

    int exec(String eip, int opcode);

    default String toBinaryStr(){
        //TODO 二进制执行流
        String bitstream = null;
        Log.write(bitstream);
        //TODO 返回什么呢？
        return null;
    }
}
