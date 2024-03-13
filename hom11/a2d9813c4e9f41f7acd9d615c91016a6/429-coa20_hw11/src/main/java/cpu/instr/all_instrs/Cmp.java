package cpu.instr.all_instrs;

import cpu.CPU;
import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import program.Log;
import transformer.Transformer;

public class Cmp implements Instruction{
    Transformer t = new Transformer();
    ALU alu = new ALU();
    EFlag eFlag = (EFlag) CPU_State.eflag;

    @Override
    public int exec(String eip, int opcode) {
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));
        String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 8));
        //所有用例都是11 001 000，cmp ev gv
        //所以ev是eax，gv是ecx
        int fst = Integer.parseInt(t.binaryToInt(CPU_State.eax.read()));
        int scd = Integer.parseInt(t.binaryToInt(CPU_State.ecx.read()));
        int ressub = fst - scd;


        if(ressub == 0) {
            eFlag.setZF(true);
            eFlag.setSF(false);
        }else{
            eFlag.setZF(false);
            if(ressub < 0){
                eFlag.setSF(true);
            }else {
                eFlag.setSF(false);
            }
        }

//        if(ressub > 0){
//            eFlag.setZF(false);
//        }else
//            eFlag.setZF(true);

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, t.intToBinary("16"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        Log.write("00111001" + bitstream);
        return 16;
    }
}
