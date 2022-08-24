package meanestSheep;
import java.awt.Point;
class Key
{
	//Uses object Key which is comprised of two  Point object, key1: which hold the x,y values for the sheep and key2: which hold the x,y values for the sheepdogbot
    Point key1;
    Point key2;
    
    //Key Constructor
    public Key(Point key1, Point key2)
    {
        this.key1 = key1;
        this.key2 = key2;
    }
    
    //Overrode the equals and hashCode method of Object Key to allow for simple search in HashMap, enabling me to create a key object of the same state to use for simple lookup
    //instead of traversing through all possible keys in HashMap before lookup
    @Override
    public boolean equals(Object obj) {
    	if(obj == null) {
    		return false;
    	}
    	Key key = (Key) obj; 
    	return (key.key1.x == this.key1.x && key.key1.y == this.key1.y && key.key2.x == this.key2.x && key.key2.y == this.key2.y );
    }
    
    @Override
    public int hashCode() {
     final int PRIME = 31;
     int result = 1;
     result = PRIME * result + this.key1.hashCode() + this.key2.hashCode();
     return result;
    }
    
}
