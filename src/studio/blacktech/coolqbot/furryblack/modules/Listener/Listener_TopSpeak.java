package studio.blacktech.coolqbot.furryblack.modules.Listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.message.CQCode;

import studio.blacktech.coolqbot.furryblack.entry;
import studio.blacktech.coolqbot.furryblack.common.LoggerX.LoggerX;
import studio.blacktech.coolqbot.furryblack.common.message.Message;
import studio.blacktech.coolqbot.furryblack.common.message.MessageDisz;
import studio.blacktech.coolqbot.furryblack.common.message.MessageGrop;
import studio.blacktech.coolqbot.furryblack.common.message.MessageUser;
import studio.blacktech.coolqbot.furryblack.common.module.ModuleListener;

public class Listener_TopSpeak extends ModuleListener {
	private static final long serialVersionUID = 1L;
	// ==========================================================================================================================================================
	//
	// 模块基本配置
	//
	// ==========================================================================================================================================================
	private static String MODULE_PACKAGENAME = "Listener_TopSpeak";
	private static String MODULE_COMMANDNAME = "shui";
	private static String MODULE_DISPLAYNAME = "水群统计";
	private static String MODULE_DESCRIPTION = "水群统计";
	private static String MODULE_VERSION = "29.0";
	private static String[] MODULE_USAGE = new String[] {};
	private static String[] MODULE_PRIVACY_STORED = new String[] {
			"按照\"群-成员-消息\"的层级关系保存所有聊天内容"
	};
	private static String[] MODULE_PRIVACY_CACHED = new String[] {};
	private static String[] MODULE_PRIVACY_OBTAIN = new String[] {};
	// ==========================================================================================================================================================
	//
	// 成员变量
	//
	// ==========================================================================================================================================================
	private ArrayList<Long> GROUP_REPORT;
	private HashMap<Long, GroupStatus> GROUP_STATUS;
	private Thread thread;
	private File CONFIG_GROUP_REPORT;
	private File GROUP_STATUS_SERIAL;

