import java.io.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * ���ݻ�ȡ�������������
 * @author zycheng4
 */
public class DataGenerator {
	/**
	 * �����������ɵ��û���Ϣ��id��[�����Ϣ]
	 */
	private HashMap<String, String[]> entityInfo = new HashMap<>();
	/**
	 * ���ɳ�ͻ�ĸ���
	 */
	private double fraudRate;
	/**
	 * �����������
	 */
	private int ageLow;
	/**
	 * �������䷶Χ
	 */
	private int ageDiff;
	/**
	 * �������������
	 */
	private int incomeBound;
	/**
	 * JSON�ļ��Ĵ��λ��
	 */
	private String writePath;
	/**
	 * �洢���еĵ�����
	 */
	private ArrayList<String> idDistrictCode = new ArrayList<>();
	/**
	 * ��¼�����Ѿ����ɹ��������˻�
	 */
	private ArrayList<String> bankAccount = new ArrayList<>();
	/**
	 * ��¼�����Ѿ����ɹ��ĵ绰����
	 */
	private ArrayList<String> phoneNumber = new ArrayList<>();
	/**
	 * �����������
	 * @see Random
	 */
	private Random rand = new Random();

	/**
	 * @param writePath JSON�ļ��Ĵ��λ�ã�ֻ���ļ���ʱĬ�ϴ�����Ŀ��Ŀ¼��
	 * @param fraudRate ���ɳ�ͻ�ĸ���
	 * @param ageLow �����������
	 * @param ageHigh �����������
	 * @param incomeBound �������������
	 * @throws IOException �׳��������ļ���ȡ�쳣
	 */
	public DataGenerator(String writePath, double fraudRate, int ageLow, int ageHigh, int incomeBound)
			throws IOException {
		// sanity check
		assert ageLow < ageHigh;
		assert fraudRate < 1 && fraudRate > 0;

		loadDistrictCode();
		this.fraudRate = fraudRate;
		this.ageLow = ageLow;
		this.ageDiff = ageHigh - ageLow;
		this.incomeBound = incomeBound;
		this.writePath = writePath;
	}

	/**
	 * ����һ���������û�������ͼ�׵ĳ�ʼ״̬��
	 * @param numInput ����ԭʼ�û���������ԭʼ�û�һ��ӵ�е�������������ɵ��û�������ڴ�����
	 * @throws IOException �׳�JSON�ļ���CSV�ļ�д���쳣
	 */
	public void generate(int numInput) throws IOException {
		for (int i=0; i < numInput; i++) {
			String[] result = generateNode(false);
			if (result == null) i--;
			else entityInfo.put(result[0], Arrays.copyOfRange(result, 1, result.length));
		}

		HashMap<String, String[]> entityInfoBonus = new HashMap<>();
		String[] keys = entityInfo.keySet().toArray(new String[0]);
		for (HashMap.Entry<String, String[]> entry: entityInfo.entrySet()) {
			String[] guarantorInfo = generateGuarantor(keys, entry.getKey(), entry.getValue());
			if (!entityInfo.containsKey(guarantorInfo[0]))
				entityInfoBonus.put(guarantorInfo[0], Arrays.copyOfRange(guarantorInfo, 1, guarantorInfo.length));
		}
		entityInfo.putAll(entityInfoBonus);

		FileIO.writeToJSON(entityInfo, writePath);
		FileIO.writeToCSV(entityInfo);
	}

	/**
	 * ����һ���µ��û���ͬʱ�����䵣���ˡ�
	 * @param notRand ���Ϊtrue��������еĵ绰����/�����˻�����ȥ������������ɣ�Ŀ����Ϊ�������ͻ
	 * @return ���������ɵ��û����䵣���˵���Ϣ
	 */
	public List<String[]> generateNewEntry(boolean notRand) {
		String[] keys = entityInfo.keySet().toArray(new String[0]);
		String[] result = null;
		while (result == null) {
			result = generateNode(false);
		}
		result[9] = String.valueOf(0);
		if (notRand) {
			result[6] = entityInfo.get(keys[rand.nextInt(keys.length)])[5];
			result[7] = entityInfo.get(keys[rand.nextInt(keys.length)])[6];
		}

		rand.setSeed(System.nanoTime());
		entityInfo.put(result[0], Arrays.copyOfRange(result, 1, result.length));
		String[] guarantorInfo = generateGuarantor(keys, result[0], entityInfo.get(result[0]));
		if (!entityInfo.containsKey(guarantorInfo[0]))
			entityInfo.put(guarantorInfo[0], Arrays.copyOfRange(guarantorInfo, 1, guarantorInfo.length));

		ArrayList<String[]> temp = new ArrayList<>();
		System.arraycopy( entityInfo.get(result[0]), 0, result, 1, result.length-1);
		temp.add(result);
		temp.add(guarantorInfo);
		return temp;
	}

