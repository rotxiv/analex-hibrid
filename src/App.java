import view.Principal;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) throws Exception {
        
        SwingUtilities.invokeLater(() -> {
			Principal p = new Principal();
			p.setVisible(true);
		});
    }
}
