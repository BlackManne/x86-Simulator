package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import transformer.Transformer;
import util.BinaryIntegers;

public class Cmp implements Instruction {
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
        int ressub = Integer.parseInt(transformer.binaryToInt(eax)) - Integer.parseInt(transformer.binaryToInt(iv));
//        String subres = alu.sub(eax, iv);
        //cf是借位，ZF是0位，判断是不是0
        if(ressub == 0) { //如果是0
            eFlag.setZF(true);
            eFlag.setCF(false);
        }else{
            eFlag.setZF(false);
            if(ressub < 0){
                eFlag.setCF(true);
            }else {
                eFlag.setCF(false);
            }
        }

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, transformer.intToBinary("40"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        return 40;
    }
}
