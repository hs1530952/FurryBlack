package studio.blacktech.coolqbot.furryblack.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import com.sobte.cqp.jcq.entity.Member;
import com.sobte.cqp.jcq.event.JcqApp;

import studio.blacktech.common.LoggerX;
import studio.blacktech.coolqbot.furryblack.entry;
import studio.blacktech.coolqbot.furryblack.module.Message;
import studio.blacktech.coolqbot.furryblack.module.ModuleListener;

@SuppressWarnings("unused")
public class Listener_TopSpeak extends ModuleListener {

	private int TOTAL_GLOBAL = 0;
	private int LENGTH_GLOBAL = 0;
	private HashMap<Long, GroupStatus> STORAGE = new HashMap<Long, GroupStatus>();

	public Listener_TopSpeak() {
		this.MODULE_DISPLAYNAME = "ˮȺͳ��";
		this.MODULE_PACKAGENAME = "shui";
		this.MODULE_DESCRIPTION = "ˮȺͳ��";
		this.MODULE_VERSION = "3.2.2";
		this.MODULE_USAGE = new String[] {};
		this.MODULE_PRIVACY_TRIGER = new String[] {};
		this.MODULE_PRIVACY_LISTEN = new String[] {
				"��ȡ��Ϣ������", "��ȡ��Ϣ��Ϣ"
		};
		this.MODULE_PRIVACY_STORED = new String[] {};
		this.MODULE_PRIVACY_CACHED = new String[] {};
		this.MODULE_PRIVACY_OBTAIN = new String[] {};

	}

