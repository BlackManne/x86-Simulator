package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import memory.Memory;
import transformer.Transformer;

public class Pop implements Instruction{
    @Override
    public int exec(String eip, int opcode) {
        Memory memory = Memory.getMemory();
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag) CPU_State.eflag;
        boolean hasCF = eFlag.getCF();

        String esp = CPU_State.esp.read();
        String newesp = alu.add(transformer.intToBinary("1"),esp);
//        String newesp = transformer.intToBinary(String.valueOf((Integer.parseInt(transformer.binaryToInt(esp))) + 1));
        CPU_State.esp.write(newesp);
        String popstr = memory.topOfStack(esp);

        if(opcode == 0x58){  //0x58
            CPU_State.eax.write(popstr);
        }else if (opcode == 0x59){    //0x59
            CPU_State.ecx.write(popstr);
        }else if(opcode == 0x5a){       //0x5a
            CPU_State.edx.write(popstr);
        }

        String jpaddr = alu.add(eip, transformer.intToBinary("8"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 8;
    }
}
