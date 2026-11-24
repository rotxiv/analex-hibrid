package me.rotxiv.analex.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import me.rotxiv.analex.application.Analex;
import me.rotxiv.analex.application.Cinta;
import me.rotxiv.analex.application.Token;
import me.rotxiv.analex.data.File;

public class Principal extends JFrame {

    private JTextArea sourceArea;
	private DefaultTableModel tableModel;
	private JTable resultTable;

	private Analex analex;
	private boolean currentTokenDisplayed;
	private boolean analysisFinished;

    public Principal() {
        setTitle("Analizador Léxico");
        setSize(800, 600);
		initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
		sourceArea = new JTextArea();
		sourceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		sourceArea.setLineWrap(true);
		sourceArea.setWrapStyleWord(true);
		JScrollPane leftScroll = new JScrollPane(sourceArea);
		leftScroll.setBorder(BorderFactory.createTitledBorder("Programa fuente"));

		JPanel topLeft = new JPanel(new BorderLayout());
		JButton loadBtn = new JButton("Cargar archivo");
		JButton clearBtn = new JButton("Limpiar");
		JButton clearAllBtn = new JButton("Limpiar todo");
		JPanel topButtons = new JPanel();
		topButtons.add(loadBtn);
		topButtons.add(clearBtn);
		topButtons.add(clearAllBtn);
		topLeft.add(topButtons, BorderLayout.NORTH);
		topLeft.add(leftScroll, BorderLayout.CENTER);

		String[] cols = {"Token", "Lexema"};
		tableModel = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		resultTable = new JTable(tableModel);
		resultTable.setFillsViewportHeight(true);
		resultTable.setRowHeight(22);
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(120);
		resultTable.getColumnModel().getColumn(1).setPreferredWidth(260);
		JScrollPane rightScroll = new JScrollPane(resultTable);
		rightScroll.setBorder(BorderFactory.createTitledBorder("Resultado del análisis"));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeft, rightScroll);
		split.setResizeWeight(0.6);

		JPanel bottom = new JPanel();
		JButton analyzeAllBtn = new JButton("Analizar todo");
		JButton analyzeNextBtn = new JButton("Analizar siguiente");
		bottom.add(analyzeAllBtn);
		bottom.add(analyzeNextBtn);

		sourceArea.setBackground(new Color(0xFAF8EF));
		resultTable.setBackground(new Color(0xF6FBFF));
		split.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		getContentPane().setLayout(new BorderLayout(8, 8));
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(bottom, BorderLayout.SOUTH);

		loadBtn.addActionListener(e -> {
			try {
				String source = File.loadFile(this);
				if (source != null) {
					sourceArea.setText(source);
					clear();
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Error al cargar archivo:\n" + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		clearBtn.addActionListener(e -> clear());
		clearAllBtn.addActionListener(e -> clearAll());

		analyzeAllBtn.addActionListener(e -> analyzeAll());
		analyzeNextBtn.addActionListener(e -> analyzeNext());
	}

	private void clear() {
		tableModel.setRowCount(0);
		analex = null;
		currentTokenDisplayed = false;
		analysisFinished = false;
		sourceArea.getHighlighter().removeAllHighlights();
	}

    private void clearAll() {
		tableModel.setRowCount(0);
		analex = null;
		currentTokenDisplayed = false;
		analysisFinished = false;
		sourceArea.getHighlighter().removeAllHighlights();
		sourceArea.setText("");
	}

    private void highlight(int pos, String lexema) {
		Highlighter highlighter = sourceArea.getHighlighter();
		highlighter.removeAllHighlights();
		if (lexema == null || lexema.isEmpty())
			return;

		String source = sourceArea.getText();
		if (pos < 0 || pos > source.length() - lexema.length()
				|| !source.regionMatches(true, pos, lexema, 0, lexema.length()))
			return;

		int end = pos + lexema.length();
		try {
			highlighter.addHighlight(pos, end,
					new DefaultHighlighter.DefaultHighlightPainter(new Color(0xFFF59D)));
		} catch (BadLocationException ex) {
			throw new IllegalStateException("No se pudo resaltar el lexema analizado.", ex);
		}
		sourceArea.setCaretPosition(end);
	}

	private void analyzeNext() {
		try {
			if (analysisFinished)
				return;
			if (analex == null) {
				prepareAnalex();
			} else if (currentTokenDisplayed) {
				analex.avanzar();
				currentTokenDisplayed = false;
			}
			Token token = analex.Preanalisis();
			String lex = analex.lexema();
			tableModel.addRow(new Object[] { token.toString(), lex });
			analex.resaltar();
			currentTokenDisplayed = true;
			analysisFinished = token.getNom() == Token.FIN;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error durante el análisis:\n" + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void analyzeAll() {
		try {
			prepareAnalex();
			if (analex == null)
				return;
			while (true) {
				Token token = analex.Preanalisis();
				String lex = analex.lexema();
				tableModel.addRow(new Object[] { token.toString(), lex });
				if (token.getNom() == Token.FIN)
					break;
				analex.avanzar();
			}
			currentTokenDisplayed = true;
			analysisFinished = true;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error durante el análisis:\n" + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void prepareAnalex() {
		String prog = sourceArea.getText();
		Cinta cinta = new Cinta(prog);
		sourceArea.getHighlighter().removeAllHighlights();
		currentTokenDisplayed = false;
		analysisFinished = false;
		analex = new Analex(cinta) {
			@Override
			public void comunicate(int pos, String lexema) {
				if (SwingUtilities.isEventDispatchThread()) {
					highlight(pos, lexema);
				} else {
					SwingUtilities.invokeLater(() -> highlight(pos, lexema));
				}
			}
		};
		tableModel.setRowCount(0);
	}
}