	@Override
	public boolean doUserMessage(int typeid, long userid, Message message, int messageid, int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doDiszMessage(long diszid, long userid, Message message, int messageid, int messagefont) throws Exception {
		return false;
	}

	@Override
	public boolean doGropMessage(long gropid, long userid, Message message, int messageid, int messagefont) throws Exception {
		this.TOTAL_GLOBAL++;
		this.LENGTH_GLOBAL = this.LENGTH_GLOBAL + message.length;
		if (!this.STORAGE.containsKey(gropid)) {
			this.STORAGE.put(gropid, new GroupStatus());
		}
		this.STORAGE.get(gropid).speak(userid, message);
		return true;
	}

	private class GroupStatus {

		public int TOTAL_GROUP = 0;
		public int LENGTH_GROUP = 0;
		public LinkedList<Message> gropMessages = new LinkedList<Message>();
		public HashMap<Long, UserStatus> userStatus = new HashMap<Long, UserStatus>();

		public void speak(long userid, Message message) {
			this.TOTAL_GROUP++;
			this.LENGTH_GROUP = this.LENGTH_GROUP + message.length;
			if (!this.userStatus.containsKey(userid)) {
				this.userStatus.put(userid, new UserStatus());
			}
			this.gropMessages.add(message);
			this.userStatus.get(userid).speak(message);
		}
	}

	private class UserStatus {

		public int TOTAL_MEMBER = 0;
		public int LENGTH_MEMBER = 0;
		public LinkedList<Message> userMessages = new LinkedList<Message>();

		public void speak(Message message) {
			this.TOTAL_MEMBER++;
			this.LENGTH_MEMBER = this.LENGTH_MEMBER + message.length;
			this.userMessages.add(message);
		}
	}

	/***
	 *
	 */
	@Override
	public String generateReport(int logLevel, int logMode, Message message, Object[] parameters) {

		long timeStart = System.currentTimeMillis();

		StringBuilder builder = new StringBuilder();

		try {
			switch (logLevel) {
			case 0:
				// 0 = ����Ⱥ�ܷ�������Ⱥ����
				this.generateGroupsRank(builder);
				break;
			case 1:
				// 1 = Ⱥ�ڰ��ճ�Ա����������
				this.generateMembersRank(builder, parameters);
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			case 100:
				// 100 = ����dump�ļ�
				this.generateDumpFile(builder);
				break;

			}

		} catch (Exception exce) {
			exce.printStackTrace();
			builder.append("�������ɳ���");
			for (StackTraceElement stack : exce.getStackTrace()) {
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
			return builder.toString();
		}

		long timeFinsh = System.currentTimeMillis();

		builder.append("\r\n\r\n���ɱ��濪���� ");
		builder.append(timeFinsh - timeStart);
		builder.append("ms");

		return builder.toString();
	}

	/***
	 * ����Ⱥ���ܷ���������
	 *
	 * @param builder
	 */
	private void generateGroupsRank(StringBuilder builder) {

		int i = 0;

		TreeMap<Integer, Long> allGroupRank = new TreeMap<Integer, Long>((a, b) -> b - a);

		// ���ú������Ⱥ���ܷ�����������
		for (long gropid : this.STORAGE.keySet()) {
			allGroupRank.put(this.STORAGE.get(gropid).TOTAL_GROUP, gropid);
		}

		builder.append("ȫ���ܷ���������");
		builder.append(this.TOTAL_GLOBAL);
		builder.append("\r\nȫ���ܷ���������");
		builder.append(this.LENGTH_GLOBAL);
		builder.append("\r\nȫ��Ⱥ���У�");

		for (int count : allGroupRank.keySet()) {
			i++;
			builder.append("\r\n");
			builder.append("No.");
			builder.append(i);
			builder.append("��");
			builder.append(allGroupRank.get(count));
			builder.append(" - ");
			builder.append(count);
			builder.append("��/");
			builder.append(this.STORAGE.get(allGroupRank.get(count)).LENGTH_GROUP);
			builder.append("��");
		}
	}

	/***
	 * ָ��Ⱥ����������Ա����
	 *
	 * @param builder
	 */
	private void generateMembersRank(StringBuilder builder, Object[] parameters) {

		long gropid = (long) parameters[0];
		GroupStatus groupStatus = this.STORAGE.get(gropid);
		Member member;
		int i = 0;

		TreeMap<Integer, Long> allMemberRank = new TreeMap<Integer, Long>((a, b) -> b - a);

		builder.append("�ܷ���������");
		builder.append(groupStatus.TOTAL_GROUP);
		builder.append("\r\n�ܷ���������");
		builder.append(groupStatus.LENGTH_GROUP);
		builder.append("\r\n��Ա���У�");

		// ���ú��������Ա�ķ�����������
		for (long userid : groupStatus.userStatus.keySet()) {
			allMemberRank.put(groupStatus.userStatus.get(userid).TOTAL_MEMBER, userid);
		}

		Long userid;
		UserStatus userStatus;
		for (int userRank : allMemberRank.keySet()) {
			i++;
			userid = allMemberRank.get(userRank);
			userStatus = groupStatus.userStatus.get(userid);
			builder.append("\r\nNo.");
			builder.append(i);
			builder.append(" - ");
			builder.append(JcqApp.CQ.getGroupMemberInfoV2(gropid, userid).getNick());
			builder.append("(");
			builder.append(userid);
			builder.append(") ");
			builder.append(userStatus.TOTAL_MEMBER);
			builder.append("��/");
			builder.append(userStatus.LENGTH_MEMBER);
			builder.append("��");
		}
	}

	private void generateDumpFile(StringBuilder builder) throws IOException {

		// DUMPʱ��
		String dumpTime = LoggerX.time("yyyy.MM.dd-HH.mm.ss");

		GroupStatus tempGroup;
		UserStatus tempUser;

		BufferedWriter writer;

		// ������Ŀ¼
		File dumpFolder = Paths.get(entry.FOLDER_DATA().getAbsolutePath(), "TopSpeak_Dump_" + dumpTime).toFile();
		dumpFolder.mkdirs();

		// ������Ŀ¼ ��� ��Ⱥʱ����
		File dumpGroupsFolder = Paths.get(entry.FOLDER_DATA().getAbsolutePath(), "main").toFile();

		// Ϊÿ��Ⱥ����һ��Ŀ¼ ��� ����Ա����
		File dumpGroupByTimeline;

		for (long gropid : this.STORAGE.keySet()) {

			tempGroup = this.STORAGE.get(gropid);

			dumpGroupByTimeline = Paths.get(dumpGroupsFolder.getAbsolutePath(), Long.toString(gropid) + ".txt").toFile();
			dumpGroupByTimeline.createNewFile();

			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpGroupByTimeline), "UTF-8"));

			writer.write("Ⱥ�ţ�" + gropid);
			writer.write("\r\n����������" + tempGroup.TOTAL_GROUP);
			writer.write("\r\n����������" + tempGroup.LENGTH_GROUP);
			writer.write("\r\n");

			for (Message tempMessage : tempGroup.gropMessages) {
				writer.write("\r\n  " + LoggerX.time(new Date(tempMessage.sendTime)) + ":" + tempMessage.rawMessage);
			}

			writer.flush();
			writer.close();
		}

		// �ٴ�ѭ�������Ա���࣬�ֿ�д�����쳣��ɵ�Ӱ�췶Χ
		File dumpGroupByUserFolder;
		File dumpGroupByUser;

		for (long gropid : this.STORAGE.keySet()) {

			tempGroup = this.STORAGE.get(gropid);

			dumpGroupByUserFolder = Paths.get(dumpFolder.getAbsolutePath(), Long.toString(gropid)).toFile();

			dumpGroupByUserFolder.mkdirs();

			for (long qqid : tempGroup.userStatus.keySet()) {

				tempUser = tempGroup.userStatus.get(qqid);

				dumpGroupByUser = Paths.get(dumpGroupByUserFolder.getAbsolutePath(), Long.toString(qqid) + ".txt").toFile();
				dumpGroupByUser.createNewFile();

				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpGroupByUser), "UTF-8"));

				writer.write("��Ա��" + qqid);
				writer.write("\r\n����������" + tempUser.TOTAL_MEMBER);
				writer.write("\r\n����������" + tempUser.LENGTH_MEMBER);

				writer.write("\r\n");

				for (Message tempMessage : tempUser.userMessages) {

					writer.write("\r\n  " + LoggerX.time(new Date(tempMessage.sendTime)) + ":" + tempMessage.rawMessage);
				}
			}
		}
		builder.append("�����ڴ澵���ѱ���");
	}
}