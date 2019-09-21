package studio.blacktech.coolqbot.furryblack.modules.Trigger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import studio.blacktech.coolqbot.furryblack.entry;
import studio.blacktech.coolqbot.furryblack.common.LoggerX.LoggerX;
import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.message.MessageDisz;
import studio.blacktech.coolqbot.furryblack.common.message.MessageGrop;
import studio.blacktech.coolqbot.furryblack.common.message.MessageUser;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleTrigger;

public class Trigger_WordDeny extends ModuleTrigger {

	private static final long serialVersionUID = 1L;

	// ==========================================================================================================================================================
	//
	// 模块基本配置
	//
	// ==========================================================================================================================================================

	private static String MODULE_PACKAGENAME = "Trigger_WordDeny";
	private static String MODULE_COMMANDNAME = "worddeny";
	private static String MODULE_DISPLAYNAME = "过滤器";
	private static String MODULE_DESCRIPTION = "正则过滤器";
	private static String MODULE_VERSION = "2.0";
	private static String[] MODULE_USAGE = new String[] {};
	private static String[] MODULE_PRIVACY_TRIGER = new String[] {
			"获取消息内容 - 用于过滤"
	};
	private static String[] MODULE_PRIVACY_LISTEN = new String[] {};
	private static String[] MODULE_PRIVACY_STORED = new String[] {
			"触发过滤的用户 - 用于记录违反ELUA的行为"
	};
	private static String[] MODULE_PRIVACY_CACHED = new String[] {};
	private static String[] MODULE_PRIVACY_OBTAIN = new String[] {};

	// ==========================================================================================================================================================
	//
	// 成员变量
	//
	// ==========================================================================================================================================================

	private ArrayList<String> BLACKLIST;

	private File FILE_BLACKLIST;

	private TreeMap<String, LinkedList<MessageUser>> BLOCK_USER_STORE;
	private TreeMap<String, LinkedList<MessageDisz>> BLOCK_DISZ_STORE;
	private TreeMap<String, LinkedList<MessageGrop>> BLOCK_GROP_STORE;

	// ==========================================================================================================================================================
	//
	// 生命周期函数
	//
	// ==========================================================================================================================================================

