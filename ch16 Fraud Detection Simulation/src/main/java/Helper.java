import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zycheng4
 */
public class Helper {
	/**
	 * �����������
	 * @see Random
	 */
	private static Random rand = new Random();

	/**
	 * ��һ������ת��Ϊ�������������ַ���������ѡ���ӡ����׼�������
	 * @param id �û�id
	 * @param info �û���Ϣ
	 * @param CN �Ƿ��������
	 * @param print �Ƿ��ӡ����׼�����
	 * @return ת������û���Ϣ
	 */
	@NotNull
	public static String[] printInfo(String id, @NotNull String[] info, boolean CN, boolean print) {
		String[] infoPrint = info.clone();
		String[] trustLevels = {"Excellent", "High", "Medium", "Low", "Critical", "BlackList"};
		String[] loanStatusMap = {"Applying", "Paying", "Fully Paid"};
		String[] purposeLst = {"house", "vehicle", "tuition", "everyday life"};
		String[] relation = {"friend"/*TODO*/};
		String[] description = { "Name", "Age", "Gender", "Income", "Loan Amount", "Phone Number",
				"Bank Account", "Trust Level", "Loan Status", "Loan Purpose", "Guarantor", "GuarantorID",
				"Relation"};
		infoPrint[2] = info[2].equals("0") ? "Female": "Male";
		String nothing = "No Data";
		if (CN) { // ����
			trustLevels = new String[] {"��������", "������", "������", "������", "������", "������"};
			loanStatusMap = new String[] {"������", "������", "�ѻ���"};
			purposeLst = new String[] {"��", "��", "����", "����"};
			relation = new String[] {"����"/*TODO*/};
			description = new String[] {"����", "����", "�Ա�", "����", "�����", "�ֻ�����", "�����˻�", "��������",
					"����״̬",	"Ŀ��", "������", "������ID", "�뵣���˹�ϵ"};
			infoPrint[2] = info[2].equals("0") ? "Ů": "��";
			nothing = "��";
		}

		// ������״̬����������������Ŀ���ɴ���ת��String��
		infoPrint[7] = info[7]==null ? null : trustLevels[Integer.parseInt(info[7])];
		infoPrint[8] = info[8]==null ? null : loanStatusMap[Integer.parseInt(info[8])];
		infoPrint[9] = info[9]==null ? null : purposeLst[Integer.parseInt(info[9])];
		infoPrint[info.length-1] = relation[Integer.parseInt(info[info.length-1])];
		for (int i = 0; i < infoPrint.length; i++) infoPrint[i] = infoPrint[i] == null ? nothing : infoPrint[i];

		if (print) {
			System.out.println("ID: " + id);
			for (int i = 0; i < description.length; i++) System.out.print(description[i] + ": " + infoPrint[i] + "; ");
			System.out.print('\n');
		}
		return infoPrint;
	}

