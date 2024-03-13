package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import transformer.Transformer;

public class Jz implements Instruction{
    @Override
    public int exec(String eip, int opcode) {
        MMU mmu = MMU.getMMU();
        Transformer transformer = new Transformer();
        ALU alu = new ALU();
        EFlag eFlag = (EFlag) CPU_State.eflag;
        boolean hasZF = eFlag.getZF();

        if(hasZF){   //如果ZF是0，说明要跳转
            String newaddr = transformer.intToBinary(String.valueOf(Integer.parseInt(transformer.binaryToInt(eip)) + 8));
            newaddr = CPU_State.cs.read() + newaddr;
            String iv = String.valueOf(mmu.read(newaddr, 8));
            if(iv.charAt(0) == '1'){
                iv = "111111111111111111111111" + iv;
            }else{
                iv = "000000000000000000000000" + iv;
            }
            boolean hasCF = eFlag.getCF();
            String jpaddr = alu.add(iv, eip);
            //设置一下eip寄存器
            CPU_State.eip.write(jpaddr);
            //重新保存一下CF位
            eFlag.setCF(hasCF);
            return 0;
        }
        //不跳转的时候，注意也要恢复CF位
        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, transformer.intToBinary("16"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);
        return 16;
    }
}
