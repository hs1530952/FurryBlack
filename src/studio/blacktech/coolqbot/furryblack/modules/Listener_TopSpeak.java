package studio.blacktech.coolqbot.furryblack.modules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.sobte.cqp.jcq.entity.Group;
import com.sobte.cqp.jcq.entity.Member;
import com.sobte.cqp.jcq.event.JcqApp;

import studio.blacktech.coolqbot.furryblack.entry;
import studio.blacktech.coolqbot.furryblack.common.LoggerX;
import studio.blacktech.coolqbot.furryblack.common.Message;
import studio.blacktech.coolqbot.furryblack.common.Module;
import studio.blacktech.coolqbot.furryblack.common.ModuleListener;

@SuppressWarnings("unused")
public class Listener_TopSpeak extends ModuleListener {

	private int GLOBAL_PICTURE = 0;
	private int GLOBAL_SENTENCE = 0;
	private int GLOBAL_CHARACTER = 0;

	public LinkedList<Message> RAW_MESSAGE = new LinkedList<>();
	private final HashMap<Long, GroupStatus> GROUP_STATUS = new HashMap<>();

	public Listener_TopSpeak() {
		this.MODULE_PACKAGENAME = "shui";
		this.MODULE_DISPLAYNAME = "水群统计";
		this.MODULE_DESCRIPTION = "水群统计";
		this.MODULE_VERSION = "14.2.2";
		this.MODULE_USAGE = new String[] {};
		this.MODULE_PRIVACY_TRIGER = new String[] {};
		this.MODULE_PRIVACY_LISTEN = new String[] {
				"缓存所有群聊消息"
		};
		this.MODULE_PRIVACY_STORED = new String[] {
				"保存所有聊天内容至文件而不分析"
		};
		this.MODULE_PRIVACY_CACHED = new String[] {
				"获取消息发送群",
				"获取消息发送人",
				"获取消息而不分析"
		};
		this.MODULE_PRIVACY_OBTAIN = new String[] {};
		final List<Group> groups = JcqApp.CQ.getGroupList();
		for (final Group group : groups) {
			final GroupStatus groupStatus = new GroupStatus();
			for (final Member member : JcqApp.CQ.getGroupMemberList(group.getId())) {
				groupStatus.USER_STATUS.put(member.getQqId(), new UserStatus());
			}
			this.GROUP_STATUS.put(group.getId(), groupStatus);
		}
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	@Override
	public boolean doUserMessage(final int typeid, final long userid, final Message message, final int messageid, final int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doDiszMessage(final long diszid, final long userid, final Message message, final int messageid, final int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doGropMessage(final long gropid, final long userid, final Message message, final int messageid, final int messagefont) throws Exception {
		if (message.isCommand) { return false; }
		if (message.isPicture) {
			this.GLOBAL_PICTURE++;
			this.GROUP_STATUS.get(gropid).pic(userid, message);
		} else {
			this.GLOBAL_SENTENCE++;
			this.GLOBAL_CHARACTER = this.GLOBAL_CHARACTER + message.length;
			this.GROUP_STATUS.get(gropid).say(userid, message);
		}
		return true;
	}

	// ==========================================================================================================================================================
	//
	//
	//
	// ==========================================================================================================================================================

	private class GroupStatus {

		public int GROUP_PICTURE = 0;
		public int GROUP_SENTENCE = 0;
		public int GROUP_CHARACTER = 0;

		public LinkedList<Long> USER_MESSAGES = new LinkedList<>();
		public LinkedList<Long> USER_PURETEXT = new LinkedList<>();
		public LinkedList<Long> USER_PICTURES = new LinkedList<>();
		public LinkedList<Message> MESG_MESSAGES = new LinkedList<>();
		public LinkedList<Message> MESG_PURETEXT = new LinkedList<>();
		public LinkedList<Message> MESG_PICTURES = new LinkedList<>();

		public HashMap<Long, UserStatus> USER_STATUS = new HashMap<>();

		public void pic(final long userid, final Message message) {
			this.GROUP_PICTURE++;
			this.USER_STATUS.get(userid).pic(userid, message);
			this.USER_MESSAGES.add(userid);
			this.USER_PICTURES.add(userid);
			this.MESG_MESSAGES.add(message);
			this.MESG_PICTURES.add(message);
		}

		public void say(final long userid, final Message message) {
			this.GROUP_SENTENCE++;
			this.GROUP_CHARACTER += message.length;
			this.USER_STATUS.get(userid).say(userid, message);
			this.USER_MESSAGES.add(userid);
			this.USER_PURETEXT.add(userid);
			this.MESG_PURETEXT.add(message);
			this.MESG_PURETEXT.add(message);
		}
	}

	// ==========================================================================================================================

	private class UserStatus {

		public int USER_PICTURE = 0;
		public int USER_SENTENCE = 0;
		public int USER_CHARACTER = 0;

		public LinkedList<Message> USER_MESSAGES = new LinkedList<>();
		public LinkedList<Message> USER_PCITURES = new LinkedList<>();

		public void pic(final long userid, final Message message) {
			this.USER_PICTURE++;
			this.USER_PCITURES.add(message);
		}

		public void say(final long userid, final Message message) {
			this.USER_SENTENCE++;
			this.USER_CHARACTER += message.length;
			this.USER_MESSAGES.add(message);
		}

	}

	// ==========================================================================================================================

	@Override
	public void memberExit(final long gropid, final long userid) {
		this.GROUP_STATUS.get(gropid).USER_STATUS.remove(userid);
	}

	@Override
	public void memberJoin(final long gropid, final long userid) {
		this.GROUP_STATUS.get(gropid).USER_STATUS.put(userid, new UserStatus());
	}

	// ==========================================================================================================================

	/***
	 * logLevel 选择模式 logMode 发送到群聊还是私聊（管理员）
	 */
	@Override
	public String[] generateReport(final int logLevel, final int logMode, final int typeid, final long userid, final long diszid, final long gropid, final Message message, final Object[] parameters) {

		final StringBuilder builder = new StringBuilder();

		try {
			switch (logLevel) {
			case 0:
				// 0 = 全局按照群总发言数排序
				this.generateGroupsRank();
				break;
			case 1:
				// 1 = 群内按照成员发言数排序
				this.generateMembersRank(logMode, gropid);
				break;
			case 100:
				// 100 = 生成dump
				this.generateDumpFile();
				break;
			}
		} catch (final Exception exce) {
			exce.printStackTrace();
			builder.append("报告生成出错");
			for (final StackTraceElement stack : exce.getStackTrace()) {
				builder.append("\r\n");
				builder.append(stack.getClassName());
				builder.append(".");
				builder.append(stack.getMethodName());
				builder.append("(");
				builder.append(stack.getClass().getSimpleName());
				builder.append(".");
				builder.append(stack.getMethodName());
				builder.append(":");
				builder.append(stack.getLineNumber());
				builder.append(")");
			}
			String res[] = new String[1];
			res[0] = builder.toString();
			return res;
		}
		return null;
	}

	// ==========================================================================================================================

	private void generateGroupsRank() {
		final StringBuilder builder = new StringBuilder();
		final TreeMap<Integer, Long> allGroupRank = new TreeMap<>((a, b) -> b - a);
		for (final long gropid : this.GROUP_STATUS.keySet()) {
			final int tempSentence = this.GROUP_STATUS.get(gropid).GROUP_SENTENCE;
			if (tempSentence > 0) { allGroupRank.put(this.GROUP_STATUS.get(gropid).GROUP_SENTENCE, gropid); }
		}
		builder.append("发言条数：");
		builder.append(this.GLOBAL_SENTENCE);
		builder.append("\r\n发言字数：");
		builder.append(this.GLOBAL_CHARACTER);
		builder.append("\r\n发言图数：");
		builder.append(this.GLOBAL_PICTURE);
		builder.append("\r\n总排行：");
		int i = 1;
		for (final int count : allGroupRank.keySet()) {
			final String groupID = String.valueOf(allGroupRank.get(count));
			builder.append("\r\n");
			builder.append("No.");
			builder.append(i);
			builder.append("：");
			builder.append(groupID.substring(0, 4) + "**" + groupID.substring(6));
			builder.append(" - ");
			builder.append(count);
			builder.append("条/");
			builder.append(this.GROUP_STATUS.get(allGroupRank.get(count)).GROUP_CHARACTER);
			builder.append("字/");
			builder.append(this.GROUP_STATUS.get(allGroupRank.get(count)).GROUP_PICTURE);
			builder.append("图");
			i++;
		}
		Module.userInfo(entry.OPERATOR(), builder.toString());
	}

	// ==========================================================================================================================

	private void generateMembersRank(final int logMode, final long gropid) {

		final StringBuilder builder1 = new StringBuilder();
		final StringBuilder builder2 = new StringBuilder();
		final StringBuilder builder3 = new StringBuilder();
		final StringBuilder builder4 = new StringBuilder();

		final GroupStatus groupStatus = this.GROUP_STATUS.get(gropid);

		int REDPACK = 0;
		int SNAPPIC = 0;

		// ================================================================================

		builder1.append("（1/1）水群统计\r\n发言条数：");
		builder1.append(groupStatus.GROUP_SENTENCE);
		builder1.append("\r\n发言字数：");
		builder1.append(groupStatus.GROUP_CHARACTER);
		builder1.append("\r\n发言图数：");
		builder1.append(groupStatus.GROUP_PICTURE);

		// ================================================================================

		builder2.append("（2/4）成员排行：");
		final TreeMap<Integer, HashSet<Long>> allMemberRank = new TreeMap<>((a, b) -> b - a);
		for (final long userid : groupStatus.USER_STATUS.keySet()) {
			final int userSentence = groupStatus.USER_STATUS.get(userid).USER_SENTENCE;
			if (userSentence > 0) {
				if (allMemberRank.containsKey(userSentence)) {
					allMemberRank.get(userSentence).add(userid);
				} else {
					final HashSet<Long> tempSet = new HashSet<>();
					tempSet.add(userid);
					allMemberRank.put(userSentence, tempSet);
				}
			}
		}

		int i = 1;
		UserStatus userStatus;
		for (final int userRank : allMemberRank.keySet()) {
			final HashSet<Long> tempSet = allMemberRank.get(userRank);
			for (final Long userid : tempSet) {
				userStatus = groupStatus.USER_STATUS.get(userid);
				builder2.append("\r\nNo.");
				builder2.append(i);
				builder2.append(" - ");
				builder2.append(Module_Nick.getNickname(userid));
				builder2.append("(");
				builder2.append(userid);
				builder2.append(") ");
				builder2.append(userStatus.USER_SENTENCE);
				builder2.append("句/");
				builder2.append(userStatus.USER_CHARACTER);
				builder2.append("字/");
				builder2.append(userStatus.USER_PICTURE);
				builder2.append("图");
			}
			i = i + tempSet.size();
		}
		// ================================================================================

		builder3.append("(3/4)最多说的话：");
		final HashMap<String, Integer> allMessageRankTemp = new HashMap<>();
		for (final Message message : groupStatus.MESG_PURETEXT) {
			final String raw = message.rawMessage();
			if (raw.equals("&#91;闪照&#93;请使用新版手机QQ查看闪照。")) {
				SNAPPIC++;
			} else if (raw.equals("&#91;QQ红包&#93;请使用新版手机QQ查收红包。")) {
				REDPACK++;
			} else {
				if (allMessageRankTemp.containsKey(raw)) {
					allMessageRankTemp.put(raw, allMessageRankTemp.get(raw) + 1);
				} else {
					allMessageRankTemp.put(raw, 1);
				}
			}
		}
		final TreeMap<Integer, HashSet<String>> allMessageRank = new TreeMap<>((a, b) -> b - a);
		for (final String raw : allMessageRankTemp.keySet()) {
			final int tempCount = allMessageRankTemp.get(raw);
			if (allMessageRank.containsKey(tempCount)) {
				allMessageRank.get(tempCount).add(raw);
			} else {
				final HashSet<String> tempSet = new HashSet<>();
				tempSet.add(raw);
				allMessageRank.put(tempCount, tempSet);
			}
		}
		i = 1;
		for (final int messageRank : allMessageRank.keySet()) {
			final HashSet<String> tempSet = allMessageRank.get(messageRank);
			for (final String message : tempSet) {
				builder3.append("\r\nNo.");
				builder3.append(i);
				builder3.append(" - ");
				builder3.append(messageRank);
				builder3.append("次：");
				builder3.append(message);
				if (i++ > 25) { break; }
			}
			if (i++ > 25) { break; }
		}

		// ================================================================================

		builder1.append("\r\n闪照数：");
		builder1.append(SNAPPIC);
		builder1.append("\r\n红包数：");
		builder1.append(REDPACK);

		// ================================================================================

		builder4.append("（4/4）最多发的图：");
		final HashMap<String, Integer> allPictureRankTemp = new HashMap<>();
		for (final Message message : groupStatus.MESG_PICTURES) {
			final String raw = message.rawMessage();
			if (allPictureRankTemp.containsKey(raw)) {
				allPictureRankTemp.put(raw, allPictureRankTemp.get(raw) + 1);
			} else {
				allPictureRankTemp.put(raw, 1);
			}
		}
		final TreeMap<Integer, HashSet<String>> allPictureRank = new TreeMap<>((a, b) -> b - a);
		for (final String raw : allPictureRankTemp.keySet()) {
			final int tempCount = allPictureRankTemp.get(raw);
			if (allPictureRank.containsKey(tempCount)) {
				allPictureRank.get(tempCount).add(raw);
			} else {
				final HashSet<String> tempSet = new HashSet<>();
				tempSet.add(raw);
				allPictureRank.put(tempCount, tempSet);
			}
		}
		i = 1;
		for (final int pictureRank : allPictureRank.keySet()) {
			final HashSet<String> tempSet = allPictureRank.get(pictureRank);
			for (final String picture : tempSet) {
				builder4.append("\r\nNo.");
				builder4.append(i);
				builder4.append(" - ");
				builder4.append(pictureRank);
				builder4.append("次：");
				builder4.append(JcqApp.CC.getCQImage(picture).getUrl());
				if (i++ > 3) { break; }
			}
			if (i++ > 3) { break; }
		}

		if (logMode == 0) {
			Module.userInfo(entry.OPERATOR(), builder1.toString());
			Module.userInfo(entry.OPERATOR(), builder2.toString());
			Module.userInfo(entry.OPERATOR(), builder3.toString());
			Module.userInfo(entry.OPERATOR(), builder4.toString());
		} else {
			Module.gropInfo(gropid, builder1.toString());
			Module.gropInfo(gropid, builder2.toString());
			Module.gropInfo(gropid, builder3.toString());
			Module.gropInfo(gropid, builder4.toString());
		}

	}

	// ==========================================================================================================================

	private void generateDumpFile() throws IOException {
		final String dumpTime = LoggerX.time("yyyy.MM.dd-HH.mm.ss");
		final File dumpFolder = Paths.get(entry.FOLDER_DATA().getAbsolutePath(), "TopSpeakDump_" + dumpTime).toFile();
		dumpFolder.mkdirs();
		for (final long tempGropid : this.GROUP_STATUS.keySet()) {
			final File groupContainerFolder = Paths.get(dumpFolder.getAbsolutePath(), "Group_" + tempGropid).toFile();
			groupContainerFolder.mkdir();
			final File dumpByTimeline = Paths.get(groupContainerFolder.getAbsolutePath(), "Main.txt").toFile();
			dumpByTimeline.createNewFile();
			final BufferedWriter groupTimelineWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpByTimeline), "UTF-8"));
			final GroupStatus tempGroupStatus = this.GROUP_STATUS.get(tempGropid);
			groupTimelineWriter.write("群号：" + tempGropid);
			groupTimelineWriter.write("\r\n发言条数：" + tempGroupStatus.GROUP_SENTENCE);
			groupTimelineWriter.write("\r\n发言字数：" + tempGroupStatus.GROUP_CHARACTER);
			groupTimelineWriter.write("\r\n发言图数：" + tempGroupStatus.GROUP_PICTURE);
			groupTimelineWriter.write("\r\n");
			for (int i = 0; i < tempGroupStatus.GROUP_SENTENCE; i++) {
				final long tempSender = tempGroupStatus.USER_MESSAGES.get(i);
				final Message tempMessage = tempGroupStatus.MESG_MESSAGES.get(i);
				groupTimelineWriter.write("\r\n[" + LoggerX.time(new Date(tempMessage.sendTime)) + "]" + JcqApp.CQ.getStrangerInfo(tempSender).getNick() + "(" + tempSender + "): " + tempMessage.rawMessage());
			}
			groupTimelineWriter.flush();
			groupTimelineWriter.close();
			for (final long tempUserid : tempGroupStatus.USER_STATUS.keySet()) {
				final File dumpByUseid = Paths.get(groupContainerFolder.getAbsolutePath(), "User_" + tempUserid + ".txt").toFile();
				dumpByUseid.createNewFile();
				final BufferedWriter useridWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpByUseid), "UTF-8"));
				final UserStatus tempUserStatus = tempGroupStatus.USER_STATUS.get(tempUserid);
				useridWriter.write("成员：" + tempUserid);
				useridWriter.write("\r\n昵称：" + JcqApp.CQ.getStrangerInfo(tempUserid).getNick());
				useridWriter.write("\r\n发言条数：" + tempUserStatus.USER_SENTENCE);
				useridWriter.write("\r\n发言字数：" + tempUserStatus.USER_CHARACTER);
				useridWriter.write("\r\n发言图数：" + tempGroupStatus.USER_PICTURES);
				useridWriter.write("\r\n");
				for (final Message tempMessage : tempUserStatus.USER_MESSAGES) {
					useridWriter.write("\r\n[" + LoggerX.time(new Date(tempMessage.sendTime)) + "]: " + tempMessage.rawMessage());
				}
				useridWriter.flush();
				useridWriter.close();
			}
		}
	}
}
