package cpu;

import cpu.instr.all_instrs.InstrFactory;
import cpu.instr.all_instrs.Instruction;
import transformer.Transformer;

public class CPU {

    Transformer transformer = new Transformer();
    MMU mmu = MMU.getMMU();


    /**
     * execInstr specific numbers of instructions
     *
     * @param number numbers of instructions
     */
    public int execInstr(long number) {
        // 执行过的指令的总长度
        int totalLen = 0;
        while (number > 0) {
            totalLen += execInstr();
            number --;
        }
        return totalLen;
    }

    /**
     * execInstr a single instruction according to eip value
     */
    private int execInstr() {
        String eip = CPU_State.eip.read();
        int len = decodeAndExecute(eip);
        return len;
    }

    private int decodeAndExecute(String eip) {
        int opcode = instrFetch(eip, 1);
        Instruction instruction = InstrFactory.getInstr(opcode);
        assert instruction != null;

        //exec the target instruction
        CPU_State.ICC = 0b10;
        int len = instruction.exec(eip, opcode);
        return len;


    }

    /**
     * @param eip
     * @param length opcode的字节数，本作业只使用单字节opcode
     * @return
     */
    private int instrFetch(String eip, int length) {
        eip = CPU_State.cs.read() + eip;
        //因为磁盘存入的每一位是一个二进制位，所以要读取8个二进制位，也就是一个字节
        return Integer.parseInt(transformer.binaryToInt(String.valueOf(mmu.read(eip, 8))));
    }

    public void execUntilHlt(){
        // TODO ICC
        //开始时设置ICC为取指令
        CPU_State.ICC = 0b00;
        while(CPU_State.ICC != 0b11){    //不停机就一直执行
            CPU_State.ICC = 0b00;
            execInstr();
        }
    }
}

