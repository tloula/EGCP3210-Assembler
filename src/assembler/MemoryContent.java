package assembler;

import java.util.Comparator;

/**
 * MemoryContent Class
 * 
 * For object storing memory content
 * 
 * @author Trevor Loula
 */
public class MemoryContent {
    private String address;
    private String contents;
    
    public MemoryContent(String address, String contents){
        this.address = address.toUpperCase();
        this.contents = contents.toUpperCase();
        while(this.address.length() < 3){
            this.address = "0" + this.address;
        }        
    }
    public String getAddress(){
        return this.address;
    }
    public String getContents(){
        return contents;
    }
    
    public static Comparator<MemoryContent> MemoryComparator = new Comparator<MemoryContent>() {
        @Override
	public int compare(MemoryContent m1, MemoryContent m2) {
            int address1 = Integer.parseInt(m1.getAddress(), 16);
            int address2 = Integer.parseInt(m2.getAddress(), 16);
            if(address1 > address2){
                return 1;
            } else if (address1 < address2){
                return -1;
            } else {
                return 0;
            }
    }};
}