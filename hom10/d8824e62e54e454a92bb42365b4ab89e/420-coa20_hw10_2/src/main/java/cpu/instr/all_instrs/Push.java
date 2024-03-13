package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import memory.Memory;
import transformer.Transformer;

public class Push implements Instruction{
    @Override
    public int exec(String eip, int opcode) {
        Memory memory = Memory.getMemory();
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag) CPU_State.eflag;
        boolean hasCF = eFlag.getCF();

        String pushres = CPU_State.ebx.read();
        //esp寄存器里面存储了栈顶的结构
        String esp = CPU_State.esp.read();
        String newesp = alu.sub(transformer.intToBinary("1"),esp);
//        String newesp = transformer.intToBinary(String.valueOf((Integer.parseInt(transformer.binaryToInt(esp))) - 1));
        CPU_State.esp.write(newesp);
        memory.pushStack(newesp, pushres);
        //更新esp的地址

        String jpaddr = alu.add(eip, transformer.intToBinary("8"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 8;
    }
}
