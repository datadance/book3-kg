import java.io.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * 数据获取：随机生成数据
 * @author zycheng4
 */
public class DataGenerator {
	/**
	 * 储存所有生成的用户信息：id，[相关信息]
	 */
	private HashMap<String, String[]> entityInfo = new HashMap<>();
	/**
	 * 生成冲突的概率
	 */
	private double fraudRate;
	/**
	 * 最低申请年龄
	 */
	private int ageLow;
	/**
	 * 申请年龄范围
	 */
	private int ageDiff;
	/**
	 * 最高申请人收入
	 */
	private int incomeBound;
	/**
	 * JSON文件的存放位置
	 */
	private String writePath;
	/**
	 * 存储所有的地区码
	 */
	private ArrayList<String> idDistrictCode = new ArrayList<>();
	/**
	 * 记录所有已经生成过的银行账户
	 */
	private ArrayList<String> bankAccount = new ArrayList<>();
	/**
	 * 记录所有已经生成过的电话号码
	 */
	private ArrayList<String> phoneNumber = new ArrayList<>();
	/**
	 * 随机数生成器
	 * @see Random
	 */
	private Random rand = new Random();

	/**
	 * @param writePath JSON文件的存放位置，只有文件名时默认存在项目根目录下
	 * @param fraudRate 生成冲突的概率
	 * @param ageLow 最低申请年龄
	 * @param ageHigh 最高申请年龄
	 * @param incomeBound 最高申请人收入
	 * @throws IOException 抛出地区码文件读取异常
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
	 * 生成一定数量的用户，构成图谱的初始状态。
	 * @param numInput 生成原始用户的数量。原始用户一定拥有担保人项，最终生成的用户数会高于此数字
	 * @throws IOException 抛出JSON文件或CSV文件写入异常
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
	 * 生成一个新的用户，同时生成其担保人。
	 * @param notRand 如果为true，则从已有的电话号码/银行账户中拿去，否则随机生成；目的是为了制造冲突
	 * @return 返回新生成的用户与其担保人的信息
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
	 * 读取地区码。
	 * @throws IOException 抛出读取文件异常
	 */
	private void loadDistrictCode() throws IOException {
		// 加载地区码
		BufferedReader br = new BufferedReader(new FileReader("districtcode.txt"));
		String str;
		while((str = br.readLine()) != null) {
			String data = str.trim();
			if (!data.isEmpty() && !data.substring(4, 6).equals("00")) idDistrictCode.add(data.substring(0, 6));
		}
		br.close();
	}

	/**
	 * 单一用户的随机生成器。
	 * @param guarantor 如果为true，生成担保人，否则生成的是借贷人
	 * @return 返回生成的用户信息：{"姓名", "年龄", "性别", "收入", "贷款额", "手机号码", "银行账户", "信用评级",
	 * "贷款状态", "目的", "担保人", "担保人ID", "与担保人关系"}
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

		/* trust: {"A", "B", "C", "D", "E", "F"} / {"超高信用", "高信用", "中信用", "低信用", "无信用", "黑名单"}; */
		int trust = rand.nextInt(6);

		/*
		 loanStatus: {"Applying", "Paying", "Fully Paid"} / {"申请中", "还款中", "已还款"};
		 已还款的用户至少是D级信用评级
		*/
		int loanStatus = trust==5 ? rand.nextInt(3) : rand.nextInt(3);
		if (loanStatus == 2 && trust == 4) trust = rand.nextInt(4);

		/* purpose: {"house", "vehicle", "tuition", "everyday life"} / {"买房", "买车", "教育", "生活"}; */
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
	 * 为一个借贷人找到其担保人并将两者相连，有一定概率新生成一个担保人个体。
	 * @param keys 当前所有用户的id
	 * @param id 当前用户的id
	 * @param info 当前用户的信息，这里传入的是用户信息的引用
	 * @return 返回担保人的信息
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

//		String[] relations = {"旁系亲属", "朋友", "父亲", "母亲", "儿子", "女儿"};
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
