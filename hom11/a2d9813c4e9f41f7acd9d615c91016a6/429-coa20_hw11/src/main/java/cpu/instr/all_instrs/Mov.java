package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.EFlag;
import memory.Memory;
import program.Log;
import transformer.Transformer;

public class Mov implements Instruction{
    Transformer t = new Transformer();
    ALU alu = new ALU();
    EFlag eFlag = (EFlag) CPU_State.eflag;

    @Override
    public int exec(String eip, int opcode) {
        if(opcode == 0xc7)
            return execc7(eip,opcode);
        else if(opcode == 0x8b)
            return exec8b(eip, opcode);
        else if(opcode == 0x89)
            return exec89(eip, opcode);
        return 0;
    }

    public int execc7(String eip, int opcode){
        //对内存中的变量赋值,mod肯定是0b00,因此一定有displacement
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));

        String bitstream = String.valueOf(Memory.getMemory().read(alu.add(eip, t.intToBinary("8")), 72));
        //String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 72));
        String ModRM = bitstream.substring(0, 8);
        String displacement = bitstream.substring(8, 40);
        String imm = bitstream.substring(40, 72);
        String RM = ModRM.substring(5,8);

        if(RM.equals("011")){
            //说明基址存放在EBX当中
            String memoryaddr = alu.add(displacement, CPU_State.ebx.read());
            //写入内存
            Memory.getMemory().write(memoryaddr, 32, imm.toCharArray());
        }

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, t.intToBinary("80"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        Log.write("11000111" + bitstream);
        return 80;
    }

    public int exec8b(String eip, int opcode){
        //将内存中的值加载到寄存器,
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));

        String bitstream = String.valueOf(Memory.getMemory().read(alu.add(eip, t.intToBinary("8")), 40));
        //String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 40));
        String ModRM = bitstream.substring(0, 8);
        String displacement = bitstream.substring(8, 40);

        String reg = ModRM.substring(2,5);
        String RM = ModRM.substring(5,8);

        if(RM.equals("011")){
            //说明基址存放在EBX当中
            String memoryaddr = alu.add(displacement, CPU_State.ebx.read());
            String data = String.valueOf(Memory.getMemory().read(memoryaddr, 32));
            //String data = String.valueOf(MMU.getMMU().read(ds + memoryaddr, 32));
            if(reg.equals("000")){
                //数据放在EAX里
                CPU_State.eax.write(data);
            }else if(reg.equals("001")){
                //数据放在EXC里面
                CPU_State.ecx.write(data);
            }
        }

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, t.intToBinary("48"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        Log.write("10001011" + bitstream);
        return 48;
    }

    public int exec89(String eip, int opcode){
        //从寄存器移到内存中，相当于由寄存器中的数值对内存中的变量赋值
        String ds = CPU_State.ds.read();
        String logicAddr = ds + alu.add(eip, t.intToBinary("8"));

        String bitstream = String.valueOf(Memory.getMemory().read(alu.add(eip, t.intToBinary("8")), 40));
        //String bitstream = String.valueOf(MMU.getMMU().read(logicAddr, 40));
        String ModRM = bitstream.substring(0, 8);
        String displacement = bitstream.substring(8, 40);

        String reg = ModRM.substring(2,5);
        String RM = ModRM.substring(5,8);

        if(RM.equals("011")){
            //基址存放在EBX当中
            String memoryaddr = alu.add(displacement, CPU_State.ebx.read());
            if(reg.equals("000")){
                //数据放在EAX里
                String data = CPU_State.eax.read();
                Memory.getMemory().write(memoryaddr, 32, data.toCharArray());
            }else if(reg.equals("001")){
                String data = CPU_State.ecx.read();
                Memory.getMemory().write(memoryaddr, 32, data.toCharArray());
            }
        }

        boolean hasCF = eFlag.getCF();
        String jpaddr = alu.add(eip, t.intToBinary("48"));
        CPU_State.eip.write(jpaddr);
        eFlag.setCF(hasCF);

        Log.write("10001001" + bitstream);
        return 48;
    }
}
