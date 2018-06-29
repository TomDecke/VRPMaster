package addOns;
import java.awt.Color;

public class MyColours {		

	public static Color getColour(int num) {
		Color c = Color.BLACK;
		num = num%9;
		switch(num){
		case 0:
			c = Color.BLUE;
			break;
		case 1:
			c = Color.RED;
			break;
		case 2:
			c = Color.GREEN;
			break;
		case 3:
			c = Color.MAGENTA;
			break;
		case 4:
			c = Color.YELLOW;
			break;
		case 5:
			c = Color.ORANGE;
			break;
		case 6:
			c = Color.PINK;
			break;
		case 7:
			c = Color.CYAN;
			break;
		case 8:
			c = Color.DARK_GRAY;
			break;
	}
		return c;
	}
}
