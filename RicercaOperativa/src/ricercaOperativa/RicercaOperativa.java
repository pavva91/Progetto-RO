package ricercaOperativa;

import javax.swing.JFrame;
import javax.swing.UIManager;

import ricercaOperativa.Views.RicercaOperativaPanel;
import ricercaOperativa.cotroller.RicercaOperativaController;

public class RicercaOperativa {
	
	private JFrame frame;
	
	public static void main(String[] args) {
		// Set cross-platform Java L&F
	 	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	 	RicercaOperativa window = new RicercaOperativa();
		window.frame.setVisible(true);

	}
	
	public RicercaOperativa() {
		initialize();
	}
	
private void initialize() {
		
				
		frame = new JFrame();
		frame.setTitle("Ricerca Operativa");
		frame.setBounds(100, 100, 1100, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		RicercaOperativaPanel panelRicercaOperativa = new RicercaOperativaPanel();
		frame.getContentPane().add(panelRicercaOperativa);
		RicercaOperativaController controller = new RicercaOperativaController(panelRicercaOperativa, frame);
		
	}

}
