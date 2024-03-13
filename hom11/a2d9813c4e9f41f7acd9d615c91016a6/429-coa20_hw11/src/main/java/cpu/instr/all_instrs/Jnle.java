package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import program.Log;
import transformer.Transformer;

public class Jnle implements Instruction{
    Transformer t = new Transformer();
    ALU alu = new ALU();
    EFlag eFlag = (EFlag) CPU_State.eflag;

    @Override
    public int exec(String eip, int opcode) {
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));
        String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 8));
        boolean hasCF = eFlag.getCF();
        String jpaddr;
        if(!eFlag.getZF() && eFlag.getSF() == eFlag.getOF()){
            //a>b跳转
            jpaddr = alu.add(CPU_State.eip.read(),"000000000000000000000000" + bitstream);
//            jpaddr = alu.add(jpaddr, t.intToBinary("16"));
            CPU_State.eip.write(jpaddr);
            eFlag.setCF(hasCF);

            Log.write("01111111" + bitstream);
            //跳转返回0
            return 0;
        }
        else{
            //a<=b不跳转
            jpaddr = alu.add(CPU_State.eip.read(), t.intToBinary("16"));
            CPU_State.eip.write(jpaddr);
            eFlag.setCF(hasCF);

            Log.write("01111111" + bitstream);
        }

        return 16;
    }
}