	// ==========================================================================================================================================================
	//
	// 生命周期函数
	//
	// ==========================================================================================================================================================
	public Listener_TopSpeak() throws Exception {
		super(MODULE_PACKAGENAME, MODULE_COMMANDNAME, MODULE_DISPLAYNAME, MODULE_DESCRIPTION, MODULE_VERSION, MODULE_USAGE, MODULE_PRIVACY_STORED, MODULE_PRIVACY_CACHED, MODULE_PRIVACY_OBTAIN);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(LoggerX logger) throws Exception {
		this.initConfFolder();
		this.initDataFolder();
		this.GROUP_REPORT = new ArrayList<>();
		this.CONFIG_GROUP_REPORT = Paths.get(this.FOLDER_CONF.getAbsolutePath(), "grop_report.txt").toFile();
		this.GROUP_STATUS_SERIAL = Paths.get(this.FOLDER_DATA.getAbsolutePath(), "topspeak.serial").toFile();
		if (this.CONFIG_GROUP_REPORT.exists()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.CONFIG_GROUP_REPORT), StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) { continue; }
				this.GROUP_REPORT.add(Long.parseLong(line));
				logger.seek(MODULE_PACKAGENAME, "每日汇报", line);
			}
			reader.close();
		} else {
			this.CONFIG_GROUP_REPORT.createNewFile();
		}
		if (this.GROUP_STATUS_SERIAL.exists()) {
			ObjectInputStream loader = new ObjectInputStream(new FileInputStream(this.GROUP_STATUS_SERIAL));
			this.GROUP_STATUS = (HashMap<Long, GroupStatus>) loader.readObject();
			loader.close();
			logger.seek(MODULE_PACKAGENAME, "读取存档", this.GROUP_STATUS == null ? "空" : "包含" + this.GROUP_STATUS.size() + "个群");
			for (long gropid : this.GROUP_STATUS.keySet()) {
				long time = this.GROUP_STATUS.get(gropid).initdt;
				logger.seek(MODULE_PACKAGENAME, LoggerX.datetime(new Date(time)) + "(" + time + ")", gropid);
			}
			File OLD_STORAGE = Paths.get(this.FOLDER_DATA.getAbsolutePath(), "topspeak_" + LoggerX.formatTime("yyyy_MM_dd_HH_mm_ss") + ".serial").toFile();
			this.GROUP_STATUS_SERIAL.renameTo(OLD_STORAGE);
		} else {
			this.GROUP_STATUS = new HashMap<>();
		}
		List<Group> groups = entry.getCQ().getGroupList();
		for (Group group : groups) {
			if (!this.GROUP_STATUS.containsKey(group.getId())) {
				this.GROUP_STATUS.put(group.getId(), new GroupStatus(group.getId()));
				logger.seek(MODULE_PACKAGENAME, " 添加新群 " + group.getName() + "(" + group.getId() + ")");
			}
		}
		this.ENABLE_USER = false;
		this.ENABLE_DISZ = false;
		this.ENABLE_GROP = true;
	}

	@Override
	public void boot(LoggerX logger) throws Exception {
		logger.info(MODULE_PACKAGENAME, "启动工作线程");
		this.thread = new Thread(new Worker());
		this.thread.start();
	}

	@Override
	public void shut(LoggerX logger) throws Exception {
		logger.info(MODULE_PACKAGENAME, "终止工作线程");
		this.thread.interrupt();
		this.thread.join();
	}

	@Override
	public void save(LoggerX logger) throws Exception {
		logger.info(MODULE_PACKAGENAME, "数据序列化");
		ObjectOutputStream saver = new ObjectOutputStream(new FileOutputStream(this.GROUP_STATUS_SERIAL));
		saver.writeObject(this.GROUP_STATUS);
		saver.close();
	}

	@Override
	public void reload(LoggerX logger) throws Exception {
	}

	@Override
	public void exec(LoggerX logger, Message message) throws Exception {
	}

	@Override
	public void groupMemberIncrease(int typeid, int sendtime, long gropid, long operid, long userid) {
		if (entry.getMessage().isMyself(userid)) {
			this.GROUP_STATUS.put(gropid, new GroupStatus(gropid));
		} else {
			this.GROUP_STATUS.get(gropid).USER_STATUS.put(userid, new UserStatus(userid));
		}
	}

	@Override
	public void groupMemberDecrease(int typeid, int sendtime, long gropid, long operid, long userid) {
		if (entry.getMessage().isMyself(userid)) {
			this.GROUP_STATUS.remove(gropid);
		} else {
			this.GROUP_STATUS.get(gropid).USER_STATUS.remove(userid);
		}
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================
	@Override
	public boolean doUserMessage(int typeid, long userid, MessageUser message, int messageid, int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doDiszMessage(long diszid, long userid, MessageDisz message, int messageid, int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doGropMessage(long gropid, long userid, MessageGrop message, int messageid, int messagefont) throws Exception {
		this.GROUP_STATUS.get(gropid).say(userid, message);
		return true;
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================
	@Override
	public String[] generateReport(int mode, Message message, Object... parameters) {
		switch (mode) {
		case 10:
			String gropid = message.getSwitch("gropid");
			if (gropid == null) {
				return new String[] {
						"参数错误 --gropid 为空"
				};
			} else {
				return this.generateMemberRank(Long.parseLong(gropid));
			}
		}
		return null;
	}

	// ==========================================================================================================================
	public String[] generateMemberRank(long gropid) {
		StringBuilder builder;
		LinkedList<String> report = new LinkedList<>();
		GroupStatus groupStatus = this.GROUP_STATUS.get(gropid).sum();
		// ===========================================================
		builder = new StringBuilder();
		// ===========================================================
		builder.append("（1/4）水群统计" + "\r\n");

		builder.append("自" + LoggerX.formatTime("yyyy-MM-dd HH", new Date(groupStatus.initdt)) + ":00 以来" + "\r\n");
		builder.append("总消息数：" + groupStatus.GROP_MESSAGES + "\r\n");
		builder.append("发言条数：" + (groupStatus.GROP_SENTENCE.size() + groupStatus.GROP_PURECCODE) + "\r\n");
		builder.append("发言字数：" + groupStatus.GROP_CHARACTER + "\r\n");
		builder.append("命令次数：" + groupStatus.GROP_COMMANDS.size() + "\r\n");
		builder.append("发言图数：" + groupStatus.GROP_PICTURES.size() + "\r\n");
		builder.append("闪照图数：" + groupStatus.GROP_SNAPSHOT + "\r\n");
		builder.append("视频个数：" + groupStatus.GROP_TAPVIDEO + "\r\n");
		builder.append("红包个数：" + groupStatus.GROP_HONGBAOS);

		report.add(builder.toString());

		// ===========================================================

		UserStatus userStatus;
		TreeMap<Integer, HashSet<Long>> allMemberRank = new TreeMap<>((a, b) -> b - a);
		for (long userid : groupStatus.USER_STATUS.keySet()) {
			userStatus = groupStatus.USER_STATUS.get(userid);
			int userCharacter = userStatus.USER_SENTENCE.size() + userStatus.USER_PURECCODE;
			if (userCharacter > 0) {
				if (allMemberRank.containsKey(userCharacter)) {
					allMemberRank.get(userCharacter).add(userid);
				} else {
					HashSet<Long> tempSet = new HashSet<>();
					tempSet.add(userid);
					allMemberRank.put(userCharacter, tempSet);
				}
			}
		}
		if (allMemberRank.size() > 0) {
			builder = new StringBuilder();
			builder.append("（2/4）成员排行：" + "\r\n");
			int i = 1;
			for (int userRank : allMemberRank.keySet()) {
				HashSet<Long> tempSet = allMemberRank.get(userRank);
				for (Long userid : tempSet) {
					userStatus = groupStatus.USER_STATUS.get(userid);
					builder.append("No." + i + " - " + entry.getNickmap().getGropnick(gropid, userid) + "(" + userid + ") " + (userStatus.USER_SENTENCE.size() + userStatus.USER_PURECCODE) + "句/" + userStatus.USER_CHARACTER + "字");
					if (userStatus.USER_PICTURES.size() > 0) { builder.append("/" + userStatus.USER_PICTURES.size() + "图"); }
					if (userStatus.USER_SNAPSHOT > 0) { builder.append("/" + userStatus.USER_SNAPSHOT + "闪"); }
					if (userStatus.USER_TAPVIDEO > 0) { builder.append("/" + userStatus.USER_TAPVIDEO + "片"); }
					if (userStatus.USER_HONGBAOS > 0) { builder.append("/" + userStatus.USER_HONGBAOS + "包"); }
					builder.append("\r\n");
				}
				i = i + tempSet.size();
			}
			report.add(builder.substring(0, builder.length() - 2).toString());
		}
		// ===========================================================
		HashMap<String, Integer> allMessageRankTemp = new HashMap<>();
		for (String message : groupStatus.GROP_SENTENCE) {
			message = message.trim();
			if (message.equals(" ")) {
				// @的原因产生了 \u0020 的句子
				continue;
			} else if (message.equals("?")) {
				message = "？";
			} else if (message.equals("??")) {
				message = "？？";
			} else if (message.equals("???")) {
				message = "？？？";
			} else if (message.equals("????")) {
				message = "？？？？";
			} else if (message.equals("wky")) {
				message = "我可以";
			} else if (message.equals("whl")) {
				message = "我好了";
			} else if (message.equals("hso")) {
				message = "好骚哦";
			} else if (message.equals("tql")) {
				message = "太强了";
			} else if (message.equals("tfl")) {
				message = "太富了";
			} else if (message.equals("tcl")) {
				message = "太草了";
			} else if (message.equals("草")) {
				message = "草";
			} else if (message.equals("操")) {
				message = "草";
			} else if (message.equals("艹")) {
				message = "草";
			} else {
				// SAM IS RAGE
				// SAM IS RAGE
			}
			if (allMessageRankTemp.containsKey(message)) {
				allMessageRankTemp.put(message, allMessageRankTemp.get(message) + 1);
			} else {
				allMessageRankTemp.put(message, 1);
			}
		}
		TreeMap<Integer, HashSet<String>> allMessageRank = new TreeMap<>((a, b) -> b - a);
		for (String raw : allMessageRankTemp.keySet()) {
			int tempCount = allMessageRankTemp.get(raw);
			if (allMessageRank.containsKey(tempCount)) {
				allMessageRank.get(tempCount).add(raw);
			} else {
				HashSet<String> tempSet = new HashSet<>();
				tempSet.add(raw);
				allMessageRank.put(tempCount, tempSet);
			}
		}
		allMessageRank.remove(1);
		if (allMessageRank.size() > 0) {
			builder = new StringBuilder();
			builder.append("（3/4）整句排行：" + "\r\n");
			int order = 1;
			int limit = 0;
			for (int messageRank : allMessageRank.keySet()) {
				HashSet<String> tempSet = allMessageRank.get(messageRank);
				for (String message : tempSet) {
					limit++;
					builder.append("No." + +order + " - " + messageRank + "次：" + message + "\r\n");
					if (limit > 20) { break; }
				}
				order = order + tempSet.size();
				if (limit > 20) { break; }
			}
			report.add(builder.substring(0, builder.length() - 2).toString());
		}
		// ===========================================================
		HashMap<String, Integer> allPictureRankTemp = new HashMap<>();
		for (String message : groupStatus.GROP_PICTURES) {
			if (allPictureRankTemp.containsKey(message)) {
				allPictureRankTemp.put(message, allPictureRankTemp.get(message) + 1);
			} else {
				allPictureRankTemp.put(message, 1);
			}
		}
		TreeMap<Integer, HashSet<String>> allPictureRank = new TreeMap<>((a, b) -> b - a);
		for (String raw : allPictureRankTemp.keySet()) {
			int tempCount = allPictureRankTemp.get(raw);
			if (allPictureRank.containsKey(tempCount)) {
				allPictureRank.get(tempCount).add(raw);
			} else {
				HashSet<String> tempSet = new HashSet<>();
				tempSet.add(raw);
				allPictureRank.put(tempCount, tempSet);
			}
		}
		allPictureRank.remove(1);
		if (allPictureRank.size() > 0) {
			int order = 1;
			int limit = 0;
			for (int pictureRank : allPictureRank.keySet()) {
				HashSet<String> tempSet = allPictureRank.get(pictureRank);
				for (String picture : tempSet) {
					report.add("No." + order + " - " + pictureRank + "次：" + CQCode.getInstance().getCQImage(picture).getUrl());
					limit++;
					if (limit > 2) { break; }
				}
				order = order + tempSet.size();
				if (limit > 2) { break; }
			}
		}
		// ===========================================================
		return report.toArray(new String[report.size()]);
	}

	@SuppressWarnings("deprecation")
	class Worker implements Runnable {
		@Override
		public void run() {
			long time;
			Date date;
			do {
				try {
					// =======================================================
					while (true) {
						date = new Date();
						time = 86400L;
						time = time - date.getSeconds();
						time = time - date.getMinutes() * 60;
						time = time - date.getHours() * 3600;
						time = time * 1000;
						entry.getCQ().logDebug(MODULE_PACKAGENAME, "休眠：" + time);
						Thread.sleep(time);
						// =======================================================
						entry.getCQ().logDebug(MODULE_PACKAGENAME, "执行");
						File DAILY_BACKUP = Paths.get(Listener_TopSpeak.this.FOLDER_DATA.getAbsolutePath(), "dailybackup_" + LoggerX.formatTime("yyyy_MM_dd_HH_mm_ss") + ".serial").toFile();
						ObjectOutputStream saver = new ObjectOutputStream(new FileOutputStream(Listener_TopSpeak.this.GROUP_STATUS_SERIAL));
						saver.writeObject(DAILY_BACKUP);
						saver.close();
						for (long temp : Listener_TopSpeak.this.GROUP_STATUS.keySet()) {
							if (Listener_TopSpeak.this.GROUP_REPORT.contains(temp)) {
								entry.getMessage().gropInfo(temp, Listener_TopSpeak.this.generateMemberRank(temp));
							} else {
								continue;
							}
						}
						entry.getCQ().logDebug(MODULE_PACKAGENAME, "结果", "备份于" + DAILY_BACKUP.getAbsolutePath());
						// =======================================================
					}
				} catch (Exception exception) {
					if (entry.isEnable()) {
						entry.getCQ().logWarning(MODULE_PACKAGENAME, "异常");
					} else {
						entry.getCQ().logInfo(MODULE_PACKAGENAME, "关闭");
					}
				}
			} while (entry.isEnable());
		}
	}
}

class GroupStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	public long gropid = 0;
	public long initdt = 0;
	public HashMap<Long, UserStatus> USER_STATUS = new HashMap<>();
	public LinkedList<String> GROP_SENTENCE;
	public LinkedList<String> GROP_COMMANDS;
	public LinkedList<String> GROP_PICTURES;
	public int GROP_SNAPSHOT = 0;
	public int GROP_HONGBAOS = 0;
	public int GROP_TAPVIDEO = 0;
	public int GROP_MESSAGES = 0;
	public int GROP_CHARACTER = 0;
	public int GROP_PURECCODE = 0;

	public GroupStatus(long gropid) {
		this.initdt = System.currentTimeMillis();
		this.gropid = gropid;
		for (Member member : entry.getCQ().getGroupMemberList(gropid)) {
			this.USER_STATUS.put(member.getQQId(), new UserStatus(member.getQQId()));
		}
	}

	public void say(long userid, MessageGrop message) {
		this.USER_STATUS.get(userid).say(message);
	}

	public GroupStatus sum() {
		this.GROP_SENTENCE = new LinkedList<>();
		this.GROP_COMMANDS = new LinkedList<>();
		this.GROP_PICTURES = new LinkedList<>();
		this.GROP_SNAPSHOT = 0;
		this.GROP_HONGBAOS = 0;
		this.GROP_TAPVIDEO = 0;
		this.GROP_MESSAGES = 0;
		this.GROP_CHARACTER = 0;
		this.GROP_PURECCODE = 0;
		for (long userid : this.USER_STATUS.keySet()) {
			UserStatus userStauts = this.USER_STATUS.get(userid).sum();
			this.GROP_SENTENCE.addAll(userStauts.USER_SENTENCE);
			this.GROP_COMMANDS.addAll(userStauts.USER_COMMANDS);
			this.GROP_PICTURES.addAll(userStauts.USER_PICTURES);
			this.GROP_SNAPSHOT = this.GROP_SNAPSHOT + userStauts.USER_SNAPSHOT;
			this.GROP_HONGBAOS = this.GROP_HONGBAOS + userStauts.USER_HONGBAOS;
			this.GROP_TAPVIDEO = this.GROP_TAPVIDEO + userStauts.USER_TAPVIDEO;
			this.GROP_MESSAGES = this.GROP_MESSAGES + userStauts.MESSAGES.size();
			this.GROP_CHARACTER = this.GROP_CHARACTER + userStauts.USER_CHARACTER;
			this.GROP_PURECCODE = this.GROP_PURECCODE + userStauts.USER_PURECCODE;
		}
		return this;
	}
}

class UserStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	public long userid = 0;
	public LinkedList<MessageGrop> MESSAGES = new LinkedList<>();
	public LinkedList<String> USER_COMMANDS;
	public LinkedList<String> USER_SENTENCE;
	public LinkedList<String> USER_PICTURES;
	public int USER_SNAPSHOT = 0;
	public int USER_HONGBAOS = 0;
	public int USER_TAPVIDEO = 0;
	public int USER_CHARACTER = 0;
	public int USER_PURECCODE = 0;

	public UserStatus(long userid) {
		this.userid = userid;
	}

	public void say(MessageGrop message) {
		this.MESSAGES.add(message);
	}

	public UserStatus sum() {
		this.USER_SENTENCE = new LinkedList<>();
		this.USER_COMMANDS = new LinkedList<>();
		this.USER_PICTURES = new LinkedList<>();
		this.USER_CHARACTER = 0;
		this.USER_PURECCODE = 0;
		this.USER_SNAPSHOT = 0;
		this.USER_TAPVIDEO = 0;
		this.USER_HONGBAOS = 0;
		for (MessageGrop temp : this.MESSAGES) {
			if (temp.isCommand()) {
				this.USER_COMMANDS.add(temp.getCommand());
			} else if (temp.isSnappic()) {
				this.USER_SNAPSHOT++;
			} else if (temp.isQQVideo()) {
				this.USER_TAPVIDEO++;
			} else if (temp.isHongbao()) {
				this.USER_HONGBAOS++;
			} else if (temp.hasPicture()) {
				for (String image : temp.getPicture()) {
					this.USER_PICTURES.add(image);
				}
			} else {
				if (temp.isPureCQC()) {
					this.USER_PURECCODE++;
					this.USER_CHARACTER++;
				} else {
					this.USER_SENTENCE.add(temp.getResMessage());
					this.USER_CHARACTER = this.USER_CHARACTER + temp.getResLength();
				}
			}
		}
		return this;
	}
}