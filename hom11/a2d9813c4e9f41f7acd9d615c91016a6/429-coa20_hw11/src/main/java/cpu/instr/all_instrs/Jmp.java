package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import program.Log;
import transformer.Transformer;

public class Jmp implements Instruction{
    Transformer t = new Transformer();
    ALU alu = new ALU();
    EFlag eFlag = (EFlag) CPU_State.eflag;

    @Override
    public int exec(String eip, int opcode) {
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));
        String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 8));

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(CPU_State.eip.read(),"000000000000000000000000" + bitstream);
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        Log.write("11101011" + bitstream);
        //跳转指令返回0
        return 0;
    }
}
