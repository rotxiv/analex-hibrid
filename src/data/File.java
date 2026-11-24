package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.Component;
import java.io.IOException;

import javax.swing.JFileChooser;

public final class File {

	private File() {
	}

    public static String loadFile(Component parent) throws IOException {
		JFileChooser fc = new JFileChooser();
		int ret = fc.showOpenDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION) {
			java.io.File f = fc.getSelectedFile();
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append('\n');
				}
				return sb.toString();
			}
		}
		return null;
	}

}