	/**
	 * ����������������������Ա����ɣ���50%�ĸ������ɵ�������
	 * ����������Դ: https://blog.csdn.net/qq1300375795/article/details/78368690
	 * @param gender �Ա�
	 * @return ���ɵ�����
	 */
	static String generateName(int gender) {
		final String firstName = "��Ǯ��������֣��������������������������ʩ�ſײ��ϻ���κ�ս���л������ˮ��������˸�" +
				"�ɷ�����³Τ������ﻨ������Ԭ��ۺ��ʷ�Ʒ����Ѧ�׺����������ޱϺ�����������ʱ��Ƥ���뿵����Ԫ������ƽ�ƺ�" +
				"������Ҧ��տ����ë����ױ���갼Ʒ��ɴ�̸��é���ܼ�������ף������������ϯ����ǿ��·¦Σ��ͯ�չ�÷ʢ�ֵ�����" +
				"������Ĳ��﷮���������֧�¾̹�¬Ī�������Ѹɽ�Ӧ�������ڵ��������������ʯ�޼�ť�������ϻ���½���������" +
				"����κ�ӷ����ഢ���������ɾ��θ����ڽ��͹�����ɽ�ȳ������ȫۭ�����������������ﱩ�����������������ղ����" +
				"Ҷ��˾��۬�輻��ӡ�ް׻���̨�Ӷ����̼���׿�����ɳ����������ܲ�˫��ݷ����̷�����̼������Ƚ��۪Ӻȴ�ɣ���" +
				"ţ��ͨ�����༽ۣ����ũ�±�ׯ�̲����ֳ�Ľ����ϰ�°���������������θ����߾Ӻⲽ�����������Ŀܹ�»�ڶ�Ź�" +
				"����εԽ��¡ʦ�������˹��������������Ǽ��Ŀ�ɳ���볲�������󽭺�����Ȩ�ָ��滸ٹ"; // 350
		final String girl = "���Ӣ���������Ⱦ���������֥��Ƽ�����ҷ���ʴ��������÷���������滷ѩ�ٰ���ϼ����ݺ������" +
				"���Ѽ�������������Ҷ�������������ɺɯ������ٻ�������ӱ¶������������Ǻɵ���ü������ޱݼ���Է�ܰ" +
				"�������԰��ӽ�������ع���ѱ�ˬ������ϣ����Ʈ�����������������������ܿ�ƺ������˿ɼ���Ӱ��֦˼��"; // 149
		final String boy = "ΰ�����㿡��ǿ��ƽ�����Ļ�������������־��������ɽ�ʲ���������Ԫȫ��ʤѧ��ŷ���������ɱ�" +
				"˳���ӽ��β��ɿ��ǹ���ﰲ����ï�����м�ͱ벩���Ⱦ�����׳��˼Ⱥ���İ�����ܹ����ƺ���������ԣ���ܽ�������" +
				"��ǫ�����֮�ֺ��ʲ����������������ά�������������󳿳�ʿ�Խ��������׵���ʱ̩ʢ��衾��ڲ�����ŷ纽��"; // 151

		int idx = rand.nextInt(firstName.length()-1);
		String name = firstName.substring(idx, idx+1);
		if (gender == 0) {
			idx = rand.nextInt(girl.length()-1);
			name += girl.substring(idx, idx+1);
			idx = rand.nextInt(girl.length()-1);
			if (rand.nextInt(2) == 1) name += girl.substring(idx, idx+1);
			return name;
		}
		idx = rand.nextInt(boy.length()-1);
		name += boy.substring(idx, idx+1);
		idx = rand.nextInt(boy.length()-1);
		if (rand.nextInt(2) == 1) name += boy.substring(idx, idx + 1);
		return name;
	}

	/**
	 * �û�ID��������ʹ�����֤��ʽ��6λ������ + 8λ�������� + 2λ˳���� + �Ա� + β��
	 * ��������Դ: https://blog.csdn.net/qq_24221531/article/details/84584695
	 * @param age ����
	 * @param gender �Ա�
	 * @param idDistrictCode ������
	 * @return �û�id
	 */
	@NotNull
	static String generateId(int age, int gender, @NotNull ArrayList<String> idDistrictCode) {
		String id = "";

		// ���ɵ������
		id += idDistrictCode.get(rand.nextInt(idDistrictCode.size()));

		// ����������
		// ʹ��ϵͳʱ�������䷴�Ƴ�����ݣ��������������
		String currYear = new SimpleDateFormat("yyyy").format(System.currentTimeMillis());
		int year = Integer.parseInt(currYear) - age;
		Calendar cal = Calendar.getInstance();
		cal.set(year, Calendar.JANUARY, 0);
		cal.add(Calendar.DAY_OF_YEAR, rand.nextInt(366));
		id += new SimpleDateFormat("yyyyMMdd").format(cal.getTime());

		// ����˳���룺��0-99�������
		int randNum = rand.nextInt(100);
		id += randNum<10 ? "0" + randNum : String.valueOf(randNum);

		// �����Ա�
		id += rand.nextInt(5) * 2 + gender;

		// ����β��: �˴�ʡ��β��X
		id += rand.nextInt(10);

		assert id.length() == 18;
		return id;
	}

	/**
	 * �绰�������������
	 * @return �绰����
	 */
	@NotNull
	static String generatePhoneNum() {
		String[] telFirst = ("130, 131, 132, 134, 135, 136, 137, 138, 139, 145, 147, 149, 150, 151, 152, 153, 155, " +
				"156, 157, 158, 159, 170, 171, 172, 173, 175, 176, 177, 178, 180, 181, 182, 184, 185, 186, 187, " +
				"188, 189").split(", ");
		String phoneNum = telFirst[rand.nextInt(telFirst.length)];
		phoneNum += String.valueOf(rand.nextInt(100000000) + 100000000).substring(1);
		return phoneNum;
	}
}
