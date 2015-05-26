package utilities_package;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;

public class Log {
	private static Log log;
	private PrintWriter pw;

	private Log() {
		try {
			Date date = new Date();
			pw = new PrintWriter(new File("log_" + date.getTime()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Log createInstance() {
		if (log != null)
			return log;

		return (log = new Log());
	}

	public void write(String data) {
		pw.write(data);
	}

	public void close() {
		pw.close();
	}
}
