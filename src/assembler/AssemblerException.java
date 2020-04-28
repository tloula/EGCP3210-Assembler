package assembler;

/**
 * Custom exception class for negative factorials
 * 
 * @author Trevor Loula
 */
public class AssemblerException  extends RuntimeException {
    public AssemblerException (String err) {
        super (err);
    }
}