package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import transformer.Transformer;

public class Hlt implements Instruction {
    @Override
    public int exec(String eip, int opcode) {

        //正常计算机里halt之后不用再设置eip，因为下一轮取指令是重新开始取
        //但是这个里面因为指令是连续存储在磁盘中的，所以要+8，以便于下一次执行时取指令是正确的
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag)CPU_State.eflag;

        //终止指令设置ICC为0b11；
        CPU_State.ICC = 0b11;

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, transformer.intToBinary("8"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 8;
    }
}
