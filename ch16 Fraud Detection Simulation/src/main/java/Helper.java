import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zycheng4
 */
public class Helper {
	/**
	 * 随机数生成器
	 * @see Random
	 */
	private static Random rand = new Random();

	/**
	 * 将一条数据转换为便于人类理解的字符串，可以选择打印至标准输出流。
	 * @param id 用户id
	 * @param info 用户信息
	 * @param CN 是否输出中文
	 * @param print 是否打印至标准输出流
	 * @return 转换后的用户信息
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
		if (CN) { // 中文
			trustLevels = new String[] {"超高信用", "高信用", "中信用", "低信用", "无信用", "黑名单"};
			loanStatusMap = new String[] {"申请中", "还款中", "已还款"};
			purposeLst = new String[] {"买房", "买车", "教育", "生活"};
			relation = new String[] {"朋友"/*TODO*/};
			description = new String[] {"姓名", "年龄", "性别", "收入", "贷款额", "手机号码", "银行账户", "信用评级",
					"贷款状态",	"目的", "担保人", "担保人ID", "与担保人关系"};
			infoPrint[2] = info[2].equals("0") ? "女": "男";
			nothing = "无";
		}

		// 将贷款状态、信用评级、贷款目的由代码转成String。
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
	 * 姓名随机生成器，名根据性别生成，有50%的概率生成单字名。
	 * 姓名数据来源: https://blog.csdn.net/qq1300375795/article/details/78368690
	 * @param gender 性别
	 * @return 生成的姓名
	 */
	static String generateName(int gender) {
		final String firstName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛" +
				"奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和" +
				"穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐" +
				"邱骆高夏蔡田樊胡凌霍虞万支柯咎管卢莫经房裘缪干解应宗宣丁贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於" +
				"惠甄魏加封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭厉戎祖武符刘姜詹束龙" +
				"叶幸司韶郜黎蓟薄印宿白怀蒲台从鄂索咸籍赖卓蔺屠蒙池乔阴郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮" +
				"牛寿通边扈燕冀郏浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庚终暨居衡步都耿满弘匡国文寇广禄阙东殴殳" +
				"沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空沙鞠须巢关蒯相查后江红游竺权逯盖益桓俟"; // 350
		final String girl = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞" +
				"凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨" +
				"瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽"; // 149
		final String boy = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富" +
				"顺信子杰涛昌成康星光天达安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮" +
				"政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘"; // 151

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
	 * 用户ID生成器，使用身份证格式：6位地区码 + 8位出生日期 + 2位顺序码 + 性别 + 尾号
	 * 地区码来源: https://blog.csdn.net/qq_24221531/article/details/84584695
	 * @param age 年龄
	 * @param gender 性别
	 * @param idDistrictCode 地区码
	 * @return 用户id
	 */
	@NotNull
	static String generateId(int age, int gender, @NotNull ArrayList<String> idDistrictCode) {
		String id = "";

		// 生成地区编号
		id += idDistrictCode.get(rand.nextInt(idDistrictCode.size()));

		// 生成年月日
		// 使用系统时间与年龄反推出生年份，并随机生成生日
		String currYear = new SimpleDateFormat("yyyy").format(System.currentTimeMillis());
		int year = Integer.parseInt(currYear) - age;
		Calendar cal = Calendar.getInstance();
		cal.set(year, Calendar.JANUARY, 0);
		cal.add(Calendar.DAY_OF_YEAR, rand.nextInt(366));
		id += new SimpleDateFormat("yyyyMMdd").format(cal.getTime());

		// 生成顺序码：【0-99】随机数
		int randNum = rand.nextInt(100);
		id += randNum<10 ? "0" + randNum : String.valueOf(randNum);

		// 生成性别：
		id += rand.nextInt(5) * 2 + gender;

		// 生成尾号: 此处省略尾号X
		id += rand.nextInt(10);

		assert id.length() == 18;
		return id;
	}

	/**
	 * 电话号码随机生成器
	 * @return 电话号码
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
