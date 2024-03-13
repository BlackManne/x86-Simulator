package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import transformer.Transformer;

public class Sub implements Instruction{
    @Override
    public int exec(String eip, int opcode) {
        MMU mmu = MMU.getMMU();
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag) CPU_State.eflag;

        String eax = CPU_State.eax.read();
        String newaddr = transformer.intToBinary(String.valueOf(Integer.parseInt(transformer.binaryToInt(eip)) + 8));
        //读取32位的数据
        newaddr = CPU_State.cs.read() + newaddr;
        String iv = String.valueOf(mmu.read(newaddr, 32));
        String strres = alu.sub(iv, eax);
        //结果存放到eax寄存器中
        CPU_State.eax.write(strres);

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, transformer.intToBinary("40"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 40;
    }
}
