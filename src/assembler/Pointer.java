package assembler;

/**
 * Pointer Class
 * 
 * For objects storing a pointer.
 * 
 * @author Trevor Loula
 */
public class Pointer {
    private String address;
    private String label;
    
    public Pointer(String address, String label){
        this.address = address.toUpperCase();
        this.label = label.toUpperCase();
        while(this.address.length() < 3){
            this.address = "0" + this.address;
        }
    }
    public String getAddress(){
        return this.address;
    }
    public String getLabel(){
        return this.label;
    }    
}