	public Trigger_WordDeny() throws Exception {
		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_TRIGER, MODULE_PRIVACY_LISTEN, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);
	}

	@Override
	public void init(LoggerX logger) throws Exception {

		this.initConfFolder();
		this.initCofigurtion();

		this.BLACKLIST = new ArrayList<>(100);

		this.BLOCK_USER_STORE = new TreeMap<>();
		this.BLOCK_DISZ_STORE = new TreeMap<>();
		this.BLOCK_GROP_STORE = new TreeMap<>();

		if (this.NEW_CONFIG) {
			this.CONFIG.setProperty("enable_user", "false");
			this.CONFIG.setProperty("enable_disz", "false");
			this.CONFIG.setProperty("enable_grop", "false");
			this.saveConfig();
		} else {
			this.loadConfig();
		}

		this.ENABLE_USER = Boolean.parseBoolean(this.CONFIG.getProperty("enable_user", "false"));
		this.ENABLE_DISZ = Boolean.parseBoolean(this.CONFIG.getProperty("enable_disz", "false"));
		this.ENABLE_GROP = Boolean.parseBoolean(this.CONFIG.getProperty("enable_grop", "false"));

		this.FILE_BLACKLIST = Paths.get(this.FOLDER_CONF.getAbsolutePath(), "blacklist.txt").toFile();

		if (!this.FILE_BLACKLIST.exists()) { this.FILE_BLACKLIST.createNewFile(); }

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.FILE_BLACKLIST), StandardCharsets.UTF_8));

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#")) { continue; }
			this.BLACKLIST.add(line);
			logger.seek(MODULE_PACKAGENAME, "过滤规则", line);
		}
		reader.close();

		boolean temp = this.BLACKLIST.size() > 0;

		this.ENABLE_USER = this.ENABLE_USER && temp;
		this.ENABLE_DISZ = this.ENABLE_DISZ && temp;
		this.ENABLE_GROP = this.ENABLE_GROP && temp;

		for (String templine : this.BLACKLIST) {
			this.BLOCK_USER_STORE.put(templine, new LinkedList<>());
			this.BLOCK_DISZ_STORE.put(templine, new LinkedList<>());
			this.BLOCK_GROP_STORE.put(templine, new LinkedList<>());
		}

	}

	@Override
	public void boot(LoggerX logger) throws Exception {
	}

	@Override
	public void shut(LoggerX logger) throws Exception {
	}

	@Override
	public void save(LoggerX logger) throws Exception {
	}

	@Override
	public void reload(LoggerX logger) throws Exception {
	}

	@Override
	public void exec(LoggerX logger, Message message) throws Exception {
	}

	@Override
	public void groupMemberIncrease(int typeid, int sendtime, long gropid, long operid, long userid) {
	}

	@Override
	public void groupMemberDecrease(int typeid, int sendtime, long gropid, long operid, long userid) {
	}

	@Override
	public boolean doUserMessage(int typeid, long userid, MessageUser message, int messageid, int messagefont) throws Exception {
		for (String temp : this.BLACKLIST) {
			if (Pattern.matches(temp, message.getRawMessage())) {
				this.BLOCK_USER_STORE.get(temp).add(message);
				entry.getMessage().adminInfo("私聊词组过滤：" + message.userid() + "\r\n" + message.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean doDiszMessage(long diszid, long userid, MessageDisz message, int messageid, int messagefont) throws Exception {
		for (String temp : this.BLACKLIST) {
			if (Pattern.matches(temp, message.getRawMessage())) {
				this.BLOCK_DISZ_STORE.get(temp).add(message);
				entry.getMessage().adminInfo("组聊词组过滤：" + message.userid() + "(" + message.diszid() + ")\r\n" + message.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean doGropMessage(long gropid, long userid, MessageGrop message, int messageid, int messagefont) throws Exception {
		for (String temp : this.BLACKLIST) {
			if (Pattern.matches(temp, message.getRawMessage())) {
				this.BLOCK_GROP_STORE.get(temp).add(message);
				entry.getMessage().adminInfo("群聊词组过滤：" + message.userid() + "(" + message.gropid() + ")\r\n" + message.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] generateReport(int mode, Message message, Object... parameters) {

		this.BLOCK_USER = 0;
		this.BLOCK_DISZ = 0;
		this.BLOCK_GROP = 0;

		for (String temp : this.BLOCK_USER_STORE.keySet()) {
			this.BLOCK_USER = this.BLOCK_USER + this.BLOCK_USER_STORE.get(temp).size();
		}

		for (String temp : this.BLOCK_DISZ_STORE.keySet()) {
			this.BLOCK_DISZ = this.BLOCK_DISZ + this.BLOCK_DISZ_STORE.get(temp).size();
		}

		for (String temp : this.BLOCK_GROP_STORE.keySet()) {
			this.BLOCK_GROP = this.BLOCK_GROP + this.BLOCK_GROP_STORE.get(temp).size();
		}

		if (this.BLOCK_USER == 0 && this.BLOCK_DISZ == 0 && this.BLOCK_GROP == 0) { return null; }

		String[] res;
		StringBuilder builder;

		if (mode == 0) {
			builder = new StringBuilder();
			builder.append("拦截私聊：");
			builder.append(this.BLOCK_USER);
			builder.append("\r\n拦截私聊：");
			builder.append(this.BLOCK_DISZ);
			builder.append("\r\n拦截私聊：");
			builder.append(this.BLOCK_GROP);
			res = new String[1];
			res[0] = builder.toString();
		} else {
			res = new String[3];
			builder = new StringBuilder();
			builder.append("拦截私聊：");
			builder.append(this.BLOCK_USER);
			if (this.COUNT_USER > 0) {
				LinkedList<MessageUser> blocks;
				for (String temp : this.BLOCK_USER_STORE.keySet()) {
					blocks = this.BLOCK_USER_STORE.get(temp);
					if (blocks.size() == 0) { continue; }
					builder.append("\r\n规则：\"");
					builder.append(temp);
					builder.append("\" - ");
					builder.append(blocks.size());
					builder.append("次");
					for (MessageUser block : blocks) {
						builder.append("\r\n");
						builder.append(LoggerX.datetime(new Date(block.getSendtime())));
						builder.append(" > ");
						builder.append(entry.getNickmap().getNickname(block.userid()));
						builder.append(" (");
						builder.append(block.userid());
						builder.append(") ");
						builder.append(block.getRawMessage());
					}
				}
				res[0] = builder.toString();
			}

			builder = new StringBuilder();
			builder.append("\r\n拦截组聊：");
			builder.append(this.BLOCK_DISZ);
			if (this.COUNT_DISZ > 0) {
				LinkedList<MessageDisz> blocks;
				for (String temp : this.BLOCK_DISZ_STORE.keySet()) {
					blocks = this.BLOCK_DISZ_STORE.get(temp);
					if (blocks.size() == 0) { continue; }
					builder.append("\r\n规则：\"");
					builder.append(temp);
					builder.append("\" - ");
					builder.append(blocks.size());
					builder.append("次");
					for (MessageDisz block : blocks) {
						builder.append("\r\n");
						builder.append(LoggerX.datetime(new Date(block.getSendtime())));
						builder.append(" > ");
						builder.append(entry.getNickmap().getNickname(block.userid()));
						builder.append(" (");
						builder.append(block.userid());
						builder.append(" [");
						builder.append(block.diszid());
						builder.append("]) ");
						builder.append(block.getRawMessage());
					}
				}
				res[1] = builder.toString();
			}

			builder = new StringBuilder();
			builder.append("\r\n拦截群聊：");
			builder.append(this.BLOCK_GROP);
			if (this.COUNT_GROP > 0) {
				LinkedList<MessageGrop> blocks;
				for (String temp : this.BLOCK_GROP_STORE.keySet()) {
					blocks = this.BLOCK_GROP_STORE.get(temp);
					if (blocks.size() == 0) { continue; }
					builder.append("\r\n规则：\"");
					builder.append(temp);
					builder.append("\" - ");
					builder.append(blocks.size());
					builder.append("次");
					for (MessageGrop block : blocks) {
						builder.append("\r\n");
						builder.append(LoggerX.datetime(new Date(block.getSendtime())));
						builder.append(" > ");
						builder.append(entry.getNickmap().getNickname(block.userid()));
						builder.append(" (");
						builder.append(block.userid());
						builder.append(" [");
						builder.append(block.gropid());
						builder.append("])：");
						builder.append(block.getRawMessage());
					}
				}
				res[2] = builder.toString();
			}
		}
		return res;
	}
}