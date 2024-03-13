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
//        String programmePath = "D:\\NJU\\大二上\\计算机组成与体系结构\\作业\\hom09\\c2ac5e32c85e46ee87226ceb0abfa95d\\418-coa20_hw09\\test\\adc_test_3.txt";
        try {
            int eip = Loader.loadExec(programmePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Set up eip value , which will be the first instruction
        int ans = cpu.execInstr(1);
//        System.out.println(ans);
    }
}
