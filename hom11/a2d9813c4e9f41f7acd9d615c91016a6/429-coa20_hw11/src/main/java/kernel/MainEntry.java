package kernel;

import cpu.CPU;
import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import memory.Memory;

import java.io.IOException;

public class MainEntry {
    //Compose the components
    public static final CPU cpu = new CPU();
    public static final ALU alu = new ALU();
    public static final MMU mmu = MMU.getMMU();
    public static final Memory memory = Memory.getMemory();
    public static final EFlag eflag = (EFlag) CPU_State.eflag;

    public static void main(String[] args) {
        assert args.length > 0;
        String programmePath = args[0];
        //String programmePath = "D:\\NJU\\大二上\\计算机组成与体系结构\\作业\\hom11\\a2d9813c4e9f41f7acd9d615c91016a6\\429-coa20_hw11\\test\\max_test_2.txt";
        try {
            int eip = Loader.loadExec(programmePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Set up eip value , which will be the first instruction
        cpu.execUntilHlt();
    }
}
