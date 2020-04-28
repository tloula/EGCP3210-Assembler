package assembler;

/**
 * Instruction Class
 * 
 * For objects storing an instruction.
 * 
 * @author Trevor Loula
 */
public class Instruction {
    private String address;
    private String instruction;
    private String reference;
    private String indirect;
    
    public Instruction(String address, String instruction, String reference, String indirect){
        this.address = address;
        this.instruction = instruction;
        this.reference = reference;
        this.indirect = indirect;
    }
    public String getAddress(){
        return this.address;
    }
    public String getInstruction(){
        return this.instruction;
    }
    public String getReference(){
        return this.reference;
    }
    public String getIndirect(){
        return this.indirect;
    }
}
