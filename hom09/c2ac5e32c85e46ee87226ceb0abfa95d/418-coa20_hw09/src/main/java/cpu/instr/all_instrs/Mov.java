package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import transformer.Transformer;

public class Mov implements Instruction{
    @Override
    public int exec(String eip, int opcode) {
        MMU mmu = MMU.getMMU();
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag) CPU_State.eflag;

        String newaddr = transformer.intToBinary(String.valueOf(Integer.parseInt(transformer.binaryToInt(eip)) + 8));
        newaddr = CPU_State.cs.read() + newaddr;
        String iv = String.valueOf(mmu.read(newaddr, 32));
        CPU_State.eax.write(iv);

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, transformer.intToBinary("40"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 40;
    }
}