	/**
	 * ��ȡ�����롣
	 * @throws IOException �׳���ȡ�ļ��쳣
	 */
	private void loadDistrictCode() throws IOException {
		// ���ص�����
		BufferedReader br = new BufferedReader(new FileReader("districtcode.txt"));
		String str;
		while((str = br.readLine()) != null) {
			String data = str.trim();
			if (!data.isEmpty() && !data.substring(4, 6).equals("00")) idDistrictCode.add(data.substring(0, 6));
		}
		br.close();
	}

	/**
	 * ��һ�û��������������
	 * @param guarantor ���Ϊtrue�����ɵ����ˣ��������ɵ��ǽ����
	 * @return �������ɵ��û���Ϣ��{"����", "����", "�Ա�", "����", "�����", "�ֻ�����", "�����˻�", "��������",
	 * "����״̬", "Ŀ��", "������", "������ID", "�뵣���˹�ϵ"}
	 */
	public String[] generateNode(boolean guarantor) {
		int age = rand.nextInt(ageDiff) + ageLow;
		int gender = rand.nextInt(2);
		String id = Helper.generateId(age, gender, idDistrictCode);
		if (entityInfo.containsKey(id)) return null;
		String name = Helper.generateName(gender);
		int income = rand.nextInt(incomeBound) + 2000;
		int loanAmount = guarantor ? income * 3 : rand.nextInt(income * 3);

		String phoneNum = Helper.generatePhoneNum();
		if (rand.nextInt(30) < fraudRate * 10 && this.phoneNumber.size()>0)
			phoneNum = this.phoneNumber.get(rand.nextInt(this.phoneNumber.size()));
		else this.phoneNumber.add(phoneNum);

		String bankAccount = String.valueOf(rand.nextInt(1000000000) + 1000000000).substring(1);
		if (rand.nextInt(30) < fraudRate * 10 && this.bankAccount.size()>0)
			bankAccount = this.bankAccount.get(rand.nextInt(this.bankAccount.size()));
		else this.bankAccount.add(bankAccount);

		/* trust: {"A", "B", "C", "D", "E", "F"} / {"��������", "������", "������", "������", "������", "������"}; */
		int trust = rand.nextInt(6);

		/*
		 loanStatus: {"Applying", "Paying", "Fully Paid"} / {"������", "������", "�ѻ���"};
		 �ѻ�����û�������D����������
		*/
		int loanStatus = trust==5 ? rand.nextInt(3) : rand.nextInt(3);
		if (loanStatus == 2 && trust == 4) trust = rand.nextInt(4);

		/* purpose: {"house", "vehicle", "tuition", "everyday life"} / {"��", "��", "����", "����"}; */
		int purpose = rand.nextInt(4);

		/*
		 Store data into HashMap for further process
		 { "Name", "Age", "Gender", "Income", "Loan Amount", "Phone Number", "Bank Account", "Trust Level",
				"Loan Status", "Loan Purpose", "Guarantor", "GuarantorID", "Relation"};
		*/
		String[] info = {
				id, name, String.valueOf(age), String.valueOf(gender),	String.valueOf(income),
				String.valueOf(loanAmount), phoneNum, bankAccount, String.valueOf(trust),
				String.valueOf(loanStatus), String.valueOf(purpose), null, null, null
		};
		if (guarantor) for (int j : new int[] {9, 10}) info[j] = null;
		return info;
	}

	/**
	 * Ϊһ��������ҵ��䵣���˲���������������һ������������һ�������˸��塣
	 * @param keys ��ǰ�����û���id
	 * @param id ��ǰ�û���id
	 * @param info ��ǰ�û�����Ϣ�����ﴫ������û���Ϣ������
	 * @return ���ص����˵���Ϣ
	 */
	@NotNull
	private String[] generateGuarantor(String[] keys, String id, @NotNull String[] info) {
		// random sample a guarantor from existing user
		String guarantor = null;
		String[] guarantorInfo = info.clone();
		if (rand.nextInt(3) < 1) {
			guarantor = keys[rand.nextInt(keys.length)];
			guarantorInfo = entityInfo.get(guarantor);
		}

		if (rand.nextInt(30) < fraudRate * 10) {
			guarantor = id;
			guarantorInfo = info.clone();
		}

		// or generate a new user
		while (guarantor == null) {
			String[] result = generateNode(true);
			guarantor = result[0];
			guarantorInfo = Arrays.copyOfRange(result, 1, result.length);
		}
		info[info.length - 3] = guarantorInfo[0];
		info[info.length - 2] = guarantor;

		info[info.length - 1] = "0";
		guarantorInfo[info.length - 1] = "0";

//		String[] relations = {"��ϵ����", "����", "����", "ĸ��", "����", "Ů��"};
//		int ageDiff = Integer.parseInt(guarantorInfo[1]) - Integer.parseInt(info[1]);
//		if (ageDiff < -50) {
//
//		} else if (ageDiff < -25) {
//
//		} else if (ageDiff < 25) {
//			info[info.length - 1] = ;
//		} else if (ageDiff < 50) {
//
//		} else {
//
//		}
//		entityInfo.put(id, info);
		String[] temp = new String[info.length + 1];
		temp[0] = guarantor;
		System.arraycopy(guarantorInfo, 0, temp, 1, info.length);
		return temp;
	}
}
