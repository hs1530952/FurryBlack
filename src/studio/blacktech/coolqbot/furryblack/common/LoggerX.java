package studio.blacktech.coolqbot.furryblack.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class LoggerX {

	private final HashMap<Integer, Long> clock = new HashMap<>();
	private final HashMap<String, Long> clockS = new HashMap<>();

	public static String time() {
		final SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formater.format(new Date());
	}

	public static String time(final Date date) {
		final SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formater.format(date);
	}

	public static String time(final String formate) {
		final SimpleDateFormat formater = new SimpleDateFormat(formate);
		return formater.format(new Date());
	}

	public static String time(final String formate, final Date date) {
		final SimpleDateFormat formater = new SimpleDateFormat(formate);
		return formater.format(date);
	}

	public long clock(final int name, final boolean isReset) {
		final long time = System.nanoTime();

		if (this.clock.containsKey(name)) {
			if (isReset) { this.clock.put(name, time); }
			return time - this.clock.get(name);
		} else {
			this.clock.put(name, time);
		}
		return 0;
	}

	public long clock(final String name, final boolean isReset) {
		final long time = System.nanoTime();
		if (this.clockS.containsKey(name)) {
			if (isReset) { this.clockS.put(name, time); }
			return time - this.clockS.get(name);
		} else {
			this.clockS.put(name, time);
		}
		return 0;
	}
}
